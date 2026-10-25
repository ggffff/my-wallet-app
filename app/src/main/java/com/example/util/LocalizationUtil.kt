package com.example.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import java.text.NumberFormat
import java.util.Locale

enum class AppLanguage(val code: String, val label: String) {
    ENGLISH("en", "English"),
    BANGLA("bn", "বাংলা")
}

val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.ENGLISH }

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
    const val ADD_ACCOUNT = "add_account"
    const val ADD_TRANSACTION = "add_transaction"
    const val ADD_INCOME = "add_income"
    const val ADD_EXPENSE = "add_expense"
    const val ADD_GOAL = "add_goal"
    const val ADD_BILL = "add_bill"
    const val BANK = "bank"
    const val CASH = "cash"
    const val BKASH = "bkash"
    const val NAGAD = "nagad"
    const val ROCKET = "rocket"
    const val INCOME = "income"
    const val SPENT = "spent"
    const val ALL = "all"
    const val RECENT_TRANSACTIONS = "recent_transactions"
    const val NO_TRANSACTIONS = "no_transactions"
    const val NO_ACCOUNTS = "no_accounts"
    const val NO_GOALS = "no_goals"
    const val NO_BILLS = "no_bills"
    const val AMOUNT = "amount"
    const val TITLE = "title"
    const val CATEGORY = "category"
    const val ACCOUNT = "account"
    const val DATE = "date"
    const val NOTE = "note"
    const val SAVE = "save"
    const val CANCEL = "cancel"
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
    const val SIGN_IN_WITH_GOOGLE = "sign_in_with_google"
    const val SIGN_OUT = "sign_out"
    const val THEME_DARK = "theme_dark"
    const val THEME_LIGHT = "theme_light"
    const val THEME_SYSTEM = "theme_system"
    const val LANGUAGE = "language"
    const val PROFILE = "profile"
    const val CURRENCY_BDT = "currency_bdt"
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

private val enStrings = mapOf(
    StringKeys.APP_NAME to "My Wallet",
    StringKeys.NET_BALANCE to "Net Balance",
    StringKeys.TOTAL_INCOME to "Total Income",
    StringKeys.TOTAL_EXPENSE to "Total Spent",
    StringKeys.ACCOUNTS to "Accounts",
    StringKeys.STATEMENTS to "Statements",
    StringKeys.GOALS to "Saving Goals",
    StringKeys.BILLS to "Bills & Dues",
    StringKeys.CHATBOT to "AI Advisor",
    StringKeys.SETTINGS to "Settings",
    StringKeys.ADD_ACCOUNT to "Add Account",
    StringKeys.ADD_TRANSACTION to "Add Transaction",
    StringKeys.ADD_INCOME to "Add Income",
    StringKeys.ADD_EXPENSE to "Add Spent",
    StringKeys.ADD_GOAL to "New Goal",
    StringKeys.ADD_BILL to "Add Bill",
    StringKeys.BANK to "Bank",
    StringKeys.CASH to "Cash",
    StringKeys.BKASH to "bKash",
    StringKeys.NAGAD to "Nagad",
    StringKeys.ROCKET to "Rocket",
    StringKeys.INCOME to "Income",
    StringKeys.SPENT to "Spent",
    StringKeys.ALL to "All",
    StringKeys.RECENT_TRANSACTIONS to "Recent Statements",
    StringKeys.NO_TRANSACTIONS to "No transactions yet",
    StringKeys.NO_ACCOUNTS to "No accounts yet. Tap + to add Bank, bKash, Cash, etc.",
    StringKeys.NO_GOALS to "No savings goals set. Create one for your next purchase!",
    StringKeys.NO_BILLS to "No bills scheduled. Keep track of upcoming payments.",
    StringKeys.AMOUNT to "Amount (BDT)",
    StringKeys.TITLE to "Title",
    StringKeys.CATEGORY to "Category",
    StringKeys.ACCOUNT to "Account",
    StringKeys.DATE to "Date",
    StringKeys.NOTE to "Note (Optional)",
    StringKeys.SAVE to "Save",
    StringKeys.CANCEL to "Cancel",
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
    StringKeys.SIGN_IN_WITH_GOOGLE to "Sign in with Google",
    StringKeys.SIGN_OUT to "Sign Out",
    StringKeys.THEME_DARK to "Dark Mode",
    StringKeys.THEME_LIGHT to "Light Mode",
    StringKeys.THEME_SYSTEM to "System Default",
    StringKeys.LANGUAGE to "Language",
    StringKeys.PROFILE to "Profile & Identity",
    StringKeys.CURRENCY_BDT to "Currency: BDT (৳)",
    StringKeys.CHAT_PLACEHOLDER to "Ask anything about your money, budget or bills...",
    StringKeys.QUICK_QUESTIONS to "Suggested Inquiries",
    StringKeys.SALARY to "Job Salary",
    StringKeys.PATHAO_DRIVE to "Pathao Drive",
    StringKeys.TIPS to "Tips",
    StringKeys.BUSINESS to "Business",
    StringKeys.FOOD to "Food",
    StringKeys.FAMILY to "Family",
    StringKeys.RENTAL_COST to "Rental cost",
    StringKeys.OTHERS to "Others",
    StringKeys.EMPTY_STATE_HINT to "Start fresh! Add your own accounts and statements."
)

private val bnStrings = mapOf(
    StringKeys.APP_NAME to "আমার ওয়ালেট",
    StringKeys.NET_BALANCE to "মোট ব্যালেন্স",
    StringKeys.TOTAL_INCOME to "মোট আয়",
    StringKeys.TOTAL_EXPENSE to "মোট ব্যয়",
    StringKeys.ACCOUNTS to "অ্যাকাউন্টস",
    StringKeys.STATEMENTS to "স্টেটমেন্ট",
    StringKeys.GOALS to "সঞ্চয় লক্ষ্য",
    StringKeys.BILLS to "বিল ও পেমেন্ট",
    StringKeys.CHATBOT to "এআই সহকারী",
    StringKeys.SETTINGS to "সেটিংস",
    StringKeys.ADD_ACCOUNT to "+ অ্যাকাউন্ট যোগ করুন",
    StringKeys.ADD_TRANSACTION to "লেনদেন যোগ করুন",
    StringKeys.ADD_INCOME to "আয় যোগ",
    StringKeys.ADD_EXPENSE to "ব্যয় যোগ",
    StringKeys.ADD_GOAL to "নতুন লক্ষ্য",
    StringKeys.ADD_BILL to "বিল যোগ করুন",
    StringKeys.BANK to "ব্যাংক",
    StringKeys.CASH to "ক্যাশ",
    StringKeys.BKASH to "বিকাশ",
    StringKeys.NAGAD to "নগদ",
    StringKeys.ROCKET to "রকেট",
    StringKeys.INCOME to "আয়",
    StringKeys.SPENT to "ব্যয়",
    StringKeys.ALL to "সব",
    StringKeys.RECENT_TRANSACTIONS to "সাম্প্রতিক স্টেটমেন্ট",
    StringKeys.NO_TRANSACTIONS to "এখনও কোনো লেনদেন এন্ট্রি নেই",
    StringKeys.NO_ACCOUNTS to "কোনো অ্যাকাউন্ট নেই। ব্যাংক, বিকাশ, ক্যাশ যোগ করতে + চাপুন।",
    StringKeys.NO_GOALS to "কোনো সঞ্চয় লক্ষ্য নেই। নতুন কেনাকাটার লক্ষ্য তৈরি করুন!",
    StringKeys.NO_BILLS to "কোনো বিল তালিকাভুক্ত নেই। বিলের তারিখ ও নোটিফিকেশন সেট করুন।",
    StringKeys.AMOUNT to "পরিমাণ (টাকা ৳)",
    StringKeys.TITLE to "বিবরণ বা শিরোনাম",
    StringKeys.CATEGORY to "ক্যাটেগরি",
    StringKeys.ACCOUNT to "অ্যাকাউন্ট",
    StringKeys.DATE to "তারিখ",
    StringKeys.NOTE to "নোট (ঐচ্ছিক)",
    StringKeys.SAVE to "সংরক্ষণ করুন",
    StringKeys.CANCEL to "বাতিল",
    StringKeys.DUE_DATE to "পরিশোধের শেষ তারিখ",
    StringKeys.NOTIFY to "নোটিফিকেশন রিমাইন্ডার",
    StringKeys.PAID to "পরিশোধিত",
    StringKeys.UNPAID to "অপরিশোধিত",
    StringKeys.MARK_AS_PAID to "পরিশোধ সম্পন্ন",
    StringKeys.TARGET_AMOUNT to "লক্ষ্যমাত্রা",
    StringKeys.SAVED_AMOUNT to "জমেছে",
    StringKeys.REMAINING to "বাকি আছে",
    StringKeys.ADD_FUNDS to "টাকা জমা",
    StringKeys.WITHDRAW to "টাকা উত্তোলন",
    StringKeys.SIGN_IN_WITH_GOOGLE to "গুগল দিয়ে সাইন ইন করুন",
    StringKeys.SIGN_OUT to "সাইন আউট",
    StringKeys.THEME_DARK to "ডার্ক মোড",
    StringKeys.THEME_LIGHT to "লাইট মোড",
    StringKeys.THEME_SYSTEM to "সিস্টেম ডিফল্ট",
    StringKeys.LANGUAGE to "ভাষা (Language)",
    StringKeys.PROFILE to "প্রোফাইল ও নাম",
    StringKeys.CURRENCY_BDT to "মুদ্রা: বিডিটি (৳)",
    StringKeys.CHAT_PLACEHOLDER to "টাকা-পয়সা, বাজেট বা বিল সম্পর্কে জিজ্ঞাসা করুন...",
    StringKeys.QUICK_QUESTIONS to "প্রস্তাবিত প্রশ্নাবলী",
    StringKeys.SALARY to "বেতন (Job Salary)",
    StringKeys.PATHAO_DRIVE to "পাঠাও ড্রাইভ (Pathao Drive)",
    StringKeys.TIPS to "টিপস (Tips)",
    StringKeys.BUSINESS to "ব্যবসা (Business)",
    StringKeys.FOOD to "খাবার (Food)",
    StringKeys.FAMILY to "পরিবার (Family)",
    StringKeys.RENTAL_COST to "ভাড়া (Rental cost)",
    StringKeys.OTHERS to "অন্যান্য (Others)",
    StringKeys.EMPTY_STATE_HINT to "পরিষ্কার শুরু! আপনার নিজস্ব অ্যাকাউন্ট ও লেনদেন যোগ করুন।"
)

fun getString(key: String, language: AppLanguage): String {
    return when (language) {
        AppLanguage.BANGLA -> bnStrings[key] ?: enStrings[key] ?: key
        AppLanguage.ENGLISH -> enStrings[key] ?: key
    }
}

@Composable
fun appString(key: String): String {
    val lang = LocalAppLanguage.current
    return getString(key, lang)
}

fun formatBdt(amount: Double, language: AppLanguage = AppLanguage.ENGLISH): String {
    val formatted = NumberFormat.getNumberInstance(Locale.US).format(amount)
    return if (language == AppLanguage.BANGLA) {
        "৳ $formatted"
    } else {
        "৳ $formatted"
    }
}
