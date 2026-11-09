package com.example.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import java.text.NumberFormat
import java.util.Locale

enum class AppLanguage(
    val code: String,
    val label: String,
    val countryName: String,
    val flagEmoji: String,
    val currencySymbol: String,
    val currencyCode: String,
    val langFamily: String
) {
    BANGLADESH("bn", "বাংলা", "Bangladesh", "🇧🇩", "৳", "BDT", "bn"),
    UNITED_STATES("en", "English (US)", "United States", "🇺🇸", "$", "USD", "en"),
    UNITED_KINGDOM("en_uk", "English (UK)", "United Kingdom", "🇬🇧", "£", "GBP", "en"),
    CANADA("en_ca", "English (CA)", "Canada", "🇨🇦", "$", "CAD", "en"),
    AUSTRALIA("en_au", "English (AU)", "Australia", "🇦🇺", "$", "AUD", "en"),
    UAE("ar_ae", "العربية (الإمارات)", "United Arab Emirates", "🇦🇪", "د.إ", "AED", "ar"),
    SAUDI_ARABIA("ar_sa", "العربية (السعودية)", "Saudi Arabia", "🇸🇦", "﷼", "SAR", "ar"),
    GERMANY("de", "Deutsch", "Germany", "🇩🇪", "€", "EUR", "de"),
    FRANCE("fr", "Français", "France", "🇫🇷", "€", "EUR", "fr"),
    NETHERLANDS("nl", "Nederlands", "Netherlands", "🇳🇱", "€", "EUR", "nl"),
    SWITZERLAND("de_ch", "Deutsch (Schweiz)", "Switzerland", "🇨🇭", "CHF", "CHF", "de");

    companion object {
        val ENGLISH get() = UNITED_STATES
        val BANGLA get() = BANGLADESH

        fun fromCode(code: String?): AppLanguage {
            if (code == null) return BANGLADESH
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) }
                ?: if (code.startsWith("en", ignoreCase = true)) UNITED_STATES
                else if (code.startsWith("ar", ignoreCase = true)) UAE
                else if (code.startsWith("de", ignoreCase = true)) GERMANY
                else if (code.startsWith("fr", ignoreCase = true)) FRANCE
                else if (code.startsWith("nl", ignoreCase = true)) NETHERLANDS
                else BANGLADESH
        }
    }
}

val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.BANGLADESH }

enum class AppCurrency(
    val code: String,
    val symbol: String,
    val displayName: String,
    val countryName: String,
    val flagEmoji: String
) {
    BDT("BDT", "৳", "Bangladeshi Taka", "Bangladesh", "🇧🇩"),
    USD("USD", "$", "US Dollar", "United States", "🇺🇸"),
    EUR("EUR", "€", "Euro", "Eurozone", "🇪🇺"),
    GBP("GBP", "£", "British Pound", "United Kingdom", "🇬🇧"),
    INR("INR", "₹", "Indian Rupee", "India", "🇮🇳"),
    SAR("SAR", "﷼", "Saudi Riyal", "Saudi Arabia", "🇸🇦"),
    AED("AED", "د.إ", "UAE Dirham", "United Arab Emirates", "🇦🇪"),
    CAD("CAD", "$", "Canadian Dollar", "Canada", "🇨🇦"),
    AUD("AUD", "$", "Australian Dollar", "Australia", "🇦🇺"),
    CHF("CHF", "₣", "Swiss Franc", "Switzerland", "🇨🇭"),
    JPY("JPY", "¥", "Japanese Yen", "Japan", "🇯🇵"),
    MYR("MYR", "RM", "Malaysian Ringgit", "Malaysia", "🇲🇾"),
    SGD("SGD", "S$", "Singapore Dollar", "Singapore", "🇸🇬"),
    QAR("QAR", "ر.ق", "Qatari Riyal", "Qatar", "🇶🇦"),
    KWD("KWD", "د.ك", "Kuwaiti Dinar", "Kuwait", "🇰🇼");

    fun format(amount: Double): String {
        val formatted = NumberFormat.getNumberInstance(Locale.US).format(amount)
        return when (code) {
            "AED", "SAR", "QAR", "KWD" -> "$formatted $symbol"
            else -> "$symbol $formatted"
        }
    }

    companion object {
        fun fromCode(code: String?): AppCurrency {
            if (code == null) return BDT
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: BDT
        }
    }
}

val LocalAppCurrency = staticCompositionLocalOf { AppCurrency.BDT }

object StringKeys {
    const val APP_NAME = "app_name"
    const val NET_BALANCE = "net_balance"
    const val TOTAL_INCOME = "total_income"
    const val TOTAL_EXPENSE = "total_expense"
    const val ACCOUNTS = "accounts"
    const val STATEMENTS = "statements"
    const val GOALS = "goals"
    const val BILLS = "bills"
    const val CHATBOT = "chatbot"
    const val SETTINGS = "settings"
    const val HOME = "home"
    const val REPORTS = "reports"
    
    // Quick Actions
    const val ADD_ACCOUNT = "add_account"
    const val ADD_TRANSACTION = "add_transaction"
    const val ADD_INCOME = "add_income"
    const val ADD_EXPENSE = "add_expense"
    const val ADD_GOAL = "add_goal"
    const val ADD_BILL = "add_bill"
    const val AI_TIPS = "ai_tips"
    const val SAVING_GOALS = "saving_goals"
    const val BILLS_AND_DUES = "bills_and_dues"
    
    // Quick Utilities: Debts & Habits
    const val DEBTS = "debts"
    const val DEBTS_SUBTITLE = "debts_subtitle"
    const val HABITS = "habits"
    const val HABITS_SUBTITLE = "habits_subtitle"
    const val LENT = "lent"
    const val BORROWED = "borrowed"
    const val TO_RECEIVE = "to_receive"
    const val TO_PAY = "to_pay"
    const val NET_STATUS = "net_status"
    const val OVERVIEW = "overview"
    const val ACTIVE_RECORDS = "active_records"
    const val PERSON_NAME = "person_name"
    const val REPAID = "repaid"
    const val SETTLED = "settled"
    const val REPAY = "repay"
    const val REPAY_TITLE = "repay_title"
    const val NO_DEBTS = "no_debts"
    const val NO_DEBTS_HINT = "no_debts_hint"
    
    // Habits
    const val HABITS_TITLE = "habits_title"
    const val HABITS_DESC = "habits_desc"
    const val INTERVAL = "interval"
    const val ROUTINE_NAME = "routine_name"
    const val LAST_DONE = "last_done"
    const val NEXT_DUE = "next_due"
    const val DAYS_LEFT = "days_left"
    const val DUE_TODAY = "due_today"
    const val OVERDUE = "overdue"
    const val MARK_DONE = "mark_done"
    const val MARK_DONE_CONFIRM = "mark_done_confirm"
    const val NO_HABITS = "no_habits"
    const val NO_HABITS_HINT = "no_habits_hint"
    const val ADD_HABIT = "add_habit"
    const val ESTIMATED_COST = "estimated_cost"
    
    // Accounts & Statements
    const val BANK = "bank"
    const val CASH = "cash"
    const val BKASH = "bkash"
    const val NAGAD = "nagad"
    const val ROCKET = "rocket"
    const val INCOME = "income"
    const val SPENT = "spent"
    const val ALL = "all"
    const val RECENT_TRANSACTIONS = "recent_transactions"
    const val SEE_ALL = "see_all"
    const val VIEW = "view"
    const val NO_TRANSACTIONS = "no_transactions"
    const val NO_ACCOUNTS = "no_accounts"
    const val NO_GOALS = "no_goals"
    const val NO_BILLS = "no_bills"
    const val UPCOMING_BILL = "upcoming_bill"
    
    // Forms & Fields
    const val AMOUNT = "amount"
    const val TITLE = "title"
    const val CATEGORY = "category"
    const val ACCOUNT = "account"
    const val DATE = "date"
    const val NOTE = "note"
    const val SAVE = "save"
    const val CANCEL = "cancel"
    const val DELETE = "delete"
    const val CONFIRM = "confirm"
    const val DUE_DATE = "due_date"
    const val NOTIFY = "notify"
    const val PAID = "paid"
    const val UNPAID = "unpaid"
    const val MARK_AS_PAID = "mark_as_paid"
    const val TARGET_AMOUNT = "target_amount"
    const val SAVED_AMOUNT = "saved_amount"
    const val REMAINING = "remaining"
    const val ADD_FUNDS = "add_funds"
    const val WITHDRAW = "withdraw"
    
    // Auth & Settings
    const val TAGLINE = "tagline"
    const val OR = "or"
    const val SIGNING_IN = "signing_in"
    const val SIGN_IN_WITH_GOOGLE = "sign_in_with_google"
    const val CONTINUE_GUEST = "continue_guest"
    const val SIGN_OUT = "sign_out"
    const val THEME_DARK = "theme_dark"
    const val THEME_LIGHT = "theme_light"
    const val THEME_SYSTEM = "theme_system"
    const val LANGUAGE = "language"
    const val COUNTRY = "country"
    const val SELECT_COUNTRY = "select_country"
    const val PROFILE = "profile"
    const val CURRENCY_BDT = "currency_bdt"
    const val FINANCIAL_UTILITIES = "financial_utilities"
    
    // Chatbot
    const val CHAT_PLACEHOLDER = "chat_placeholder"
    const val QUICK_QUESTIONS = "quick_questions"
    const val SALARY = "salary"
    const val PATHAO_DRIVE = "pathao_drive"
    const val TIPS = "tips"
    const val BUSINESS = "business"
    const val FOOD = "food"
    const val FAMILY = "family"
    const val RENTAL_COST = "rental_cost"
    const val OTHERS = "others"
    const val EMPTY_STATE_HINT = "empty_state_hint"
}

// 1. English Translations (en)
private val enStrings = mapOf(
    StringKeys.APP_NAME to "My Wallet",
    StringKeys.NET_BALANCE to "Net Balance",
    StringKeys.TOTAL_INCOME to "Total Income",
    StringKeys.TOTAL_EXPENSE to "Total Spent",
    StringKeys.ACCOUNTS to "Accounts",
    StringKeys.STATEMENTS to "Statements",
    StringKeys.GOALS to "Saving Goals",
    StringKeys.BILLS to "Bills & Dues",
    StringKeys.CHATBOT to "AI Financial Advisor",
    StringKeys.SETTINGS to "Settings",
    StringKeys.HOME to "Home",
    StringKeys.REPORTS to "Reports",
    StringKeys.ADD_ACCOUNT to "+ Add Account",
    StringKeys.ADD_TRANSACTION to "Add Transaction",
    StringKeys.ADD_INCOME to "Add Income",
    StringKeys.ADD_EXPENSE to "Add Spent",
    StringKeys.ADD_GOAL to "New Goal",
    StringKeys.ADD_BILL to "Add Bill",
    StringKeys.AI_TIPS to "AI Tips",
    StringKeys.SAVING_GOALS to "Saving Goals",
    StringKeys.BILLS_AND_DUES to "Bills & Dues",
    StringKeys.DEBTS to "Debts & Loans",
    StringKeys.DEBTS_SUBTITLE to "Track who owes you or who you owe",
    StringKeys.HABITS_SUBTITLE to "Set intervals and track recurring tasks and routines",
    StringKeys.HABITS to "Habits & Routine",
    StringKeys.LENT to "I Lent (To Receive)",
    StringKeys.BORROWED to "I Borrowed (To Pay)",
    StringKeys.TO_RECEIVE to "To Receive",
    StringKeys.TO_PAY to "To Pay",
    StringKeys.NET_STATUS to "Net Balance",
    StringKeys.OVERVIEW to "Overview",
    StringKeys.ACTIVE_RECORDS to "active records",
    StringKeys.PERSON_NAME to "Person Name",
    StringKeys.REPAID to "Repaid",
    StringKeys.SETTLED to "Settled",
    StringKeys.REPAY to "Record Payment",
    StringKeys.REPAY_TITLE to "Payment & Settlement",
    StringKeys.NO_DEBTS to "No debts recorded",
    StringKeys.NO_DEBTS_HINT to "Tap + below to add a new debt or receivable record.",
    StringKeys.HABITS_TITLE to "Habits & Routine Reminders",
    StringKeys.HABITS_DESC to "Track recurring routines, appointments, servicing or maintenance",
    StringKeys.INTERVAL to "Interval",
    StringKeys.ROUTINE_NAME to "Routine Name",
    StringKeys.LAST_DONE to "Last Completed",
    StringKeys.NEXT_DUE to "Next Due Date",
    StringKeys.DAYS_LEFT to "days remaining",
    StringKeys.DUE_TODAY to "Due Today!",
    StringKeys.OVERDUE to "Overdue",
    StringKeys.MARK_DONE to "Mark Done",
    StringKeys.MARK_DONE_CONFIRM to "Confirm Completion",
    StringKeys.NO_HABITS to "No recurring routines yet",
    StringKeys.NO_HABITS_HINT to "Tap + below to add a recurring routine or task.",
    StringKeys.ADD_HABIT to "Add Routine",
    StringKeys.ESTIMATED_COST to "Estimated Cost",
    StringKeys.BANK to "Bank",
    StringKeys.CASH to "Cash",
    StringKeys.BKASH to "bKash",
    StringKeys.NAGAD to "Nagad",
    StringKeys.ROCKET to "Rocket",
    StringKeys.INCOME to "Income",
    StringKeys.SPENT to "Spent",
    StringKeys.ALL to "All",
    StringKeys.RECENT_TRANSACTIONS to "Recent Statements",
    StringKeys.SEE_ALL to "See All",
    StringKeys.VIEW to "View >",
    StringKeys.NO_TRANSACTIONS to "No transactions yet",
    StringKeys.NO_ACCOUNTS to "No accounts yet. Tap + to add Bank, Card, Cash.",
    StringKeys.NO_GOALS to "No savings goals set. Create one for your target!",
    StringKeys.NO_BILLS to "No bills scheduled. Keep track of upcoming payments.",
    StringKeys.UPCOMING_BILL to "Upcoming Bill",
    StringKeys.AMOUNT to "Amount",
    StringKeys.TITLE to "Title",
    StringKeys.CATEGORY to "Category",
    StringKeys.ACCOUNT to "Account",
    StringKeys.DATE to "Date",
    StringKeys.NOTE to "Note (Optional)",
    StringKeys.SAVE to "Save",
    StringKeys.CANCEL to "Cancel",
    StringKeys.DELETE to "Delete",
    StringKeys.CONFIRM to "Confirm",
    StringKeys.DUE_DATE to "Due Date",
    StringKeys.NOTIFY to "Reminder Notification",
    StringKeys.PAID to "Paid",
    StringKeys.UNPAID to "Unpaid",
    StringKeys.MARK_AS_PAID to "Mark Paid",
    StringKeys.TARGET_AMOUNT to "Target Amount",
    StringKeys.SAVED_AMOUNT to "Saved Amount",
    StringKeys.REMAINING to "Remaining",
    StringKeys.ADD_FUNDS to "Add Money",
    StringKeys.WITHDRAW to "Withdraw",
    StringKeys.TAGLINE to "Your Smart Personal Finance Companion",
    StringKeys.OR to "OR",
    StringKeys.SIGNING_IN to "Signing in...",
    StringKeys.SIGN_IN_WITH_GOOGLE to "Sign in with Google",
    StringKeys.CONTINUE_GUEST to "Continue as Guest",
    StringKeys.SIGN_OUT to "Sign Out",
    StringKeys.THEME_DARK to "Dark Mode",
    StringKeys.THEME_LIGHT to "Lite Mode",
    StringKeys.THEME_SYSTEM to "System Default",
    StringKeys.LANGUAGE to "Language & Country",
    StringKeys.COUNTRY to "Country",
    StringKeys.SELECT_COUNTRY to "Select Country & Language",
    StringKeys.PROFILE to "Profile & Identity",
    StringKeys.CURRENCY_BDT to "Currency",
    StringKeys.FINANCIAL_UTILITIES to "Financial Utilities",
    StringKeys.CHAT_PLACEHOLDER to "Ask anything about your money, budget or bills...",
    StringKeys.QUICK_QUESTIONS to "Suggested Inquiries",
    StringKeys.SALARY to "Job Salary",
    StringKeys.PATHAO_DRIVE to "Drive / Transport",
    StringKeys.TIPS to "Tips",
    StringKeys.BUSINESS to "Business",
    StringKeys.FOOD to "Food",
    StringKeys.FAMILY to "Family",
    StringKeys.RENTAL_COST to "Rental cost",
    StringKeys.OTHERS to "Others",
    StringKeys.EMPTY_STATE_HINT to "Clean start! Add your own accounts and transactions."
)

// 2. Bangla Translations (bn)
private val bnStrings = mapOf(
    StringKeys.APP_NAME to "আমার ওয়ালেট",
    StringKeys.NET_BALANCE to "মোট ব্যালেন্স",
    StringKeys.TOTAL_INCOME to "মোট আয়",
    StringKeys.TOTAL_EXPENSE to "মোট ব্যয়",
    StringKeys.ACCOUNTS to "অ্যাকাউন্টস",
    StringKeys.STATEMENTS to "স্টেটমেন্ট",
    StringKeys.GOALS to "সঞ্চয় লক্ষ্য",
    StringKeys.BILLS to "বিল ও বকেয়া",
    StringKeys.CHATBOT to "এআই আর্থিক সহকারী",
    StringKeys.SETTINGS to "সেটিংস",
    StringKeys.HOME to "হোম",
    StringKeys.REPORTS to "রিপোর্ট",
    StringKeys.ADD_ACCOUNT to "+ অ্যাকাউন্ট যোগ",
    StringKeys.ADD_TRANSACTION to "লেনদেন যোগ",
    StringKeys.ADD_INCOME to "আয় যোগ",
    StringKeys.ADD_EXPENSE to "ব্যয় যোগ",
    StringKeys.ADD_GOAL to "নতুন লক্ষ্য",
    StringKeys.ADD_BILL to "বিল যোগ",
    StringKeys.AI_TIPS to "এআই টিপস",
    StringKeys.SAVING_GOALS to "সঞ্চয় লক্ষ্য",
    StringKeys.BILLS_AND_DUES to "বিল ও বকেয়া",
    StringKeys.DEBTS to "দেনা-পাওনা",
    StringKeys.DEBTS_SUBTITLE to "কারো কাছে টাকা পেলে বা দিলে হিসাব রাখুন",
    StringKeys.HABITS to "অভ্যাস ও রুটিন",
    StringKeys.HABITS_SUBTITLE to "নিয়মিত যেকোনো কাজের নির্দিষ্ট সময় ও খরচ হিসাব রাখুন",
    StringKeys.LENT to "আমি পাবো (Lent)",
    StringKeys.BORROWED to "দিতে হবে (Borrowed)",
    StringKeys.TO_RECEIVE to "আমি পাবো",
    StringKeys.TO_PAY to "দিতে হবে",
    StringKeys.NET_STATUS to "মোট স্থিতি",
    StringKeys.OVERVIEW to "সারসংক্ষেপ",
    StringKeys.ACTIVE_RECORDS to "টি চলমান হিসাব",
    StringKeys.PERSON_NAME to "ব্যক্তির নাম",
    StringKeys.REPAID to "পরিশোধিত",
    StringKeys.SETTLED to "পরিশোধ সম্পন্ন",
    StringKeys.REPAY to "পরিশোধ রেকর্ড",
    StringKeys.REPAY_TITLE to "পরিশোধ / জমার হিসাব",
    StringKeys.NO_DEBTS to "কোন দেনা-পাওনা রেকর্ড নেই",
    StringKeys.NO_DEBTS_HINT to "নতুন এন্ট্রি যোগ করতে নিচের '+' বাটনে চাপুন।",
    StringKeys.HABITS_TITLE to "অভ্যাস ও রুটিন রিমাইন্ডার",
    StringKeys.HABITS_DESC to "নিয়মিত কাজের সময়, পর্যায়ক্রম ও খরচ হিসাব রাখুন",
    StringKeys.INTERVAL to "কতদিন পর পর",
    StringKeys.ROUTINE_NAME to "রুটিনের নাম",
    StringKeys.LAST_DONE to "আগের বার করেছিলাম",
    StringKeys.NEXT_DUE to "পরের বার করতে হবে",
    StringKeys.DAYS_LEFT to "দিন বাকি",
    StringKeys.DUE_TODAY to "আজকেই সময়!",
    StringKeys.OVERDUE to "মেয়াদ শেষ",
    StringKeys.MARK_DONE to "সম্পন্ন করেছি (Mark Done)",
    StringKeys.MARK_DONE_CONFIRM to "সম্পন্ন নিশ্চিত করুন",
    StringKeys.NO_HABITS to "কোনো রুটিন যুক্ত নেই",
    StringKeys.NO_HABITS_HINT to "নতুন রুটিন বা কাজ যোগ করতে নিচের বাটনে চাপুন।",
    StringKeys.ADD_HABIT to "রুটিন যোগ করুন",
    StringKeys.ESTIMATED_COST to "আনুমানিক খরচ",
    StringKeys.BANK to "ব্যাংক",
    StringKeys.CASH to "ক্যাশ",
    StringKeys.BKASH to "বিকাশ",
    StringKeys.NAGAD to "নগদ",
    StringKeys.ROCKET to "রকেট",
    StringKeys.INCOME to "আয়",
    StringKeys.SPENT to "ব্যয়",
    StringKeys.ALL to "সবগুলো",
    StringKeys.RECENT_TRANSACTIONS to "সাম্প্রতিক স্টেটমেন্ট",
    StringKeys.SEE_ALL to "সব দেখুন",
    StringKeys.VIEW to "দেখুন >",
    StringKeys.NO_TRANSACTIONS to "এখনও কোনো লেনদেন রেকর্ড নেই",
    StringKeys.NO_ACCOUNTS to "কোনো অ্যাকাউন্ট নেই। ব্যাংক, বিকাশ বা ক্যাশ যোগ করুন।",
    StringKeys.NO_GOALS to "কোনো সঞ্চয় লক্ষ্য নেই। নতুন লক্ষ্য তৈরি করুন!",
    StringKeys.NO_BILLS to "কোনো বিল তালিকাভুক্ত নেই। বিলের রিমাইন্ডার সেট করুন।",
    StringKeys.UPCOMING_BILL to "আসন্ন বিল",
    StringKeys.AMOUNT to "টাকার পরিমাণ",
    StringKeys.TITLE to "বিবরণ বা নাম",
    StringKeys.CATEGORY to "ক্যাটেগরি",
    StringKeys.ACCOUNT to "অ্যাকাউন্ট",
    StringKeys.DATE to "তারিখ",
    StringKeys.NOTE to "নোট (ঐচ্ছিক)",
    StringKeys.SAVE to "সংরক্ষণ করুন",
    StringKeys.CANCEL to "বাতিল",
    StringKeys.DELETE to "মুছে ফেলুন",
    StringKeys.CONFIRM to "নিশ্চিত করুন",
    StringKeys.DUE_DATE to "পরিশোধের শেষ তারিখ",
    StringKeys.NOTIFY to "নোটিফিকেশন রিমাইন্ডার",
    StringKeys.PAID to "পরিশোধিত",
    StringKeys.UNPAID to "অপরিশোধিত",
    StringKeys.MARK_AS_PAID to "পরিশোধ সম্পন্ন",
    StringKeys.TARGET_AMOUNT to "লক্ষ্যমাত্রা",
    StringKeys.SAVED_AMOUNT to "জমা হয়েছে",
    StringKeys.REMAINING to "বাকি আছে",
    StringKeys.ADD_FUNDS to "টাকা জমা",
    StringKeys.WITHDRAW to "টাকা উত্তোলন",
    StringKeys.TAGLINE to "আপনার স্মার্ট পার্সোনাল ফাইন্যান্স সহযোগী",
    StringKeys.OR to "অথবা",
    StringKeys.SIGNING_IN to "লগইন হচ্ছে...",
    StringKeys.SIGN_IN_WITH_GOOGLE to "গুগল দিয়ে সাইন ইন",
    StringKeys.CONTINUE_GUEST to "গেস্ট হিসেবে চালিয়ে যান",
    StringKeys.SIGN_OUT to "সাইন আউট",
    StringKeys.THEME_DARK to "ডার্ক মোড",
    StringKeys.THEME_LIGHT to "লাইট মোড",
    StringKeys.THEME_SYSTEM to "সিস্টেম ডিফল্ট",
    StringKeys.LANGUAGE to "ভাষা ও দেশ",
    StringKeys.COUNTRY to "দেশ",
    StringKeys.SELECT_COUNTRY to "দেশ ও ভাষা নির্বাচন করুন",
    StringKeys.PROFILE to "প্রোফাইল ও পরিচয়",
    StringKeys.CURRENCY_BDT to "মুদ্রা",
    StringKeys.FINANCIAL_UTILITIES to "আর্থিক সুবিধাসমূহ",
    StringKeys.CHAT_PLACEHOLDER to "টাকা-পয়সা, বাজেট বা বিল সম্পর্কে জিজ্ঞাসা করুন...",
    StringKeys.QUICK_QUESTIONS to "প্রস্তাবিত প্রশ্নাবলী",
    StringKeys.SALARY to "বেতন",
    StringKeys.PATHAO_DRIVE to "ড্রাইভ / পরিবহন",
    StringKeys.TIPS to "টিপস",
    StringKeys.BUSINESS to "ব্যবসা",
    StringKeys.FOOD to "খাবার",
    StringKeys.FAMILY to "পরিবার",
    StringKeys.RENTAL_COST to "ভাড়া",
    StringKeys.OTHERS to "অন্যান্য",
    StringKeys.EMPTY_STATE_HINT to "সম্পূর্ণ পরিষ্কার! আপনার নিজস্ব অ্যাকাউন্ট ও হিসাব যোগ করুন।"
)

// 3. Arabic Translations (ar)
private val arStrings = mapOf(
    StringKeys.APP_NAME to "محفظتي",
    StringKeys.NET_BALANCE to "صافي الرصيد",
    StringKeys.TOTAL_INCOME to "إجمالي الدخل",
    StringKeys.TOTAL_EXPENSE to "إجمالي المصروفات",
    StringKeys.ACCOUNTS to "الحسابات",
    StringKeys.STATEMENTS to "الكشوفات",
    StringKeys.GOALS to "أهداف الادخار",
    StringKeys.BILLS to "الفواتير والمستحقات",
    StringKeys.CHATBOT to "المستشار المالي الذكي",
    StringKeys.SETTINGS to "الإعدادات",
    StringKeys.HOME to "الرئيسية",
    StringKeys.REPORTS to "التقارير",
    StringKeys.ADD_ACCOUNT to "+ إضافة حساب",
    StringKeys.ADD_TRANSACTION to "إضافة معاملة",
    StringKeys.ADD_INCOME to "إضافة دخل",
    StringKeys.ADD_EXPENSE to "إضافة مصروف",
    StringKeys.ADD_GOAL to "هدف جديد",
    StringKeys.ADD_BILL to "إضافة فاتورة",
    StringKeys.AI_TIPS to "نصائح ذكية",
    StringKeys.SAVING_GOALS to "أهداف الادخار",
    StringKeys.BILLS_AND_DUES to "الفواتير والمستحقات",
    StringKeys.DEBTS to "الديون والقروض",
    StringKeys.DEBTS_SUBTITLE to "تتبع ما لك وما عليك من أموال",
    StringKeys.HABITS to "العادات والروتين",
    StringKeys.HABITS_SUBTITLE to "جدولة قص الشعر والصيانة والمصاريف الدورية",
    StringKeys.LENT to "أنا أقرضت (مستحق لي)",
    StringKeys.BORROWED to "أنا اقترضت (مستحق علي)",
    StringKeys.TO_RECEIVE to "مستحق لي",
    StringKeys.TO_PAY to "مستحق علي",
    StringKeys.NET_STATUS to "صافي الحالة",
    StringKeys.OVERVIEW to "نظرة عامة",
    StringKeys.ACTIVE_RECORDS to "سجلات نشطة",
    StringKeys.PERSON_NAME to "اسم الشخص",
    StringKeys.REPAID to "تم السداد",
    StringKeys.SETTLED to "مسدد بالكامل",
    StringKeys.REPAY to "تسجيل سداد",
    StringKeys.REPAY_TITLE to "تسجيل دفعة أو سداد",
    StringKeys.NO_DEBTS to "لا توجد ديون مسجلة",
    StringKeys.NO_DEBTS_HINT to "اضغط على + لإضافة سجل دين جديد.",
    StringKeys.HABITS_TITLE to "تذكير العادات والروتين",
    StringKeys.HABITS_DESC to "تتبع قص الشعر والصيانة والمواعيد الدورية",
    StringKeys.INTERVAL to "الفترة الزمنية",
    StringKeys.ROUTINE_NAME to "اسم الروتين",
    StringKeys.LAST_DONE to "آخر مرة أنجزتها",
    StringKeys.NEXT_DUE to "الموعد القادم",
    StringKeys.DAYS_LEFT to "يوم متبقي",
    StringKeys.DUE_TODAY to "الموعد اليوم!",
    StringKeys.OVERDUE to "متأخر",
    StringKeys.MARK_DONE to "تم الإنجاز (Mark Done)",
    StringKeys.MARK_DONE_CONFIRM to "تأكيد الإنجاز",
    StringKeys.NO_HABITS to "لا توجد روتينات مسجلة",
    StringKeys.NO_HABITS_HINT to "اضغط على + لجدولة روتين دوري.",
    StringKeys.ADD_HABIT to "إضافة روتين",
    StringKeys.ESTIMATED_COST to "التكلفة التقديرية",
    StringKeys.BANK to "بنك",
    StringKeys.CASH to "نقدي",
    StringKeys.BKASH to "محفظة رقمية",
    StringKeys.NAGAD to "محفظة نقدية",
    StringKeys.ROCKET to "حساب إلكتروني",
    StringKeys.INCOME to "دخل",
    StringKeys.SPENT to "مصروف",
    StringKeys.ALL to "الكل",
    StringKeys.RECENT_TRANSACTIONS to "أحدث المعاملات",
    StringKeys.SEE_ALL to "عرض الكل",
    StringKeys.VIEW to "عرض >",
    StringKeys.NO_TRANSACTIONS to "لا توجد معاملات بعد",
    StringKeys.NO_ACCOUNTS to "لا توجد حسابات. اضغط على + لإضافة حساب.",
    StringKeys.NO_GOALS to "لا توجد أهداف ادخار. ابدأ بإنشاء هدفك الآن!",
    StringKeys.NO_BILLS to "لا توجد فواتير مجدولة.",
    StringKeys.UPCOMING_BILL to "الفاتورة القادمة",
    StringKeys.AMOUNT to "المبلغ",
    StringKeys.TITLE to "العنوان",
    StringKeys.CATEGORY to "الفئة",
    StringKeys.ACCOUNT to "الحساب",
    StringKeys.DATE to "التاريخ",
    StringKeys.NOTE to "ملاحظة (اختياري)",
    StringKeys.SAVE to "حفظ",
    StringKeys.CANCEL to "إلغاء",
    StringKeys.DELETE to "حذف",
    StringKeys.CONFIRM to "تأكيد",
    StringKeys.DUE_DATE to "تاريخ الاستحقاق",
    StringKeys.NOTIFY to "تنبيه التذكير",
    StringKeys.PAID to "مدفوع",
    StringKeys.UNPAID to "غير مدفوع",
    StringKeys.MARK_AS_PAID to "تحديد كمدفوع",
    StringKeys.TARGET_AMOUNT to "المبلغ المستهدف",
    StringKeys.SAVED_AMOUNT to "المبلغ المدخر",
    StringKeys.REMAINING to "المتبقي",
    StringKeys.ADD_FUNDS to "إضافة أموال",
    StringKeys.WITHDRAW to "سحب أموال",
    StringKeys.TAGLINE to "رفيقك الذكي في إدارة أموالك الشخصية",
    StringKeys.OR to "أو",
    StringKeys.SIGNING_IN to "جارٍ تسجيل الدخول...",
    StringKeys.SIGN_IN_WITH_GOOGLE to "تسجيل الدخول عبر Google",
    StringKeys.CONTINUE_GUEST to "المتابعة كضيف",
    StringKeys.SIGN_OUT to "تسجيل الخروج",
    StringKeys.THEME_DARK to "الوضع الداكن",
    StringKeys.THEME_LIGHT to "الوضع الفاتح",
    StringKeys.THEME_SYSTEM to "افتراضي النظام",
    StringKeys.LANGUAGE to "اللغة والبلد",
    StringKeys.COUNTRY to "البلد",
    StringKeys.SELECT_COUNTRY to "اختر الدولة واللغة",
    StringKeys.PROFILE to "الملف الشخصي",
    StringKeys.CURRENCY_BDT to "العملة",
    StringKeys.FINANCIAL_UTILITIES to "الأدوات المالية",
    StringKeys.CHAT_PLACEHOLDER to "اسأل المستشار الذكي عن ميزانيتك ومصاريفك...",
    StringKeys.QUICK_QUESTIONS to "استفسارات مقترحة",
    StringKeys.SALARY to "الراتب",
    StringKeys.PATHAO_DRIVE to "المواصلات",
    StringKeys.TIPS to "إكراميات",
    StringKeys.BUSINESS to "أعمال",
    StringKeys.FOOD to "طعام",
    StringKeys.FAMILY to "عائلة",
    StringKeys.RENTAL_COST to "إيجار",
    StringKeys.OTHERS to "أخرى",
    StringKeys.EMPTY_STATE_HINT to "بداية نظيفة! ابدأ بإضافة حساباتك ومعاملاتك الخاصة."
)

// 4. German Translations (de)
private val deStrings = mapOf(
    StringKeys.APP_NAME to "Meine Geldbörse",
    StringKeys.NET_BALANCE to "Nettosaldo",
    StringKeys.TOTAL_INCOME to "Gesamteinnahmen",
    StringKeys.TOTAL_EXPENSE to "Gesamtausgaben",
    StringKeys.ACCOUNTS to "Konten",
    StringKeys.STATEMENTS to "Kontoauszüge",
    StringKeys.GOALS to "Sparziele",
    StringKeys.BILLS to "Rechnungen & Fälligkeiten",
    StringKeys.CHATBOT to "KI-Finanzberater",
    StringKeys.SETTINGS to "Einstellungen",
    StringKeys.HOME to "Übersicht",
    StringKeys.REPORTS to "Berichte",
    StringKeys.ADD_ACCOUNT to "+ Konto hinzufügen",
    StringKeys.ADD_TRANSACTION to "Transaktion erfassen",
    StringKeys.ADD_INCOME to "Einnahme buchen",
    StringKeys.ADD_EXPENSE to "Ausgabe buchen",
    StringKeys.ADD_GOAL to "Neues Sparziel",
    StringKeys.ADD_BILL to "Rechnung anlegen",
    StringKeys.AI_TIPS to "KI-Tipps",
    StringKeys.SAVING_GOALS to "Sparziele",
    StringKeys.BILLS_AND_DUES to "Rechnungen & Fälligkeiten",
    StringKeys.DEBTS to "Schulden & Forderungen",
    StringKeys.DEBTS_SUBTITLE to "Verwalte geliehenes und verliehenes Geld",
    StringKeys.HABITS to "Gewohnheiten & Routinen",
    StringKeys.HABITS_SUBTITLE to "Intervalle für Haarschnitt, Service & wiederkehrende Aufgaben",
    StringKeys.LENT to "Ich erhalte (Forderung)",
    StringKeys.BORROWED to "Ich schulde (Verbindlichkeit)",
    StringKeys.TO_RECEIVE to "Zu erhalten",
    StringKeys.TO_PAY to "Zu zahlen",
    StringKeys.NET_STATUS to "Nettostatus",
    StringKeys.OVERVIEW to "Übersicht",
    StringKeys.ACTIVE_RECORDS to "aktive Einträge",
    StringKeys.PERSON_NAME to "Name der Person",
    StringKeys.REPAID to "Abbezahlt",
    StringKeys.SETTLED to "Beglichen",
    StringKeys.REPAY to "Zahlung verbuchen",
    StringKeys.REPAY_TITLE to "Zahlung & Begleichung",
    StringKeys.NO_DEBTS to "Keine Schulden oder Forderungen",
    StringKeys.NO_DEBTS_HINT to "Tippe unten auf +, um einen Eintrag hinzuzufügen.",
    StringKeys.HABITS_TITLE to "Routinen & Erinnerungen",
    StringKeys.HABITS_DESC to "Haarschnitt, Wartung & feste Intervalle im Blick behalten",
    StringKeys.INTERVAL to "Intervall",
    StringKeys.ROUTINE_NAME to "Name der Routine",
    StringKeys.LAST_DONE to "Zuletzt erledigt",
    StringKeys.NEXT_DUE to "Nächste Fälligkeit",
    StringKeys.DAYS_LEFT to "Tage verbleibend",
    StringKeys.DUE_TODAY to "Heute fällig!",
    StringKeys.OVERDUE to "Überfällig",
    StringKeys.MARK_DONE to "Erledigt (Mark Done)",
    StringKeys.MARK_DONE_CONFIRM to "Erledigung bestätigen",
    StringKeys.NO_HABITS to "Noch keine Routinen hinterlegt",
    StringKeys.NO_HABITS_HINT to "Erstelle mit + eine wiederkehrende Routine.",
    StringKeys.ADD_HABIT to "Routine anlegen",
    StringKeys.ESTIMATED_COST to "Geschätzte Kosten",
    StringKeys.BANK to "Bank",
    StringKeys.CASH to "Bargeld",
    StringKeys.BKASH to "Online-Konto",
    StringKeys.NAGAD to "Kreditkarte",
    StringKeys.ROCKET to "Sparkonto",
    StringKeys.INCOME to "Einnahme",
    StringKeys.SPENT to "Ausgabe",
    StringKeys.ALL to "Alle",
    StringKeys.RECENT_TRANSACTIONS to "Letzte Buchungen",
    StringKeys.SEE_ALL to "Alle anzeigen",
    StringKeys.VIEW to "Details >",
    StringKeys.NO_TRANSACTIONS to "Noch keine Buchungen erfasst",
    StringKeys.NO_ACCOUNTS to "Noch keine Konten angelegt.",
    StringKeys.NO_GOALS to "Keine Sparziele definiert.",
    StringKeys.NO_BILLS to "Keine anstehenden Rechnungen.",
    StringKeys.UPCOMING_BILL to "Nächste Rechnung",
    StringKeys.AMOUNT to "Betrag",
    StringKeys.TITLE to "Titel",
    StringKeys.CATEGORY to "Kategorie",
    StringKeys.ACCOUNT to "Konto",
    StringKeys.DATE to "Datum",
    StringKeys.NOTE to "Notiz (optional)",
    StringKeys.SAVE to "Speichern",
    StringKeys.CANCEL to "Abbrechen",
    StringKeys.DELETE to "Löschen",
    StringKeys.CONFIRM to "Bestätigen",
    StringKeys.DUE_DATE to "Fälligkeitsdatum",
    StringKeys.NOTIFY to "Benachrichtigung",
    StringKeys.PAID to "Bezahlt",
    StringKeys.UNPAID to "Offen",
    StringKeys.MARK_AS_PAID to "Als bezahlt markieren",
    StringKeys.TARGET_AMOUNT to "Zielbetrag",
    StringKeys.SAVED_AMOUNT to "Bereits gespart",
    StringKeys.REMAINING to "Verbleibend",
    StringKeys.ADD_FUNDS to "Guthaben einzahlen",
    StringKeys.WITHDRAW to "Guthaben entnehmen",
    StringKeys.TAGLINE to "Ihr intelligenter persönlicher Finanzbegleiter",
    StringKeys.OR to "ODER",
    StringKeys.SIGNING_IN to "Anmeldung läuft...",
    StringKeys.SIGN_IN_WITH_GOOGLE to "Mit Google anmelden",
    StringKeys.CONTINUE_GUEST to "Als Gast fortfahren",
    StringKeys.SIGN_OUT to "Abmelden",
    StringKeys.THEME_DARK to "Dunkelmodus",
    StringKeys.THEME_LIGHT to "Hellmodus",
    StringKeys.THEME_SYSTEM to "Systemstandard",
    StringKeys.LANGUAGE to "Sprache & Land",
    StringKeys.COUNTRY to "Land",
    StringKeys.SELECT_COUNTRY to "Land & Sprache wählen",
    StringKeys.PROFILE to "Profil & Identität",
    StringKeys.CURRENCY_BDT to "Währung",
    StringKeys.FINANCIAL_UTILITIES to "Finanz-Werkzeuge",
    StringKeys.CHAT_PLACEHOLDER to "Frage den KI-Berater nach Budgets oder Ausgaben...",
    StringKeys.QUICK_QUESTIONS to "Empfohlene Fragen",
    StringKeys.SALARY to "Gehalt",
    StringKeys.PATHAO_DRIVE to "Fahrten",
    StringKeys.TIPS to "Trinkgeld",
    StringKeys.BUSINESS to "Geschäftlich",
    StringKeys.FOOD to "Essen & Trinken",
    StringKeys.FAMILY to "Familie",
    StringKeys.RENTAL_COST to "Miete",
    StringKeys.OTHERS to "Sonstiges",
    StringKeys.EMPTY_STATE_HINT to "Sauberer Start! Trage eigene Konten und Buchungen ein."
)

// 5. French Translations (fr)
private val frStrings = mapOf(
    StringKeys.APP_NAME to "Mon Portefeuille",
    StringKeys.NET_BALANCE to "Solde Net",
    StringKeys.TOTAL_INCOME to "Revenu Total",
    StringKeys.TOTAL_EXPENSE to "Dépenses Totales",
    StringKeys.ACCOUNTS to "Comptes",
    StringKeys.STATEMENTS to "Relevés",
    StringKeys.GOALS to "Objectifs d'Épargne",
    StringKeys.BILLS to "Factures & Échéances",
    StringKeys.CHATBOT to "Conseiller Financier IA",
    StringKeys.SETTINGS to "Paramètres",
    StringKeys.HOME to "Accueil",
    StringKeys.REPORTS to "Rapports",
    StringKeys.ADD_ACCOUNT to "+ Ajouter un Compte",
    StringKeys.ADD_TRANSACTION to "Ajouter une Transaction",
    StringKeys.ADD_INCOME to "Ajouter un Revenu",
    StringKeys.ADD_EXPENSE to "Ajouter une Dépense",
    StringKeys.ADD_GOAL to "Nouvel Objectif",
    StringKeys.ADD_BILL to "Ajouter une Facture",
    StringKeys.AI_TIPS to "Conseils IA",
    StringKeys.SAVING_GOALS to "Objectifs d'Épargne",
    StringKeys.BILLS_AND_DUES to "Factures & Échéances",
    StringKeys.DEBTS to "Dettes & Créances",
    StringKeys.DEBTS_SUBTITLE to "Suivez l'argent prêté et emprunté",
    StringKeys.HABITS to "Habitudes & Routines",
    StringKeys.HABITS_SUBTITLE to "Intervalles pour coupe de cheveux, entretien et loyer",
    StringKeys.LENT to "J'ai prêté (À recevoir)",
    StringKeys.BORROWED to "J'ai emprunté (À payer)",
    StringKeys.TO_RECEIVE to "À recevoir",
    StringKeys.TO_PAY to "À payer",
    StringKeys.NET_STATUS to "Statut Net",
    StringKeys.OVERVIEW to "Vue d'ensemble",
    StringKeys.ACTIVE_RECORDS to "enregistrements actifs",
    StringKeys.PERSON_NAME to "Nom de la personne",
    StringKeys.REPAID to "Remboursé",
    StringKeys.SETTLED to "Soldé",
    StringKeys.REPAY to "Enregistrer un paiement",
    StringKeys.REPAY_TITLE to "Remboursement & Règlement",
    StringKeys.NO_DEBTS to "Aucune dette enregistrée",
    StringKeys.NO_DEBTS_HINT to "Appuyez sur + pour ajouter une nouvelle dette ou créance.",
    StringKeys.HABITS_TITLE to "Rappels d'Habitudes & Routines",
    StringKeys.HABITS_DESC to "Suivez les événements réguliers comme la coiffure et le loyer",
    StringKeys.INTERVAL to "Intervalle",
    StringKeys.ROUTINE_NAME to "Nom de la routine",
    StringKeys.LAST_DONE to "Dernière réalisation",
    StringKeys.NEXT_DUE to "Prochaine échéance",
    StringKeys.DAYS_LEFT to "jours restants",
    StringKeys.DUE_TODAY to "Échéance aujourd'hui !",
    StringKeys.OVERDUE to "En retard",
    StringKeys.MARK_DONE to "Marquer comme fait (Mark Done)",
    StringKeys.MARK_DONE_CONFIRM to "Confirmer la réalisation",
    StringKeys.NO_HABITS to "Aucune routine enregistrée",
    StringKeys.NO_HABITS_HINT to "Appuyez sur + pour ajouter une routine périodique.",
    StringKeys.ADD_HABIT to "Ajouter une routine",
    StringKeys.ESTIMATED_COST to "Coût estimé",
    StringKeys.BANK to "Banque",
    StringKeys.CASH to "Espèces",
    StringKeys.BKASH to "Portefeuille Mobile",
    StringKeys.NAGAD to "Carte",
    StringKeys.ROCKET to "Compte Épargne",
    StringKeys.INCOME to "Revenu",
    StringKeys.SPENT to "Dépense",
    StringKeys.ALL to "Tous",
    StringKeys.RECENT_TRANSACTIONS to "Relevés Récents",
    StringKeys.SEE_ALL to "Voir tout",
    StringKeys.VIEW to "Voir >",
    StringKeys.NO_TRANSACTIONS to "Aucune transaction pour le moment",
    StringKeys.NO_ACCOUNTS to "Aucun compte. Appuyez sur + pour en créer un.",
    StringKeys.NO_GOALS to "Aucun objectif défini.",
    StringKeys.NO_BILLS to "Aucune facture programmée.",
    StringKeys.UPCOMING_BILL to "Prochaine facture",
    StringKeys.AMOUNT to "Montant",
    StringKeys.TITLE to "Titre",
    StringKeys.CATEGORY to "Catégorie",
    StringKeys.ACCOUNT to "Compte",
    StringKeys.DATE to "Date",
    StringKeys.NOTE to "Note (Facultatif)",
    StringKeys.SAVE to "Enregistrer",
    StringKeys.CANCEL to "Annuler",
    StringKeys.DELETE to "Supprimer",
    StringKeys.CONFIRM to "Confirmer",
    StringKeys.DUE_DATE to "Date d'échéance",
    StringKeys.NOTIFY to "Notification de rappel",
    StringKeys.PAID to "Payé",
    StringKeys.UNPAID to "Impayé",
    StringKeys.MARK_AS_PAID to "Marquer comme payé",
    StringKeys.TARGET_AMOUNT to "Montant cible",
    StringKeys.SAVED_AMOUNT to "Montant épargné",
    StringKeys.REMAINING to "Restant",
    StringKeys.ADD_FUNDS to "Ajouter des fonds",
    StringKeys.WITHDRAW to "Retirer",
    StringKeys.TAGLINE to "Votre compagnon intelligent de finances personnelles",
    StringKeys.OR to "OU",
    StringKeys.SIGNING_IN to "Connexion en cours...",
    StringKeys.SIGN_IN_WITH_GOOGLE to "Se connecter avec Google",
    StringKeys.CONTINUE_GUEST to "Continuer en tant qu'invité",
    StringKeys.SIGN_OUT to "Se déconnecter",
    StringKeys.THEME_DARK to "Mode Sombre",
    StringKeys.THEME_LIGHT to "Mode Clair",
    StringKeys.THEME_SYSTEM to "Système par défaut",
    StringKeys.LANGUAGE to "Langue & Pays",
    StringKeys.COUNTRY to "Pays",
    StringKeys.SELECT_COUNTRY to "Sélectionnez le Pays & la Langue",
    StringKeys.PROFILE to "Profil & Identité",
    StringKeys.CURRENCY_BDT to "Devise",
    StringKeys.FINANCIAL_UTILITIES to "Outils Financiers",
    StringKeys.CHAT_PLACEHOLDER to "Posez une question sur votre budget, vos dépenses...",
    StringKeys.QUICK_QUESTIONS to "Suggestions de questions",
    StringKeys.SALARY to "Salaire",
    StringKeys.PATHAO_DRIVE to "Transport",
    StringKeys.TIPS to "Pourboires",
    StringKeys.BUSINESS to "Affaires",
    StringKeys.FOOD to "Nourriture",
    StringKeys.FAMILY to "Famille",
    StringKeys.RENTAL_COST to "Loyer",
    StringKeys.OTHERS to "Autres",
    StringKeys.EMPTY_STATE_HINT to "Nouveau départ ! Ajoutez vos propres comptes et dépenses."
)

// 6. Dutch Translations (nl)
private val nlStrings = mapOf(
    StringKeys.APP_NAME to "Mijn Portemonnee",
    StringKeys.NET_BALANCE to "Netto Saldo",
    StringKeys.TOTAL_INCOME to "Totaal Inkomen",
    StringKeys.TOTAL_EXPENSE to "Totale Uitgaven",
    StringKeys.ACCOUNTS to "Rekeningen",
    StringKeys.STATEMENTS to "Overzichten",
    StringKeys.GOALS to "Spardoelen",
    StringKeys.BILLS to "Rekeningen & Facturen",
    StringKeys.CHATBOT to "AI Financieel Adviseur",
    StringKeys.SETTINGS to "Instellingen",
    StringKeys.HOME to "Home",
    StringKeys.REPORTS to "Rapporten",
    StringKeys.ADD_ACCOUNT to "+ Rekening Toevoegen",
    StringKeys.ADD_TRANSACTION to "Transactie Toevoegen",
    StringKeys.ADD_INCOME to "Inkomsten Boeken",
    StringKeys.ADD_EXPENSE to "Uitgaven Boeken",
    StringKeys.ADD_GOAL to "Nieuw Doel",
    StringKeys.ADD_BILL to "Rekening Toevoegen",
    StringKeys.AI_TIPS to "AI Tips",
    StringKeys.SAVING_GOALS to "Spardoelen",
    StringKeys.BILLS_AND_DUES to "Rekeningen & Facturen",
    StringKeys.DEBTS to "Schulden & Vorderingen",
    StringKeys.DEBTS_SUBTITLE to "Houd bij wie jou geld schuldig is en wie jij geld verschuldigd bent",
    StringKeys.HABITS to "Gewoontes & Routine",
    StringKeys.HABITS_SUBTITLE to "Stel intervallen in voor kapper, onderhoud en vaste kosten",
    StringKeys.LENT to "Ik heb uitgeleend (Te ontvangen)",
    StringKeys.BORROWED to "Ik heb geleend (Te betalen)",
    StringKeys.TO_RECEIVE to "Te ontvangen",
    StringKeys.TO_PAY to "Te betalen",
    StringKeys.NET_STATUS to "Netto Status",
    StringKeys.OVERVIEW to "Overzicht",
    StringKeys.ACTIVE_RECORDS to "actieve registraties",
    StringKeys.PERSON_NAME to "Naam persoon",
    StringKeys.REPAID to "Afgelost",
    StringKeys.SETTLED to "Voldaan",
    StringKeys.REPAY to "Betaling registreren",
    StringKeys.REPAY_TITLE to "Aflossing & Afhandeling",
    StringKeys.NO_DEBTS to "Geen schulden of vorderingen",
    StringKeys.NO_DEBTS_HINT to "Tik op + hieronder om een nieuwe post toe te voegen.",
    StringKeys.HABITS_TITLE to "Gewoontes & Routine Herinneringen",
    StringKeys.HABITS_DESC to "Volg herhalende zaken zoals kapper, beurt en huur",
    StringKeys.INTERVAL to "Interval",
    StringKeys.ROUTINE_NAME to "Naam routine",
    StringKeys.LAST_DONE to "Laatst uitgevoerd",
    StringKeys.NEXT_DUE to "Volgende datum",
    StringKeys.DAYS_LEFT to "dagen resterend",
    StringKeys.DUE_TODAY to "Vandaag gepland!",
    StringKeys.OVERDUE to "Achterstallig",
    StringKeys.MARK_DONE to "Afgerond (Mark Done)",
    StringKeys.MARK_DONE_CONFIRM to "Afronding bevestigen",
    StringKeys.NO_HABITS to "Nog geen routines ingesteld",
    StringKeys.NO_HABITS_HINT to "Tik op + om een periodieke routine aan te maken.",
    StringKeys.ADD_HABIT to "Routine toevoegen",
    StringKeys.ESTIMATED_COST to "Geschatte kosten",
    StringKeys.BANK to "Bank",
    StringKeys.CASH to "Contant",
    StringKeys.BKASH to "Mobiel Bankieren",
    StringKeys.NAGAD to "Kaart",
    StringKeys.ROCKET to "Spaarrekening",
    StringKeys.INCOME to "Inkomsten",
    StringKeys.SPENT to "Uitgaven",
    StringKeys.ALL to "Alles",
    StringKeys.RECENT_TRANSACTIONS to "Recente Overzichten",
    StringKeys.SEE_ALL to "Bekijk alles",
    StringKeys.VIEW to "Bekijk >",
    StringKeys.NO_TRANSACTIONS to "Nog geen transacties geregistreerd",
    StringKeys.NO_ACCOUNTS to "Geen rekeningen. Tik op + om er een toe te voegen.",
    StringKeys.NO_GOALS to "Geen spardoelen ingesteld.",
    StringKeys.NO_BILLS to "Geen rekeningen ingepland.",
    StringKeys.UPCOMING_BILL to "Volgende rekening",
    StringKeys.AMOUNT to "Bedrag",
    StringKeys.TITLE to "Titel",
    StringKeys.CATEGORY to "Categorie",
    StringKeys.ACCOUNT to "Rekening",
    StringKeys.DATE to "Datum",
    StringKeys.NOTE to "Notitie (optioneel)",
    StringKeys.SAVE to "Opslaan",
    StringKeys.CANCEL to "Annuleren",
    StringKeys.DELETE to "Verwijderen",
    StringKeys.CONFIRM to "Bevestigen",
    StringKeys.DUE_DATE to "Vervaldatum",
    StringKeys.NOTIFY to "Herinneringsmelding",
    StringKeys.PAID to "Betaald",
    StringKeys.UNPAID to "Onbetaald",
    StringKeys.MARK_AS_PAID to "Als betaald markeren",
    StringKeys.TARGET_AMOUNT to "Doelbedrag",
    StringKeys.SAVED_AMOUNT to "Gespaard bedrag",
    StringKeys.REMAINING to "Resterend",
    StringKeys.ADD_FUNDS to "Geld toevoegen",
    StringKeys.WITHDRAW to "Opnemen",
    StringKeys.TAGLINE to "Uw slimme persoonlijke financiële metgezel",
    StringKeys.OR to "OF",
    StringKeys.SIGNING_IN to "Inloggen...",
    StringKeys.SIGN_IN_WITH_GOOGLE to "Inloggen met Google",
    StringKeys.CONTINUE_GUEST to "Doorgaan als gast",
    StringKeys.SIGN_OUT to "Uitloggen",
    StringKeys.THEME_DARK to "Donkere Modus",
    StringKeys.THEME_LIGHT to "Lichte Modus",
    StringKeys.THEME_SYSTEM to "Systeemstandaard",
    StringKeys.LANGUAGE to "Taal & Land",
    StringKeys.COUNTRY to "Land",
    StringKeys.SELECT_COUNTRY to "Selecteer Land & Taal",
    StringKeys.PROFILE to "Profiel & Identiteit",
    StringKeys.CURRENCY_BDT to "Valuta",
    StringKeys.FINANCIAL_UTILITIES to "Financiële Hulpmiddelen",
    StringKeys.CHAT_PLACEHOLDER to "Vraag de AI adviseur over budgetten en uitgaven...",
    StringKeys.QUICK_QUESTIONS to "Voorgestelde vragen",
    StringKeys.SALARY to "Salaris",
    StringKeys.PATHAO_DRIVE to "Vervoer",
    StringKeys.TIPS to "Fooi",
    StringKeys.BUSINESS to "Zakelijk",
    StringKeys.FOOD to "Eten",
    StringKeys.FAMILY to "Familie",
    StringKeys.RENTAL_COST to "Huur",
    StringKeys.OTHERS to "Overig",
    StringKeys.EMPTY_STATE_HINT to "Schone start! Voeg je eigen rekeningen en transacties toe."
)

fun getString(key: String, language: AppLanguage): String {
    return when (language.langFamily) {
        "bn" -> bnStrings[key] ?: enStrings[key] ?: key
        "ar" -> arStrings[key] ?: enStrings[key] ?: key
        "de" -> deStrings[key] ?: enStrings[key] ?: key
        "fr" -> frStrings[key] ?: enStrings[key] ?: key
        "nl" -> nlStrings[key] ?: enStrings[key] ?: key
        else -> enStrings[key] ?: key
    }
}

@Composable
fun appString(key: String): String {
    val lang = LocalAppLanguage.current
    return getString(key, lang)
}

/**
 * Universal formatted currency amount based on the selected country and language.
 * Uses the country's native currency symbol (e.g. ৳, $, £, د.إ, ﷼, €, CHF).
 */
fun formatCurrency(amount: Double, language: AppLanguage = AppLanguage.BANGLADESH): String {
    val formatted = NumberFormat.getNumberInstance(Locale.US).format(amount)
    return when (language.langFamily) {
        "ar" -> "$formatted ${language.currencySymbol}"
        "de", "fr", "nl" -> "$formatted ${language.currencySymbol}"
        else -> "${language.currencySymbol} $formatted"
    }
}

fun formatCurrency(amount: Double, currency: AppCurrency): String {
    return currency.format(amount)
}

/**
 * Backward-compatible helper method that formats amounts in BDT currency (৳).
 */
fun formatBdt(amount: Double, language: AppLanguage = AppLanguage.BANGLADESH): String {
    val formatted = NumberFormat.getNumberInstance(Locale.US).format(amount)
    return "৳ $formatted"
}

fun formatBdt(amount: Double, currency: AppCurrency): String {
    return currency.format(amount)
}
