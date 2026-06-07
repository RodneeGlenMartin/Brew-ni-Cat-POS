# 🐾 Brew ni Cat POS
## *Bionic Glass UI, Live Orders & Editable Receipts*

<p align="center">
  <img src="app/src/main/res/drawable/logo.png" width="250" alt="Brew-Ni-Cat Logo">
</p>

**Brew ni Cat POS** is a high-performance, offline-first Android Point-of-Sale system built for modern retail. It pairs a **Bionic Glass** design language with a **Room v12** data layer, reactive Kotlin Flow pipelines, and human-readable receipt numbers—delivering a premium, adaptive experience for fast-paced, cat-themed retail environments.

---

## 🚀 What's New in v1.1.0

Production milestone (`versionCode 10100`) bringing the core POS into a reactive, glass-themed, and operationally flexible architecture:

* **Bionic Glass UI:** System-wide adaptive glass surfaces (`AdaptiveGlassDialog`, `AdaptiveGlassCard`) with `iOSSpringSpec` physics, collapsing headers, and complementary dual-tone themes tuned for both dark and light mode.
* **Styled payment checkout:** Cash/GCash segment chips, rounded inputs, and a primary confirm flow aligned with the glass design system.
* **Catalog search:** Full-width dashboard search with flat results while typing and grouped category browsing when idle.
* **Incremental receipt numbers:** Auto-increment order IDs displayed as `Order #0001` on receipts, history, exports, and prints.
* **Editable receipts:** Open any order from History to change line items, adjust discounts, add menu items, and save—with inventory restored for removed lines and deducted for new ones.
* **Reactive order history:** Live `observeOrdersPage()` streams replace one-shot fetches; collapsible order cards keep the timeline scannable.
* **Dynamic operations:** Zero-migration `PaymentConfigJson` for cashier and GCash SIM management from Settings (PIN-protected).
* **Responsive storefront:** Single-column mobile catalog with overlay checkout; dual-pane catalog + cart on tablets.
* **Timezone-safe reporting:** UTC/local date-picker mapping for accurate history filters and Z-Reading ranges.
* **Inventory recipe clarity:** Size-selected BOM editor surfaces inherited base mappings (e.g. cups) alongside size-specific rows.

> **Upgrade note:** v1.1.0 moves orders to Room **v12** with auto-increment IDs. Existing order history may reset on upgrade when the v12 migration runs.

---

## 🏗️ Core Architecture

| Layer | Technology |
| :--- | :--- |
| **Language** | Kotlin (Coroutines, Flow) |
| **UI Toolkit** | Jetpack Compose + Material 3 |
| **Database** | Room (v12) with reactive streams |
| **Architecture** | MVVM + Repository + Use Cases |
| **Physics** | iOS-inspired spring dynamics (`iOSSpringSpec`) |

---

## 📋 Key Features

* **Adaptive catalog & checkout:** Product config sheets (size/flavor), collapsible cart panel, held-order queues, and discount strategies (None, 5%, 10%, 20%, Free).
* **Editable receipt workflow:** Post-checkout corrections via `UpdateOrderUseCase` with transactional inventory adjustment.
* **Adaptive inventory engine:** Multi-variant BOM mapping (sizes + flavors) with flavor-aware checkout deduction.
* **Financial integrity:** Z-Reading summaries, expense logging, cashier sales breakdown, and export to Downloads.
* **Order history log:** Real-time paging, date-range filter, receipt share, and void with inventory restoration.
* **Operational admin:** PIN-protected app settings; direct navigation to History and Inventory from the dashboard.
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
