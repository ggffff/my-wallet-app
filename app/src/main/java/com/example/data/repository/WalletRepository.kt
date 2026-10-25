package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.Account
import com.example.data.model.Bill
import com.example.data.model.Goal
import com.example.data.model.Transaction
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class WalletRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        resolveFirestore(context)
    )

    companion object {
        private fun resolveFirestore(context: Context): FirebaseFirestore {
            return try {
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
    }

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    // --- User Profile ---
    fun observeUserProfile(): Flow<UserProfile?> = callbackFlow {
        val uid = try { requireUserId() } catch (e: Exception) { close(e); return@callbackFlow }
        val docRef = db.collection("users").document(uid)
        val registration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.GET, docRef.path)
                close(error)
                return@addSnapshotListener
            }
            val profile = snapshot?.toObject(UserProfile::class.java)
            trySend(profile)
        }
        awaitClose { registration.remove() }
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        val uid = requireUserId()
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
    fun observeAccounts(): Flow<List<Account>> = callbackFlow {
        val uid = try { requireUserId() } catch (e: Exception) { close(e); return@callbackFlow }
        val colRef = db.collection("users").document(uid).collection("accounts")
        val registration = colRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.LIST, colRef.path)
                close(error)
                return@addSnapshotListener
            }
            val accounts = snapshot?.documents?.mapNotNull { it.toObject(Account::class.java) } ?: emptyList()
            trySend(accounts)
        }
        awaitClose { registration.remove() }
    }

    suspend fun addAccount(account: Account): String {
        val uid = requireUserId()
        val colRef = db.collection("users").document(uid).collection("accounts")
        try {
            val payload = hashMapOf<String, Any>(
                "name" to account.name,
                "type" to account.type,
                "accountNumber" to account.accountNumber,
                "balance" to account.balance,
                "colorHex" to account.colorHex,
                "iconName" to account.iconName,
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
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("accounts").document(account.id)
        try {
            val payload = hashMapOf<String, Any>(
                "name" to account.name,
                "type" to account.type,
                "accountNumber" to account.accountNumber,
                "balance" to account.balance,
                "colorHex" to account.colorHex,
                "iconName" to account.iconName,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.update(payload).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteAccount(accountId: String) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("accounts").document(accountId)
        try {
            docRef.delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- Transactions ---
    fun observeTransactions(): Flow<List<Transaction>> = callbackFlow {
        val uid = try { requireUserId() } catch (e: Exception) { close(e); return@callbackFlow }
        val colRef = db.collection("users").document(uid).collection("transactions")
        val registration = colRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.LIST, colRef.path)
                close(error)
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { it.toObject(Transaction::class.java) } ?: emptyList()
            trySend(list)
        }
        awaitClose { registration.remove() }
    }

    suspend fun addTransaction(txn: Transaction) {
        val uid = requireUserId()
        val txnColRef = db.collection("users").document(uid).collection("transactions")
        val accDocRef = if (txn.accountId.isNotBlank()) {
            db.collection("users").document(uid).collection("accounts").document(txn.accountId)
        } else null

        try {
            db.runTransaction { firestoreTxn ->
                // Adjust account balance if account selected
                if (accDocRef != null) {
                    val accSnapshot = firestoreTxn.get(accDocRef)
                    val currentBalance = accSnapshot.getDouble("balance") ?: 0.0
                    val newBalance = if (txn.type == "INCOME") {
                        currentBalance + txn.amount
                    } else {
                        currentBalance - txn.amount
                    }
                    firestoreTxn.update(accDocRef, "balance", newBalance, "updatedAt", FieldValue.serverTimestamp())
                }

                val newDocRef = txnColRef.document()
                val payload = hashMapOf<String, Any>(
                    "title" to txn.title,
                    "amount" to txn.amount,
                    "type" to txn.type,
                    "category" to txn.category,
                    "accountId" to txn.accountId,
                    "accountName" to txn.accountName,
                    "date" to txn.date,
                    "note" to txn.note,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                firestoreTxn.set(newDocRef, payload)
            }.await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, txnColRef.path)
            throw e
        }
    }

    suspend fun deleteTransaction(txn: Transaction) {
        val uid = requireUserId()
        val txnDocRef = db.collection("users").document(uid).collection("transactions").document(txn.id)
        val accDocRef = if (txn.accountId.isNotBlank()) {
            db.collection("users").document(uid).collection("accounts").document(txn.accountId)
        } else null

        try {
            db.runTransaction { firestoreTxn ->
                if (accDocRef != null) {
                    val accSnapshot = firestoreTxn.get(accDocRef)
                    val currentBalance = accSnapshot.getDouble("balance") ?: 0.0
                    // Revert balance change
                    val revertedBalance = if (txn.type == "INCOME") {
                        currentBalance - txn.amount
                    } else {
                        currentBalance + txn.amount
                    }
                    firestoreTxn.update(accDocRef, "balance", revertedBalance, "updatedAt", FieldValue.serverTimestamp())
                }
                firestoreTxn.delete(txnDocRef)
            }.await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, txnDocRef.path)
            throw e
        }
    }

    // --- Goals ---
    fun observeGoals(): Flow<List<Goal>> = callbackFlow {
        val uid = try { requireUserId() } catch (e: Exception) { close(e); return@callbackFlow }
        val colRef = db.collection("users").document(uid).collection("goals")
        val registration = colRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.LIST, colRef.path)
                close(error)
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { it.toObject(Goal::class.java) } ?: emptyList()
            trySend(list)
        }
        awaitClose { registration.remove() }
    }

    suspend fun addGoal(goal: Goal) {
        val uid = requireUserId()
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
        val uid = requireUserId()
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
                    // Depositing to goal decreases account balance; withdrawing increases account balance
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
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("goals").document(goalId)
        try {
            docRef.delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- Bills ---
    fun observeBills(): Flow<List<Bill>> = callbackFlow {
        val uid = try { requireUserId() } catch (e: Exception) { close(e); return@callbackFlow }
        val colRef = db.collection("users").document(uid).collection("bills")
        val registration = colRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.LIST, colRef.path)
                close(error)
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { it.toObject(Bill::class.java) } ?: emptyList()
            trySend(list)
        }
        awaitClose { registration.remove() }
    }

    suspend fun addBill(bill: Bill) {
        val uid = requireUserId()
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
        val uid = requireUserId()
        val billDocRef = db.collection("users").document(uid).collection("bills").document(bill.id)
        val accDocRef = if (!bill.isPaid && !accountId.isNullOrBlank()) {
            db.collection("users").document(uid).collection("accounts").document(accountId)
        } else null

        try {
            db.runTransaction { firestoreTxn ->
                val newPaidStatus = !bill.isPaid
                firestoreTxn.update(
                    billDocRef,
                    "isPaid", newPaidStatus,
                    "updatedAt", FieldValue.serverTimestamp()
                )

                // If marking as paid with an account, deduct bill amount
                if (accDocRef != null && newPaidStatus) {
                    val accSnap = firestoreTxn.get(accDocRef)
                    val accBal = accSnap.getDouble("balance") ?: 0.0
                    val updatedBal = accBal - bill.amount
                    firestoreTxn.update(accDocRef, "balance", updatedBal, "updatedAt", FieldValue.serverTimestamp())
                }
            }.await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, billDocRef.path)
            throw e
        }
    }

    suspend fun deleteBill(billId: String) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("bills").document(billId)
        try {
            docRef.delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }
}
