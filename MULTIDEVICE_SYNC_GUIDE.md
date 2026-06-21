# Multi-Device Real-Time Synchronization & Historical Catch-Up Implementation

## Overview

This implementation enables the Cat-Tastic POS application to support multiple concurrent devices (tablet terminal + owner phone) with:

- **Real-time order synchronization** between all connected devices
- **Historical data catch-up** when a secondary device connects
- **Soft-delete voiding** with propagation across devices
- **Conflict resolution** using cloud state as the source of truth

## Architecture

### Sync Model

```
Tablet Terminal (Device A)          Phone Terminal (Device B)           Supabase Cloud
───────────────────────────────────────────────────────────────────────────────────────

Creates order #123                                                      
    ↓                                                                    
Saves to local Room DB                                                  
    ↓                                                                    
Marks as PENDING                                                        
    ↓                                                                    
SyncWorker uploads → ────────────────────────────────────────────────→ orders table
                                                                        (id: xxxx123)
                                                                        is_voided: false
                        
Voids order #123 locally
    ↓
Updates isVoided=true, syncStatus=PENDING
    ↓
                                                                        
SyncWorker: PATCH → ───────────────────────────────────────────────→ orders table
           {is_voided: true}                                          (id: xxxx123)
                                                                       is_voided: true

                                                            App startup
                                                                ↓
                                                            SyncWorker starts
                                                                ↓
                                                            [Download Phase]
                                                            Fetch all orders
                                                                ↓
                                                            Check local DB
                                                                ↓
                                                            Order #123 exists?
                                                            - YES: Update void status
                                                            - NO: Insert new order
                                                                ↓
                                                            isVoided=true synced
                                                                ↓
                                                            UI refreshes Flow
                                                                ↓
                                                            Voided orders filtered
```

### Database Schema (v16)

**Orders Table:**
```
Column             Type      Purpose
─────────────────────────────────────────────────────
id                 LONG      Unique order ID
timestamp          LONG      Order creation time
isVoided           INT(0/1)  Soft-delete flag
syncStatus         TEXT      PENDING | SYNCED
lastSyncedAt       LONG      Last sync timestamp
deviceId           TEXT      Source device ID
is_served          INT(0/1)  Served status
...other fields...
```

**Supabase orders Table (Must Match):**
```
Column              Type        Purpose
────────────────────────────────────────────────
id                  BIGINT      Global order ID
is_voided           BOOLEAN     Void status
last_synced_at      TIMESTAMP   Sync timestamp
device_id           TEXT        Source device
timestamp           BIGINT      Order time
...other fields...
```

## Implementation Details

### 1. Database Migration (PosDatabase.kt)

Version 15 → 16 adds:
- `isVoided INTEGER NOT NULL DEFAULT 0` column
- `lastSyncedAt INTEGER NOT NULL DEFAULT 0` column

```kotlin
val MIGRATION_15_16 = object : androidx.room.migration.Migration(15, 16) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE orders ADD COLUMN isVoided INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE orders ADD COLUMN lastSyncedAt INTEGER NOT NULL DEFAULT 0")
    }
}
```

### 2. SyncWorker Three-Phase Sync

#### Phase A: Catalog Upload (Existing)
Uploads local categories, items, inventory, recipes to Supabase with merge-duplicates strategy.

#### Phase B: Order Upload (Enhanced)
```kotlin
// Upload PENDING local orders to cloud
val unsyncedOrders = database.orderDao()
    .getOrdersPage(...).filter { it.order.syncStatus == "PENDING" }

for (order in unsyncedOrders) {
    // Include is_voided flag in upload
    orderJson.put("is_voided", order.isVoided)
    
    // POST to Supabase
    // Mark as SYNCED with lastSyncedAt timestamp
}
```

#### Phase C: Historical Download (NEW)
```kotlin
// Download ALL orders from Supabase
val response = client.newCall(
    GET("$supabaseUrl/rest/v1/orders?select=*")
).execute()

// For each cloud order:
for (cloudOrder in response) {
    val localOrderId = cloudOrder.id % 1_000_000_000L
    val existingLocal = dao.getOrderWithItems(localOrderId)
    
    if (existingLocal == null) {
        // NEW order from cloud: Insert with SYNCED status
        dao.insertOrderWithItems(orderEntity, itemsFromCloud)
    } else {
        // EXISTING order locally: Update state from cloud
        if (existingLocal.order.isVoided != cloudOrder.isVoided
            || existingLocal.order.isServed != cloudOrder.isServed) {
            dao.updateOrderEntity(existingLocal.order.copy(
                isVoided = cloudOrder.isVoided,
                isServed = cloudOrder.isServed,
                syncStatus = "SYNCED"
            ))
        }
    }
    
    // Download related order items
    itemsFromCloud = client.newCall(
        GET("$supabaseUrl/rest/v1/order_items?order_id=eq.${cloudOrder.id}")
    ).execute()
}
```

### 3. Soft-Delete Implementation (OrderRepositoryImpl)

Traditional delete() replaced with soft-delete:

```kotlin
override suspend fun deleteOrder(orderId: Long) {
    // Mark as voided instead of removing
    val existingOrder = database.orderDao().getOrderWithItems(orderId)
    if (existingOrder != null) {
        database.orderDao().updateOrderEntity(
            existingOrder.order.copy(
                isVoided = true,
                syncStatus = "PENDING",
                lastSyncedAt = System.currentTimeMillis()
            )
        )
    }

    // Async push to cloud
    async {
        patchSupabase("{\"is_voided\": true}")
    }
}
```

### 4. Data Filtering (OrderDao)

All queries exclude voided orders:

```kotlin
// Before:
SELECT * FROM orders WHERE timestamp >= :start AND timestamp <= :end

// After:
SELECT * FROM orders 
WHERE timestamp >= :start AND timestamp <= :end
AND isVoided = 0
```

Applies to:
- `observeOrdersPage()` - Active orders list
- `getOrdersPage()` - Paginated orders
- `getGrossSalesForDay()` - Dashboard stats
- `getCashSalesForDay()` - Payment analytics
- `observeCashierSalesForDay()` - Staff performance

Voided orders remain in DB for audit trail but hidden from UI.

## Testing Guide

### Prerequisites
1. Two devices (tablet + phone)
2. Supabase project configured with:
   - `is_voided BOOLEAN` column in orders table
   - `last_synced_at TIMESTAMP` column in orders table

### Test Scenario 1: Order Sync on First Connect

**Setup:**
- Device A (Tablet): Fresh install, creates 5 orders

**Action:**
- Device B (Phone): Install app, configure same Supabase

**Expected Result:**
- App startup → SyncWorker runs
- Historical download phase pulls all 5 orders
- Phone shows same orders as tablet
- NO app restart required

### Test Scenario 2: Multi-Device Void Propagation

**Setup:**
- Both devices connected, showing same orders
- Order #123 exists on both

**Action 1 (Tablet voids order):**
```
1. Tap void on order #123
2. deleteOrder(123) → isVoided=true, syncStatus=PENDING
3. SyncWorker uploads: PATCH is_voided=true to Supabase
4. Verify Supabase shows order 123 with is_voided=true
```

**Action 2 (Phone auto-syncs):**
```
1. Wait for SyncWorker or open Settings → Sync
2. SyncWorker's download phase fetches order 123
3. Finds local order 123 exists
4. Updates: isVoided=true, syncStatus=SYNCED
5. observeOrdersPage() emits updated list
6. Order #123 disappears from active orders (filtered)
7. Dashboard stats recalculated (excludes voided)
```

**Verification:**
- [ ] Tablet shows order as voided/hidden
- [ ] Phone automatically hides same order
- [ ] No error messages
- [ ] Both devices' dashboards match
- [ ] Supabase shows is_voided=true

### Test Scenario 3: Offline Void + Sync Recovery

**Setup:**
- Phone offline, both devices have order #456

**Action:**
```
1. Tablet: Create order #456 (syncs to cloud)
2. Phone: Offline mode, creates order #789 (stored local)
3. Phone goes online
4. SyncWorker: Uploads order #789 to cloud
5. SyncWorker: Downloads all orders including #456
6. Both devices have #456 and #789
```

### Test Scenario 4: Dashboard Stats Accuracy

**Setup:**
- 10 orders created, 3 are voided

**Expected:**
- Gross Sales = sum of 7 active orders (excludes 3 voided)
- Cash Sales = only CASH payment orders that aren't voided
- Discounts = sum of discounts from 7 active orders
- Top Item = calculated from 7 active orders only

**Verification:**
```kotlin
// Orders view
assertEquals(7, observeOrdersPage().count())

// Dashboard
assertEquals(expectedTotal, getGrossSalesForDay().value)
assertFalse(voidedOrderIds.any { it in getCashSalesForDay().value })
```

## Deployment Steps

### 1. Update Supabase Schema

Ensure your Supabase `orders` table has:
```sql
ALTER TABLE orders ADD COLUMN IF NOT EXISTS is_voided BOOLEAN DEFAULT false;
ALTER TABLE orders ADD COLUMN IF NOT EXISTS last_synced_at TIMESTAMP DEFAULT NOW();
```

### 2. Build & Test Locally

```bash
./gradlew assembleDebug
# App size: ~20 MB

# Test on emulator/device
# Verify database migration applies
# Check SyncWorker logs
```

### 3. Release to GitHub

```bash
# APK location:
# app/build/outputs/apk/debug/app-debug.apk

# Create GitHub release 1.1.0 with:
# - APK file
# - Release notes (see below)
# - Database schema SQL for Supabase
```

### 4. User Installation

1. Users install new APK
2. App auto-runs migration 15→16
3. SyncWorker triggers immediately
4. Historical orders download on first startup
5. Both devices sync automatically

## Release Notes Template

```markdown
# Cat-Tastic POS v1.1.0 - Multi-Device Synchronization

## ✨ New Features

### Multi-Device Real-Time Sync
- Tablet and phone can now sync in real-time
- All devices show same order state simultaneously
- No app restart needed for updates

### Historical Data Catch-Up
- When a secondary device connects, all historical orders download automatically
- Orders, items, inventory sync seamlessly
- Device becomes ready immediately after sync completes

### Soft-Delete Order Voiding
- Voided orders no longer permanently deleted
- Void status propagates to all devices
- Complete audit trail maintained

### Improved Dashboard Accuracy
- Sales stats now exclude voided orders
- Top-selling items calculated from active orders only
- Cashier performance reflects actual sales

## 📝 Database Changes

**Schema Version: 15 → 16**

New columns in `orders` table:
- `isVoided` (INTEGER/BOOLEAN): Tracks if order was voided
- `lastSyncedAt` (INTEGER/TIMESTAMP): Last sync timestamp for conflict resolution

Supabase schema must match - contact admin if not updated.

## 🔄 How It Works

1. **Order Created:** Saved locally, marked PENDING
2. **Sync:** Uploaded to Supabase, marked SYNCED
3. **Other Device Connects:** Downloads all orders from Supabase
4. **Order Voided:** Marked isVoided=true, pushed to Supabase
5. **All Devices Update:** Void status synced automatically

## ✅ Testing Instructions

1. Install on two devices
2. Create orders on tablet
3. Open phone - orders appear automatically
4. Void an order on tablet
5. Phone automatically hides same order
6. Check dashboard - stats updated on both

## 🐛 Known Limitations

- Requires internet for sync (offline mode queues updates)
- Supabase must have is_voided column
- First sync may take longer (downloads all historical data)

## 📊 Migration Notes

- Automatic database migration on first launch
- No data loss
- Can rollback to v1.0 (requires database downgrade)

---

Built with ❤️ for seamless multi-terminal operations
```

## Troubleshooting

### Issue: Voided orders still appear on phone

**Cause:** Phone hasn't synced yet
**Solution:** Open app, wait 30s for SyncWorker, or manually trigger sync

### Issue: Order items missing on phone after download

**Cause:** order_items table query failed
**Check:** 
```sql
SELECT * FROM order_items WHERE order_id = <supabase_order_id>
```

### Issue: Database migration fails

**Cause:** Corrupted database or missing tables
**Solution:**
```
1. Clear app data
2. Reinstall app
3. Migration 15→16 runs fresh
```

### Issue: Sync freezes or takes too long

**Cause:** Downloading large order history (1000+ orders)
**Solution:**
```
1. Check network connection
2. Check Supabase quota (API rate limits)
3. Consider archiving very old orders
```

## Performance Considerations

| Metric | Target | Notes |
|--------|--------|-------|
| Initial Sync | < 30s | For 100 orders |
| Upload Pending | < 5s | 1-2 orders typical |
| Download Updates | < 10s | Periodic checks |
| Void Propagation | < 5min | Eventually consistent |
| UI Response | Instant | Reactive via Flow |

---

**Documentation Version:** 1.0  
**Last Updated:** 2026-06-21  
**Status:** Ready for Production
