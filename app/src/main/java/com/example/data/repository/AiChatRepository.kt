package com.example.data.repository

import android.util.Log
import com.example.data.model.Account
import com.example.data.model.Bill
import com.example.data.model.Goal
import com.example.data.model.Transaction
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class AiChatRepository {

    private val generativeModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            systemInstruction = content {
                text(
                    """
                    You are 'My Wallet AI Advisor', an empathetic, smart personal finance and money management assistant specialized in Bangladeshi personal finances (Currency: BDT / ৳).
                    You assist the user with:
                    1. Analyzing their income and expenditures (Job salary, Pathao Drive, Tips, Business, Food, Family, Rental cost, etc.).
                    2. Giving practical savings advice in Bangladesh (bKash, Nagad, Rocket, Bank deposits, inflation tips).
                    3. Tracking budget, upcoming bills reminders, and savings goals.
                    4. Answering questions concisely and accurately in both English and Bengali (বাংলা) depending on what language the user speaks.
                    Be polite, proactive, structured with bullet points where appropriate, and encourage disciplined saving habits.
                    """.trimIndent()
                )
            }
        )
    }

    suspend fun sendChatMessage(
        userMessage: String,
        history: List<ChatMessage>,
        contextSummary: String
    ): Flow<String> = flow {
        try {
            val contentHistory = history.takeLast(10).map { msg ->
                content(if (msg.isUser) "user" else "model") {
                    text(msg.text)
                }
            }

            val chatSession = generativeModel.startChat(history = contentHistory)

            val promptWithContext = if (contextSummary.isNotBlank()) {
                "CURRENT FINANCIAL CONTEXT:\n$contextSummary\n\nUSER INQUIRY:\n$userMessage"
            } else {
                userMessage
            }

            withTimeout(60_000L) {
                val stream = chatSession.sendMessageStream(promptWithContext)
                stream.collect { chunk ->
                    val chunkText = chunk.text.orEmpty()
                    if (chunkText.isNotEmpty()) {
                        emit(chunkText)
                    }
                }
            }
        } catch (e: TimeoutCancellationException) {
            Log.e("AiChatRepository", "Chat response timed out", e)
            emit("Sorry, the request timed out. Please try asking again.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("AiChatRepository", "AI generation error", e)
            emit("I encountered an issue connecting to AI services: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    fun buildFinancialContext(
        netBalance: Double,
        accounts: List<Account>,
        transactions: List<Transaction>,
        goals: List<Goal>,
        bills: List<Bill>
    ): String {
        val accSummary = accounts.joinToString(", ") { "${it.name} (${it.type}): ৳${it.balance}" }
        val recentTxns = transactions.take(6).joinToString("; ") { "${it.type} ৳${it.amount} for ${it.title} (${it.category})" }
        val activeGoals = goals.filter { !it.isCompleted }.joinToString(", ") { "${it.title}: ৳${it.savedAmount}/৳${it.targetAmount}" }
        val unpaidBills = bills.filter { !it.isPaid }.joinToString(", ") { "${it.title}: ৳${it.amount} (Due: ${it.dueDate})" }

        return """
            Net Balance: ৳$netBalance
            Accounts: ${if (accSummary.isNotBlank()) accSummary else "None added yet"}
            Recent Statements: ${if (recentTxns.isNotBlank()) recentTxns else "None yet"}
            Active Goals: ${if (activeGoals.isNotBlank()) activeGoals else "None"}
            Unpaid Bills: ${if (unpaidBills.isNotBlank()) unpaidBills else "None"}
        """.trimIndent()
    }
}
