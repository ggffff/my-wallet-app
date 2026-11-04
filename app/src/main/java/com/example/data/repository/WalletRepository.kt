package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.Account
import com.example.data.model.Bill
import com.example.data.model.Budget
import com.example.data.model.Debt
import com.example.data.model.DebtType
import com.example.data.model.Goal
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class WalletRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        resolveFirestore(context)
    )

    companion object {
        private fun resolveFirestore(context: Context): FirebaseFirestore {
            return try {
                try {
                    val dbName = context.getString(R.string.firestore_database_id)
                    if (dbName.isNotBlank()) {
                        return FirebaseFirestore.getInstance(dbName)
                    }
                } catch (_: Throwable) {
                    // Fall back to identifier lookup
                }
                val resId = context.resources.getIdentifier("firestore_database_id", "string", context.packageName)
                val dbName = if (resId != 0) context.getString(resId) else null
                if (!dbName.isNullOrBlank()) {
                    FirebaseFirestore.getInstance(dbName)
                } else {
                    FirebaseFirestore.getInstance()
                }
            } catch (e: Exception) {
                android.util.Log.e("WalletRepository", "Failed to get named Firestore instance; falling back to default", e)
                FirebaseFirestore.getInstance()
            }
        }

        // Shared in-memory guest state so users exploring without login have a seamless, working app
        private val guestProfile = MutableStateFlow<UserProfile?>(
            UserProfile(
                displayName = "Guest User",
                currency = "BDT",
                language = "en",
                themeMode = "frosted_pearl"
            )
        )
        private val guestAccounts = MutableStateFlow<List<Account>>(
            listOf(
                Account(id = "acc_cash", name = "Cash Wallet", type = "CASH_FLOW", balance = 5000.0, colorHex = "#10B981", iconName = "AccountBalanceWallet"),
                Account(id = "acc_bkash", name = "bKash", type = "MFS", balance = 12500.0, colorHex = "#E2136E", iconName = "PhoneAndroid"),
                Account(id = "acc_bank", name = "City Bank", type = "BANK", balance = 45000.0, colorHex = "#3882F6", iconName = "AccountBalance")
            )
        )
        private val guestTransactions = MutableStateFlow<List<Transaction>>(
            listOf(
                Transaction(id = "txn_1", title = "Monthly Salary", type = "INCOME", amount = 60000.0, category = "Salary", accountId = "acc_bank", accountName = "City Bank", date = "2026-11-01"),
                Transaction(id = "txn_2", title = "Grocery Shopping", type = "SPENT", amount = 3500.0, category = "Groceries", accountId = "acc_bkash", accountName = "bKash", date = "2026-11-02"),
                Transaction(id = "txn_3", title = "Electricity Bill", type = "SPENT", amount = 1450.0, category = "Utilities", accountId = "acc_cash", accountName = "Cash Wallet", date = "2026-11-03")
            )
        )
        private val guestGoals = MutableStateFlow<List<Goal>>(
            listOf(
                Goal(id = "goal_1", title = "Emergency Fund", targetAmount = 50000.0, savedAmount = 25000.0, linkedAccountId = "acc_bank")
            )
        )
        private val guestBills = MutableStateFlow<List<Bill>>(
            listOf(
                Bill(id = "bill_1", title = "Internet Bill", amount = 1000.0, dueDate = "2026-11-10", isPaid = false, notifyEnabled = true, category = "Internet")
            )
        )
        private val guestBudgets = MutableStateFlow<List<Budget>>(emptyList())
        private val guestDebts = MutableStateFlow<List<Debt>>(emptyList())
    }

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    // --- User Profile ---
    fun observeUserProfile(): Flow<UserProfile?> {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            return guestProfile
        }
        return callbackFlow {
            val docRef = db.collection("users").document(uid)
            val registration = docRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.GET, docRef.path)
                    trySend(null)
                    return@addSnapshotListener
                }
                val profile = snapshot?.toObject(UserProfile::class.java)
                trySend(profile)
            }
            awaitClose { registration.remove() }
        }
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            guestProfile.value = profile
            return
        }
        val docRef = db.collection("users").document(uid)
        try {
            val payload = hashMapOf<String, Any>(
                "displayName" to profile.displayName,
                "currency" to profile.currency,
                "language" to profile.language,
                "themeMode" to profile.themeMode,
                "email" to (auth.currentUser?.email ?: profile.email),
                "photoUrl" to (auth.currentUser?.photoUrl?.toString() ?: profile.photoUrl),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload, SetOptions.merge()).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    // --- Accounts ---
    fun observeAccounts(): Flow<List<Account>> {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            return guestAccounts
        }
        return callbackFlow {
            val colRef = db.collection("users").document(uid).collection("accounts")
            val registration = colRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, colRef.path)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val accounts = snapshot?.documents?.mapNotNull { it.toObject(Account::class.java) } ?: emptyList()
                trySend(accounts)
            }
            awaitClose { registration.remove() }
        }
    }

    suspend fun addAccount(account: Account): String {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            val newId = "acc_" + System.currentTimeMillis()
            val newAcc = account.copy(id = newId)
            guestAccounts.value = guestAccounts.value + newAcc
            return newId
        }
        val colRef = db.collection("users").document(uid).collection("accounts")
        try {
            val allowedTypes = setOf("BANK", "MFS", "CASH_FLOW", "CASH", "BKASH", "NAGAD", "ROCKET", "OTHER")
            val sanitizedType = if (allowedTypes.contains(account.type.trim().uppercase())) {
                account.type.trim().uppercase()
            } else if (account.type.contains("MFS", ignoreCase = true) || account.type.contains("BKASH", ignoreCase = true) || account.type.contains("NAGAD", ignoreCase = true)) {
                "MFS"
            } else if (account.type.contains("CASH", ignoreCase = true)) {
                "CASH_FLOW"
            } else {
                "BANK"
            }

            val payload = hashMapOf<String, Any>(
                "name" to account.name.ifBlank { "Account" },
                "type" to sanitizedType,
                "accountNumber" to account.accountNumber,
                "balance" to account.balance,
                "colorHex" to account.colorHex,
                "iconName" to account.iconName,
                "isArchived" to account.isArchived,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            val docRef = colRef.add(payload).await()
            return docRef.id
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, colRef.path)
            throw e
        }
    }

    suspend fun updateAccount(account: Account) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            guestAccounts.value = guestAccounts.value.map { if (it.id == account.id) account else it }
            return
        }
        val docRef = db.collection("users").document(uid).collection("accounts").document(account.id)
        try {
            val allowedTypes = setOf("BANK", "MFS", "CASH_FLOW", "CASH", "BKASH", "NAGAD", "ROCKET", "OTHER")
            val sanitizedType = if (allowedTypes.contains(account.type.trim().uppercase())) {
                account.type.trim().uppercase()
            } else if (account.type.contains("MFS", ignoreCase = true) || account.type.contains("BKASH", ignoreCase = true) || account.type.contains("NAGAD", ignoreCase = true)) {
                "MFS"
            } else if (account.type.contains("CASH", ignoreCase = true)) {
                "CASH_FLOW"
            } else {
                "BANK"
            }

            val payload = hashMapOf<String, Any>(
                "name" to account.name.ifBlank { "Account" },
                "type" to sanitizedType,
                "accountNumber" to account.accountNumber,
                "balance" to account.balance,
                "colorHex" to account.colorHex,
                "iconName" to account.iconName,
                "isArchived" to account.isArchived,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.update(payload).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun archiveAccount(accountId: String, isArchived: Boolean) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            guestAccounts.value = guestAccounts.value.map { if (it.id == accountId) it.copy(isArchived = isArchived) else it }
            return
        }
        val docRef = db.collection("users").document(uid).collection("accounts").document(accountId)
        try {
            docRef.update("isArchived", isArchived, "updatedAt", FieldValue.serverTimestamp()).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun adjustAccountBalance(accountId: String, newBalance: Double) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            guestAccounts.value = guestAccounts.value.map { if (it.id == accountId) it.copy(balance = newBalance) else it }
            return
        }
        val docRef = db.collection("users").document(uid).collection("accounts").document(accountId)
        try {
            docRef.update("balance", newBalance, "updatedAt", FieldValue.serverTimestamp()).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteAccount(accountId: String) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            guestAccounts.value = guestAccounts.value.filterNot { it.id == accountId }
            return
        }
        val docRef = db.collection("users").document(uid).collection("accounts").document(accountId)
        try {
            docRef.delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- Transactions ---
    fun observeTransactions(): Flow<List<Transaction>> {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            return guestTransactions
        }
        return callbackFlow {
            val colRef = db.collection("users").document(uid).collection("transactions")
            val registration = colRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, colRef.path)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toObject(Transaction::class.java) } ?: emptyList()
                trySend(list)
            }
            awaitClose { registration.remove() }
        }
    }

    suspend fun addTransaction(txn: Transaction) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            val newId = "txn_" + System.currentTimeMillis()
            val newTxn = txn.copy(id = newId)
            guestTransactions.value = listOf(newTxn) + guestTransactions.value
            if (txn.type == TransactionType.TRANSFER.name) {
                val fee = txn.transferFee ?: 0.0
                guestAccounts.value = guestAccounts.value.map {
                    when (it.id) {
                        txn.accountId -> it.copy(balance = it.balance - (txn.amount + fee))
                        txn.toAccountId -> it.copy(balance = it.balance + txn.amount)
                        else -> it
                    }
                }
            } else {
                guestAccounts.value = guestAccounts.value.map {
                    if (it.id == txn.accountId) {
                        val newBal = if (txn.type == TransactionType.INCOME.name) it.balance + txn.amount else it.balance - txn.amount
                        it.copy(balance = newBal)
                    } else it
                }
            }
            return
        }

        val txnColRef = db.collection("users").document(uid).collection("transactions")
        val fromAccDocRef = if (txn.accountId.isNotBlank()) {
            db.collection("users").document(uid).collection("accounts").document(txn.accountId)
        } else null
        val toAccDocRef = if (txn.type == TransactionType.TRANSFER.name && !txn.toAccountId.isNullOrBlank()) {
            db.collection("users").document(uid).collection("accounts").document(txn.toAccountId)
        } else null

        try {
            db.runTransaction { firestoreTxn ->
                if (txn.type == TransactionType.TRANSFER.name) {
                    if (fromAccDocRef != null) {
                        val fromSnap = firestoreTxn.get(fromAccDocRef)
                        val fromBal = fromSnap.getDouble("balance") ?: 0.0
                        val fee = txn.transferFee ?: 0.0
                        firestoreTxn.update(fromAccDocRef, "balance", fromBal - (txn.amount + fee), "updatedAt", FieldValue.serverTimestamp())
                    }
                    if (toAccDocRef != null) {
                        val toSnap = firestoreTxn.get(toAccDocRef)
                        val toBal = toSnap.getDouble("balance") ?: 0.0
                        firestoreTxn.update(toAccDocRef, "balance", toBal + txn.amount, "updatedAt", FieldValue.serverTimestamp())
                    }
                } else {
                    if (fromAccDocRef != null) {
                        val accSnapshot = firestoreTxn.get(fromAccDocRef)
                        val currentBalance = accSnapshot.getDouble("balance") ?: 0.0
                        val newBalance = if (txn.type == TransactionType.INCOME.name) {
                            currentBalance + txn.amount
                        } else {
                            currentBalance - txn.amount
                        }
                        firestoreTxn.update(fromAccDocRef, "balance", newBalance, "updatedAt", FieldValue.serverTimestamp())
                    }
                }

                val newDocRef = txnColRef.document()
                val payload = hashMapOf<String, Any?>(
                    "title" to txn.title,
                    "amount" to txn.amount,
                    "type" to txn.type,
                    "category" to txn.category,
                    "accountId" to txn.accountId,
                    "accountName" to txn.accountName,
                    "toAccountId" to txn.toAccountId,
                    "toAccountName" to txn.toAccountName,
                    "transferFee" to txn.transferFee,
                    "date" to txn.date,
                    "note" to txn.note,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }

                firestoreTxn.set(newDocRef, payload)
            }.await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, txnColRef.path)
            throw e
        }
    }

    suspend fun editTransaction(oldTxn: Transaction, newTxn: Transaction) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            guestTransactions.value = guestTransactions.value.map { if (it.id == oldTxn.id) newTxn else it }
            return
        }
        val txnDocRef = db.collection("users").document(uid).collection("transactions").document(oldTxn.id)

        try {
            db.runTransaction { firestoreTxn ->
                if (oldTxn.type == TransactionType.TRANSFER.name) {
                    if (oldTxn.accountId.isNotBlank()) {
                        val oldFromRef = db.collection("users").document(uid).collection("accounts").document(oldTxn.accountId)
                        val oldFromBal = firestoreTxn.get(oldFromRef).getDouble("balance") ?: 0.0
                        val oldFee = oldTxn.transferFee ?: 0.0
                        firestoreTxn.update(oldFromRef, "balance", oldFromBal + (oldTxn.amount + oldFee), "updatedAt", FieldValue.serverTimestamp())
                    }
                    if (!oldTxn.toAccountId.isNullOrBlank()) {
                        val oldToRef = db.collection("users").document(uid).collection("accounts").document(oldTxn.toAccountId)
                        val oldToBal = firestoreTxn.get(oldToRef).getDouble("balance") ?: 0.0
                        firestoreTxn.update(oldToRef, "balance", oldToBal - oldTxn.amount, "updatedAt", FieldValue.serverTimestamp())
                    }
                } else if (oldTxn.accountId.isNotBlank()) {
                    val oldAccRef = db.collection("users").document(uid).collection("accounts").document(oldTxn.accountId)
                    val oldBal = firestoreTxn.get(oldAccRef).getDouble("balance") ?: 0.0
                    val revertedBal = if (oldTxn.type == TransactionType.INCOME.name) oldBal - oldTxn.amount else oldBal + oldTxn.amount
                    firestoreTxn.update(oldAccRef, "balance", revertedBal, "updatedAt", FieldValue.serverTimestamp())
                }

                if (newTxn.type == TransactionType.TRANSFER.name) {
                    if (newTxn.accountId.isNotBlank()) {
                        val newFromRef = db.collection("users").document(uid).collection("accounts").document(newTxn.accountId)
                        val newFromBal = firestoreTxn.get(newFromRef).getDouble("balance") ?: 0.0
                        val newFee = newTxn.transferFee ?: 0.0
                        firestoreTxn.update(newFromRef, "balance", newFromBal - (newTxn.amount + newFee), "updatedAt", FieldValue.serverTimestamp())
                    }
                    if (!newTxn.toAccountId.isNullOrBlank()) {
                        val newToRef = db.collection("users").document(uid).collection("accounts").document(newTxn.toAccountId)
                        val newToBal = firestoreTxn.get(newToRef).getDouble("balance") ?: 0.0
                        firestoreTxn.update(newToRef, "balance", newToBal + newTxn.amount, "updatedAt", FieldValue.serverTimestamp())
                    }
                } else if (newTxn.accountId.isNotBlank()) {
                    val newAccRef = db.collection("users").document(uid).collection("accounts").document(newTxn.accountId)
                    val currentBal = firestoreTxn.get(newAccRef).getDouble("balance") ?: 0.0
                    val updatedBal = if (newTxn.type == TransactionType.INCOME.name) currentBal + newTxn.amount else currentBal - newTxn.amount
                    firestoreTxn.update(newAccRef, "balance", updatedBal, "updatedAt", FieldValue.serverTimestamp())
                }

                val payload = hashMapOf<String, Any?>(
                    "title" to newTxn.title,
                    "amount" to newTxn.amount,
                    "type" to newTxn.type,
                    "category" to newTxn.category,
                    "accountId" to newTxn.accountId,
                    "accountName" to newTxn.accountName,
                    "toAccountId" to newTxn.toAccountId,
                    "toAccountName" to newTxn.toAccountName,
                    "transferFee" to newTxn.transferFee,
                    "date" to newTxn.date,
                    "note" to newTxn.note,
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }

                firestoreTxn.update(txnDocRef, payload)
            }.await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, txnDocRef.path)
            throw e
        }
    }

    suspend fun deleteTransaction(txn: Transaction) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            guestTransactions.value = guestTransactions.value.filterNot { it.id == txn.id }
            if (txn.type == TransactionType.TRANSFER.name) {
                val fee = txn.transferFee ?: 0.0
                guestAccounts.value = guestAccounts.value.map {
                    when (it.id) {
                        txn.accountId -> it.copy(balance = it.balance + (txn.amount + fee))
                        txn.toAccountId -> it.copy(balance = it.balance - txn.amount)
                        else -> it
                    }
                }
            } else {
                guestAccounts.value = guestAccounts.value.map {
                    if (it.id == txn.accountId) {
                        val revertedBal = if (txn.type == TransactionType.INCOME.name) it.balance - txn.amount else it.balance + txn.amount
                        it.copy(balance = revertedBal)
                    } else it
                }
            }
            return
        }

        val txnDocRef = db.collection("users").document(uid).collection("transactions").document(txn.id)
        val fromAccDocRef = if (txn.accountId.isNotBlank()) {
            db.collection("users").document(uid).collection("accounts").document(txn.accountId)
        } else null
        val toAccDocRef = if (txn.type == TransactionType.TRANSFER.name && !txn.toAccountId.isNullOrBlank()) {
            db.collection("users").document(uid).collection("accounts").document(txn.toAccountId)
        } else null

        try {
            db.runTransaction { firestoreTxn ->
                val fromBal = if (fromAccDocRef != null) {
                    val fromSnap = firestoreTxn.get(fromAccDocRef)
                    if (fromSnap.exists()) fromSnap.getDouble("balance") else null
                } else null

                val toBal = if (toAccDocRef != null) {
                    val toSnap = firestoreTxn.get(toAccDocRef)
                    if (toSnap.exists()) toSnap.getDouble("balance") else null
                } else null

                if (txn.type == TransactionType.TRANSFER.name) {
                    if (fromAccDocRef != null && fromBal != null) {
                        val fee = txn.transferFee ?: 0.0
                        firestoreTxn.update(fromAccDocRef, "balance", fromBal + (txn.amount + fee), "updatedAt", FieldValue.serverTimestamp())
                    }
                    if (toAccDocRef != null && toBal != null) {
                        firestoreTxn.update(toAccDocRef, "balance", toBal - txn.amount, "updatedAt", FieldValue.serverTimestamp())
                    }
                } else {
                    if (fromAccDocRef != null && fromBal != null) {
                        val revertedBalance = if (txn.type == TransactionType.INCOME.name) {
                            fromBal - txn.amount
                        } else {
                            fromBal + txn.amount
                        }
                        firestoreTxn.update(fromAccDocRef, "balance", revertedBalance, "updatedAt", FieldValue.serverTimestamp())
                    }
                }

                firestoreTxn.delete(txnDocRef)
            }.await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, txnDocRef.path)
            throw e
        }
    }

    // --- Goals ---
    fun observeGoals(): Flow<List<Goal>> {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            return guestGoals
        }
        return callbackFlow {
            val colRef = db.collection("users").document(uid).collection("goals")
            val registration = colRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, colRef.path)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toObject(Goal::class.java) } ?: emptyList()
                trySend(list)
            }
            awaitClose { registration.remove() }
        }
    }

    suspend fun addGoal(goal: Goal) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            val newGoal = goal.copy(id = "goal_" + System.currentTimeMillis())
            guestGoals.value = guestGoals.value + newGoal
            return
        }
        val colRef = db.collection("users").document(uid).collection("goals")
        try {
            val payload = hashMapOf<String, Any>(
                "title" to goal.title,
                "targetAmount" to goal.targetAmount,
                "savedAmount" to goal.savedAmount,
                "targetDate" to goal.targetDate,
                "icon" to goal.icon,
                "note" to goal.note,
                "isCompleted" to (goal.savedAmount >= goal.targetAmount),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            colRef.add(payload).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, colRef.path)
            throw e
        }
    }

    suspend fun adjustGoalSavings(goal: Goal, deltaAmount: Double, accountId: String? = null) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            val newSaved = (goal.savedAmount + deltaAmount).coerceAtLeast(0.0)
            val isCompleted = newSaved >= goal.targetAmount
            guestGoals.value = guestGoals.value.map {
                if (it.id == goal.id) it.copy(savedAmount = newSaved, isCompleted = isCompleted) else it
            }
            if (!accountId.isNullOrBlank()) {
                guestAccounts.value = guestAccounts.value.map {
                    if (it.id == accountId) it.copy(balance = it.balance - deltaAmount) else it
                }
            }
            return
        }

        val goalDocRef = db.collection("users").document(uid).collection("goals").document(goal.id)
        val accDocRef = if (!accountId.isNullOrBlank()) {
            db.collection("users").document(uid).collection("accounts").document(accountId)
        } else null

        try {
            db.runTransaction { firestoreTxn ->
                val newSaved = (goal.savedAmount + deltaAmount).coerceAtLeast(0.0)
                val isCompleted = newSaved >= goal.targetAmount

                firestoreTxn.update(
                    goalDocRef,
                    "savedAmount", newSaved,
                    "isCompleted", isCompleted,
                    "updatedAt", FieldValue.serverTimestamp()
                )

                if (accDocRef != null) {
                    val accSnap = firestoreTxn.get(accDocRef)
                    val accBal = accSnap.getDouble("balance") ?: 0.0
                    val updatedBal = accBal - deltaAmount
                    firestoreTxn.update(accDocRef, "balance", updatedBal, "updatedAt", FieldValue.serverTimestamp())
                }
            }.await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, goalDocRef.path)
            throw e
        }
    }

    suspend fun deleteGoal(goalId: String) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            guestGoals.value = guestGoals.value.filterNot { it.id == goalId }
            return
        }
        val docRef = db.collection("users").document(uid).collection("goals").document(goalId)
        try {
            docRef.delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- Bills ---
    fun observeBills(): Flow<List<Bill>> {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            return guestBills
        }
        return callbackFlow {
            val colRef = db.collection("users").document(uid).collection("bills")
            val registration = colRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, colRef.path)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toObject(Bill::class.java) } ?: emptyList()
                trySend(list)
            }
            awaitClose { registration.remove() }
        }
    }

    suspend fun addBill(bill: Bill) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            val newBill = bill.copy(id = "bill_" + System.currentTimeMillis())
            guestBills.value = guestBills.value + newBill
            return
        }
        val colRef = db.collection("users").document(uid).collection("bills")
        try {
            val payload = hashMapOf<String, Any>(
                "title" to bill.title,
                "amount" to bill.amount,
                "dueDate" to bill.dueDate,
                "category" to bill.category,
                "isPaid" to bill.isPaid,
                "notifyEnabled" to bill.notifyEnabled,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            colRef.add(payload).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, colRef.path)
            throw e
        }
    }

    suspend fun toggleBillPaid(bill: Bill, accountId: String? = null) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            val newPaid = !bill.isPaid
            guestBills.value = guestBills.value.map {
                if (it.id == bill.id) it.copy(isPaid = newPaid) else it
            }
            if (newPaid && !accountId.isNullOrBlank()) {
                guestAccounts.value = guestAccounts.value.map {
                    if (it.id == accountId) it.copy(balance = it.balance - bill.amount) else it
                }
            }
            return
        }

        val billDocRef = db.collection("users").document(uid).collection("bills").document(bill.id)
        val accDocRef = if (!bill.isPaid && !accountId.isNullOrBlank()) {
            db.collection("users").document(uid).collection("accounts").document(accountId)
        } else null

        try {
            db.runTransaction { firestoreTxn ->
                val newPaid = !bill.isPaid
                firestoreTxn.update(
                    billDocRef,
                    "isPaid", newPaid,
                    "paidAt", if (newPaid) FieldValue.serverTimestamp() else null,
                    "paidFromAccountId", if (newPaid) accountId else null,
                    "updatedAt", FieldValue.serverTimestamp()
                )

                if (newPaid && accDocRef != null) {
                    val accSnap = firestoreTxn.get(accDocRef)
                    val accBal = accSnap.getDouble("balance") ?: 0.0
                    firestoreTxn.update(accDocRef, "balance", accBal - bill.amount, "updatedAt", FieldValue.serverTimestamp())
                }
            }.await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, billDocRef.path)
            throw e
        }
    }

    suspend fun editGoal(goal: Goal) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            guestGoals.value = guestGoals.value.map { if (it.id == goal.id) goal else it }
            return
        }
        val docRef = db.collection("users").document(uid).collection("goals").document(goal.id)
        try {
            val payload = hashMapOf<String, Any?>(
                "title" to goal.title,
                "targetAmount" to goal.targetAmount,
                "targetDate" to goal.targetDate,
                "startDate" to goal.startDate,
                "linkedAccountId" to goal.linkedAccountId,
                "reminderFrequency" to goal.reminderFrequency,
                "reminderEnabled" to goal.reminderEnabled,
                "icon" to goal.icon,
                "note" to goal.note,
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }
            docRef.update(payload).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteBill(billId: String) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            guestBills.value = guestBills.value.filterNot { it.id == billId }
            return
        }
        val docRef = db.collection("users").document(uid).collection("bills").document(billId)
        try {
            docRef.delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- Budgets ---
    fun observeBudgets(): Flow<List<Budget>> {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            return guestBudgets
        }
        return callbackFlow {
            val colRef = db.collection("users").document(uid).collection("budgets")
            val registration = colRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, colRef.path)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toObject(Budget::class.java) } ?: emptyList()
                trySend(list)
            }
            awaitClose { registration.remove() }
        }
    }

    suspend fun saveBudget(budget: Budget) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            guestBudgets.value = if (budget.id.isNotBlank()) {
                guestBudgets.value.map { if (it.id == budget.id) budget else it }
            } else {
                guestBudgets.value + budget.copy(id = "budget_" + System.currentTimeMillis())
            }
            return
        }
        val colRef = db.collection("users").document(uid).collection("budgets")
        try {
            val payload = hashMapOf<String, Any>(
                "category" to budget.category,
                "monthlyLimit" to budget.monthlyLimit,
                "monthYear" to budget.monthYear,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (budget.id.isNotBlank()) {
                val docRef = colRef.document(budget.id)
                docRef.update(payload).await()
            } else {
                payload["createdAt"] = FieldValue.serverTimestamp()
                colRef.add(payload).await()
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, colRef.path)
            throw e
        }
    }

    suspend fun deleteBudget(budgetId: String) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            guestBudgets.value = guestBudgets.value.filterNot { it.id == budgetId }
            return
        }
        val docRef = db.collection("users").document(uid).collection("budgets").document(budgetId)
        try {
            docRef.delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- Debts ---
    fun observeDebts(): Flow<List<Debt>> {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            return guestDebts
        }
        return callbackFlow {
            val colRef = db.collection("users").document(uid).collection("debts")
            val registration = colRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, colRef.path)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toObject(Debt::class.java) } ?: emptyList()
                trySend(list)
            }
            awaitClose { registration.remove() }
        }
    }

    suspend fun addDebt(debt: Debt, accountId: String? = null) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            val newDebt = debt.copy(id = "debt_" + System.currentTimeMillis())
            guestDebts.value = guestDebts.value + newDebt
            if (!accountId.isNullOrBlank()) {
                guestAccounts.value = guestAccounts.value.map {
                    if (it.id == accountId) {
                        val newBal = if (debt.type == DebtType.LENT.name) it.balance - debt.amount else it.balance + debt.amount
                        it.copy(balance = newBal)
                    } else it
                }
            }
            return
        }

        val colRef = db.collection("users").document(uid).collection("debts")
        val accDocRef = if (!accountId.isNullOrBlank()) {
            db.collection("users").document(uid).collection("accounts").document(accountId)
        } else null

        try {
            db.runTransaction { firestoreTxn ->
                if (accDocRef != null) {
                    val accSnap = firestoreTxn.get(accDocRef)
                    val accBal = accSnap.getDouble("balance") ?: 0.0
                    val newBal = if (debt.type == DebtType.LENT.name) accBal - debt.amount else accBal + debt.amount
                    firestoreTxn.update(accDocRef, "balance", newBal, "updatedAt", FieldValue.serverTimestamp())
                }

                val docRef = colRef.document()
                val payload = hashMapOf<String, Any>(
                    "personName" to debt.personName,
                    "type" to debt.type,
                    "amount" to debt.amount,
                    "repaidAmount" to debt.repaidAmount,
                    "date" to debt.date,
                    "dueDate" to debt.dueDate,
                    "note" to debt.note,
                    "isSettled" to (debt.repaidAmount >= debt.amount),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                firestoreTxn.set(docRef, payload)
            }.await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, colRef.path)
            throw e
        }
    }

    suspend fun recordDebtRepayment(debt: Debt, repayAmount: Double, accountId: String? = null) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            val newRepaid = debt.repaidAmount + repayAmount
            val isSettled = newRepaid >= debt.amount
            guestDebts.value = guestDebts.value.map {
                if (it.id == debt.id) it.copy(repaidAmount = newRepaid, isSettled = isSettled) else it
            }
            if (!accountId.isNullOrBlank()) {
                guestAccounts.value = guestAccounts.value.map {
                    if (it.id == accountId) {
                        val newBal = if (debt.type == DebtType.LENT.name) it.balance + repayAmount else it.balance - repayAmount
                        it.copy(balance = newBal)
                    } else it
                }
            }
            return
        }

        val debtDocRef = db.collection("users").document(uid).collection("debts").document(debt.id)
        val accDocRef = if (!accountId.isNullOrBlank()) {
            db.collection("users").document(uid).collection("accounts").document(accountId)
        } else null

        try {
            db.runTransaction { firestoreTxn ->
                val newRepaid = debt.repaidAmount + repayAmount
                val isSettled = newRepaid >= debt.amount

                firestoreTxn.update(
                    debtDocRef,
                    "repaidAmount", newRepaid,
                    "isSettled", isSettled,
                    "updatedAt", FieldValue.serverTimestamp()
                )

                if (accDocRef != null) {
                    val accSnap = firestoreTxn.get(accDocRef)
                    val accBal = accSnap.getDouble("balance") ?: 0.0
                    val newBal = if (debt.type == DebtType.LENT.name) accBal + repayAmount else accBal - repayAmount
                    firestoreTxn.update(accDocRef, "balance", newBal, "updatedAt", FieldValue.serverTimestamp())
                }
            }.await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, debtDocRef.path)
            throw e
        }
    }

    suspend fun deleteDebt(debtId: String) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            guestDebts.value = guestDebts.value.filterNot { it.id == debtId }
            return
        }
        val docRef = db.collection("users").document(uid).collection("debts").document(debtId)
        try {
            docRef.delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }
}
