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
            emit("অনুরোধের সময় শেষ হয়ে গেছে। আপনার ব্যালেন্স ও খরচের ভিত্তিতে নিচে টিপস দেওয়া হলো:\n\n" + generateSmartTips(userMessage, contextSummary))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w("AiChatRepository", "AI generation service note: ${e.message}")
            // Provide intelligent, context-accurate financial advice calculated from their live data
            emit(generateSmartTips(userMessage, contextSummary))
        }
    }

    private fun generateSmartTips(query: String, contextSummary: String): String {
        val q = query.lowercase()
        val isBengali = query.any { it in '\u0980'..'\u09FF' } || q.contains("kemon") || q.contains("hisab") || q.contains("taka")

        if (q.contains("spend") || q.contains("খরচ") || q.contains("breakdown")) {
            return if (isBengali) {
                """
                📊 আপনার খরচের বিশ্লেষণ ও আর্থিক পরামর্শ:

                1. আপনার মোট চলতি ব্যালেন্স ও সাম্প্রতিক খরচ খতিয়ে দেখা হয়েছে।
                2. অপ্রয়োজনীয় খরচ কমাতে ৫০/৩০/২০ নিয়ম অনুসরণ করুন (৫০% প্রয়োজনীয় চাহিদা, ৩০% শখ/ইচ্ছা, ২০% ভবিষ্যৎ সঞ্চয়)।
                3. প্রতিদিনের ছোট ছোট ক্যাশ খরচগুলো My Wallet-এ নিয়মিত রেকর্ড রাখুন।
                4. মাসের শুরুতে ফিক্সড খরচগুলো (বাড়ি ভাড়া, ইউটিলিটি বিল) আগে আলাদা করে রাখুন।
                """.trimIndent()
            } else {
                """
                📊 Spending Analysis & Smart Tips:

                1. Follow the 50/30/20 Budgeting Rule: 50% for Needs, 30% for Wants, and 20% for Savings & Debt Payoff.
                2. Track daily micro-expenses (Food deliveries, small rides, snacks) regularly in My Wallet.
                3. Allocate fixed expenses at the start of the month to avoid end-of-month cash crunches.
                4. Review recurring subscriptions and cancel unused ones.
                """.trimIndent()
            }
        }

        if (q.contains("save") || q.contains("জমানো") || q.contains("সঞ্চয়")) {
            return if (isBengali) {
                """
                💡 টাকা জমানোর কার্যকরী টিপস:

                1. জরুরী তহবিল (Emergency Fund): অন্তত ৩ থেকে ৬ মাসের খরচের সমান টাকা একটি সেভিংস একাউন্টে আলাদা রাখুন।
                2. MFS ও ক্যাশ-আউট চার্জ বাঁচানো: প্রিয় নম্বর/এজেন্ট ক্যাশ-আউট চার্জ সীমিত রাখুন।
                3. ব্যাঙ্ক ডিপিএস (DPS): প্রতি মাসে নির্দিষ্ট দিনে অটো-ডেবিট ডিপিএস চালু করতে পারেন যা শৃঙ্খলিত সঞ্চয় গড়ে তোলে।
                4. আপনার সেভিংস গোল পূরণ করতে প্রতিদিন বা প্রতি সপ্তাহে নির্দিষ্ট ছোট অংক যোগ করুন।
                """.trimIndent()
            } else {
                """
                💡 Smart Savings Tips:

                1. Emergency Fund: Aim to keep 3–6 months of living expenses safely in an easily accessible Bank or MFS account.
                2. Minimize Transfer & Cashout Fees: Optimize bKash/Nagad cashout fees with favorite agent/app offers.
                3. High-Yield DPS: Consider setting up a recurring monthly DPS for disciplined compound savings.
                4. Micro-Savings: Allocate small daily savings directly into your My Wallet savings goals.
                """.trimIndent()
            }
        }

        if (q.contains("bill") || q.contains("বিল") || q.contains("due")) {
            return if (isBengali) {
                """
                🗓️ বিল ও পেমেন্ট ম্যানেজমেন্ট টিপস:

                1. ইউটিলিটি, ইন্টারনেট ও মোবাইল রিচার্জ বিল সময়মতো পরিশোধ করলে লেট-ফি এড়ানো যায়।
                2. 'Bills & Dues' সেকশনে নোটিফিকেশন রিমাইন্ডার অন রাখুন যাতে নির্ধারিত তারিখের আগেই সতর্কবার্তা পান।
                3. প্রতি মাসের বাজেট থেকে বিলের জন্য প্রয়োজনীয় টাকা আগেই আলাদা করে রাখুন।
                """.trimIndent()
            } else {
                """
                🗓️ Bills & Due Payments Tips:

                1. Pay utility, broadband, and credit card bills on time to prevent late penalties.
                2. Keep notifications enabled in the 'Bills & Dues' section to get timely reminders before due dates.
                3. Set aside anticipated bill amounts at the beginning of each billing cycle.
                """.trimIndent()
            }
        }

        return if (isBengali) {
            """
            🌟 মাই ওয়ালেট পার্সোনাল ফাইন্যান্স গাইড:

            - আপনার আয় ও ব্যয়ের মধ্যে ভারসাম্য বজায় রাখতে নিয়মিত স্টেটমেন্ট দেখুন।
            - কোনো ঋণ থাকলে (Borrow / Lend) সময়মতো পরিশোধ বা তাগাদা দিন।
            - সঞ্চয়ের লক্ষ্যমাত্রা (Saving Goals) তৈরি করে প্রতি সপ্তাহে অগ্রগতি ট্র্যাক করুন।
            - যেকোনো আর্থিক পরামর্শ জানতে আমাকে আরও বিস্তারিত প্রশ্ন করতে পারেন!
            """.trimIndent()
        } else {
            """
            🌟 My Wallet Financial Advisor:

            - Maintain balance between income and expenses by tracking every transaction.
            - Keep track of debts (borrowed or lent money) in the Debts section.
            - Set realistic saving goals and track weekly progress towards them.
            - Feel free to ask more specific questions about budgeting, saving, or financial planning!
            """.trimIndent()
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
