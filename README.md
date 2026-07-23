# 🐾 Brew ni Cat POS
## *Bionic Glass UI, Live Orders & Editable Receipts*

<p align="center">
  <img src="app/src/main/res/drawable/logo.png" width="250" alt="Brew-Ni-Cat Logo">
</p>

**Brew ni Cat POS** is a high-performance, offline-first Android Point-of-Sale system built for modern retail. It pairs a **Bionic Glass** design language with a **Room v12** data layer, reactive Kotlin Flow pipelines, and human-readable receipt numbers—delivering a premium, adaptive experience for fast-paced, cat-themed retail environments.

---

## 🚀 What's New in v1.1.0

Production milestone (`versionCode 10100`) bringing the core POS into a reactive, glass-themed, and operationally flexible architecture:

* **Bionic Glass UI:** System-wide adaptive glass surfaces (`AdaptiveGlassDialog`, `AdaptiveGlassCard`) with `iOSSpringSpec` / `liquidSwipeSpring` physics, collapsing headers, and complementary dual-tone themes tuned for both dark and light mode.
* **Styled payment & hold flows:** Cash/GCash checkout and **Hold Order** use glass dialogs with segment chips, rounded inputs, and primary actions aligned with the design system.
* **Catalog search:** Full-width dashboard search with flat results while typing and grouped category browsing when idle.
* **Step-by-step product picker:** One screen at a time for style, flavor, and size—with liquid edge-to-edge swipe transitions instead of a long scrollable sheet.
* **Incremental receipt numbers:** Auto-increment order IDs displayed as `Order #0001` on receipts, history, exports, and prints.
* **Editable receipts:** Swipe an order card left in History to reveal **Share**, **Edit**, and **Delete**; edit line items, discounts, and add menu items with inventory adjustment on save.
* **Reactive order history:** Live `observeOrdersPage()` streams replace one-shot fetches; collapsible expense/order timelines and expandable order cards keep the log scannable.
* **Dynamic operations:** Zero-migration `PaymentConfigJson` for cashier and GCash SIM management from App Settings.
* **Responsive storefront:** Phones use a full-width catalog with a collapsed order bar and modal checkout sheet; tablets keep a dual-pane catalog + cart split.
* **Timezone-safe reporting:** UTC/local date-picker mapping for accurate history filters and Z-Reading ranges.
* **Inventory recipe clarity:** Size-selected BOM editor surfaces inherited base mappings (e.g. cups) alongside size-specific rows.

> **Data & upgrade note**
> - **Day-to-day use:** Orders, inventory, settings, and config persist normally between sessions on the same install.
> - **App update (v11 → v12):** Menu, inventory, and settings are preserved. Order history may reset when the v12 migration runs.
> - **Uninstall / reinstall:** Auto Backup is disabled to prevent restoring an incompatible pre-v12 database. Reinstalls start fresh unless you restore from **Export**. Full reinstall retention is planned for **v1.1.1**.

---

## 🏗️ Core Architecture

| Layer | Technology |
| :--- | :--- |
| **Language** | Kotlin (Coroutines, Flow) |
| **UI Toolkit** | Jetpack Compose + Material 3 |
| **Database** | Room (v12) with reactive streams |
| **Architecture** | MVVM + Repository + Use Cases |
| **Physics** | iOS-inspired spring dynamics (`iOSSpringSpec`, `liquidSwipeSpring`) |

---

## 📋 Key Features

* **Adaptive catalog & checkout:** Step-by-step size/flavor picker, collapsed mobile order bar with modal checkout, held-order queues, and discount strategies (None, 5%, 10%, 20%, Free).
* **Editable receipt workflow:** Post-checkout corrections via `UpdateOrderUseCase` with transactional inventory adjustment.
* **Adaptive inventory engine:** Multi-variant BOM mapping (sizes + flavors) with flavor-aware checkout deduction.
* **Financial integrity:** Z-Reading summaries, expense logging, cashier sales breakdown, and export to Downloads.
* **Order history log:** Real-time paging, date-range filter, swipe-to-reveal share/edit/void, and collapsible timeline sections.
* **Operational admin:** App Settings for business goals, cashiers, GCash SIMs, and theme accents; direct navigation to History and Inventory from the dashboard.
* **Bluetooth printing:** ESC/POS service for hardware receipt and Z-Reading output.

---

## ⚙️ Setup Instructions

1. **Clone the repository**
   ```bash
   git clone https://github.com/RodneeGlenMartin/Brew-ni-Cat-POS.git
   ```
2. **Open in Android Studio** — latest stable release recommended.
3. **Sync Gradle** and wait for dependencies to resolve.
4. **Run on device or emulator** — **Run** (`Shift + F10`) or:
   ```bash
   ./gradlew assembleDebug
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```
   *Pair a Bluetooth ESC/POS printer if you plan to test print workflows on physical hardware.*

---

## 🛡️ License

MIT License. Copyright (c) 2026 Brew-Ni-Cat Coffee Shop.
