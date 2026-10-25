package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class AccountType(val defaultLabel: String) {
    BANK("Bank"),
    CASH("Cash"),
    BKASH("bKash"),
    NAGAD("Nagad"),
    ROCKET("Rocket"),
    OTHER("Other")
}

enum class TransactionType {
    INCOME,
    SPENT
}

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val currency: String = "BDT",
    val language: String = "en",
    val themeMode: String = "dark",
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
    val targetDate: String = "",
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
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
