package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class AccountType(val defaultLabel: String) {
    BANK("Bank"),
    MFS("MFS"),
    CASH_FLOW("Cash Flow"),
    CASH("Cash"),
    BKASH("bKash"),
    NAGAD("Nagad"),
    ROCKET("Rocket"),
    OTHER("Other")
}

enum class TransactionType {
    INCOME,
    SPENT,
    TRANSFER
}

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val displayName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val isPhoneVerified: Boolean = false,
    val photoUrl: String = "",
    val currency: String = "BDT",
    val language: String = "en",
    val themeMode: String = "cyber_neon",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Account(
    @DocumentId val id: String = "",
    val name: String = "",
    val type: String = AccountType.CASH.name,
    val accountNumber: String = "",
    val balance: Double = 0.0,
    val colorHex: String = "#0284C7",
    val iconName: String = "wallet",
    val isArchived: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Transaction(
    @DocumentId val id: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val type: String = TransactionType.SPENT.name,
    val category: String = "Others",
    val accountId: String = "",
    val accountName: String = "",
    val toAccountId: String = "",
    val toAccountName: String = "",
    val transferFee: Double = 0.0,
    val date: String = "",
    val note: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Goal(
    @DocumentId val id: String = "",
    val title: String = "",
    val targetAmount: Double = 0.0,
    val savedAmount: Double = 0.0,
    val startDate: String = "",
    val targetDate: String = "",
    val linkedAccountId: String = "",
    val reminderFrequency: String = "MONTHLY",
    val reminderEnabled: Boolean = true,
    val icon: String = "star",
    val note: String = "",
    val isCompleted: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Bill(
    @DocumentId val id: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val dueDate: String = "",
    val category: String = "Utilities",
    val isPaid: Boolean = false,
    val notifyEnabled: Boolean = true,
    val isRecurring: Boolean = false,
    val recurringFrequency: String = "MONTHLY",
    val paidFromAccountId: String = "",
    val paidAt: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Budget(
    @DocumentId val id: String = "",
    val category: String = "OVERALL",
    val monthlyLimit: Double = 0.0,
    val monthYear: String = "", // e.g. "2026-10"
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

enum class DebtType {
    LENT,      // Money lent to someone (receivable)
    BORROWED   // Money borrowed from someone (payable)
}

@IgnoreExtraProperties
data class Debt(
    @DocumentId val id: String = "",
    val personName: String = "",
    val type: String = DebtType.LENT.name,
    val amount: Double = 0.0,
    val repaidAmount: Double = 0.0,
    val date: String = "",
    val dueDate: String = "",
    val note: String = "",
    val isSettled: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class HabitRoutine(
    @DocumentId val id: String = "",
    val title: String = "",
    val category: String = "Personal",
    val lastDoneDate: String = "",
    val nextDueDate: String = "",
    val intervalDays: Int = 30,
    val intervalLabel: String = "1 Month",
    val estimatedCost: Double = 0.0,
    val note: String = "",
    val totalTimesCompleted: Int = 1,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
