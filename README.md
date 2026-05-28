# Brew-Ni-Cat Coffee Shop POS

<p align="center">
  <img src="app/src/main/res/drawable/logo.png" width="250" alt="Brew-Ni-Cat Logo">
</p>

## Project Overview
**Brew-Ni-Cat Coffee Shop POS** is a fully featured, offline-first Android Point-of-Sale (POS) application designed for a cat-themed business. It manages menus, dynamic inventory recipes, orders, split payments, and end-of-day financial reporting without requiring a constant internet connection.

## Key Features
* **Dynamic Inventory Engine (Recipe/BOM mapping):** Link custom raw materials to specific menu variants for accurate, real-time stock deduction during checkout.
* **Financial Analytics (Z-Reading):** Comprehensive end-of-day reports including total sales, cash flow, custom expenses, and detailed order timelines.
* **Dual-Mode Payments (Cash/GCash):** Seamlessly split payments between cash and e-wallets, with automated change calculation.
* **Bluetooth Printing:** Built-in ESC/POS service to directly print professional customer receipts.
* **Data Export:** Export order histories and Z-Reading summaries to CSV for external accounting.

## Tech Stack
* **Language:** Kotlin
* **UI Toolkit:** Jetpack Compose (Material 3)
* **Local Database:** Room (SQLite)
* **Architecture:** MVVM (Model-View-ViewModel) + Repository Pattern
* **Concurrency:** Kotlin Coroutines & Flow

## Setup Instructions
1. Clone this repository to your local machine.
2. Open the project in **Android Studio**.
3. Allow Gradle to sync dependencies.
4. Click **Run** (`Shift + F10`) to build and deploy the app to an emulator or connected Android device.
*Note: A Bluetooth ESC/POS printer is required to test the printing functionality on a physical device.*

## License
MIT License

Copyright (c) 2026 Brew-Ni-Cat Coffee Shop

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
