# Student Expense Tracker (ExTrack) 📱💰

A modern, feature-rich Android application built with **Jetpack Compose** and **Kotlin**, designed specifically to help students and young adults manage expenses, track budgets, split group bills effortlessly, scan receipts, and gain smart financial insights.

---

## ✨ Key Features

- **Dashboard & Financial Health**: Get an instant overview of your total balance, income, expenses, and a **Survival Calculator** estimating how many days your remaining funds will sustain you.
- **Transaction Management**: Easily add, edit, categorize, filter, and delete income and expense transactions.
- **Budgeting & Limits**: Set monthly or category-specific budgets to keep your spending in check.
- **Group Expense Splitting & Debt Simplifier**: Create expense groups, split bills among friends, and use an automated **Debt Simplifier** algorithm to minimize the number of transactions needed to settle debts.
- **Receipt Scanner**: Scan and parse receipts to quickly log expenses on the go.
- **Mood & Spending Insights**: Analyze how your emotional state (mood) correlates with your spending habits through automated reports and insights.
- **Local Persistence & Security**: All data is stored securely offline using **Room Database**.

---

## 🛠️ Tech Stack & Architecture

- **UI & Design**: Jetpack Compose, Material 3 design system, custom animations, and responsive layouts.
- **Architecture**: MVVM (Model-View-ViewModel) architectural pattern combined with repository pattern and unidirectional data flow (UDF).
- **Asynchronous Programming**: Kotlin Coroutines & `StateFlow`.
- **Local Storage**: Room Database for relational data & Jetpack DataStore for user preferences.
- **Dependency Injection**: Hilt.
- **Background Processing**: WorkManager (for periodic survival/budget reminders).

---

## 📂 Project Structure

```text
com.example.studentexpensetracker/
│
├── data/
│   ├── local/          # Room Database, DAOs, and Entities (Transactions, Categories, Splits)
│   ├── preferences/    # Jetpack DataStore preferences repository
│   └── repository/     # Data repositories (TransactionRepository, SplitRepository)
│
├── di/                 # Hilt Dependency Injection modules (DatabaseModule, DataStoreModule)
│
├── domain/             # Business logic (DebtSimplifier, ReceiptParser, SurvivalCalculator)
│
├── ui/                 # Jetpack Compose UI screens and ViewModels
│   ├── budget/         # Budget management
│   ├── components/     # Reusable UI components (SwipeableItem, etc.)
│   ├── home/           # Dashboard screen
│   ├── navigation/     # App navigation graph & routes
│   ├── reports/        # Reports, charts, and mood insights
│   ├── scanner/        # Receipt scanner
│   ├── settings/       # App preferences
│   ├── split/          # Group expenses and debt settlement
│   ├── theme/          # Material 3 colors, typography, and theme
│   └── transactions/   # Add, edit, and list transactions
│
└── worker/             # Background WorkManager workers
```

---

## 🚀 Getting Started

1. **Clone the repository**:
   ```bash
   git clone https://github.com/Revanthgowda45/expense_tracker_app.git
   ```
2. **Open the project** in [Android Studio](https://developer.android.com/studio) (Koala or newer recommended).
3. **Sync project with Gradle files**.
4. **Run the app** on an Android emulator or physical device (Minimum SDK 26, Target SDK 34/35).

---

## 📄 License

This project is open-source and available under the [MIT License](LICENSE).
