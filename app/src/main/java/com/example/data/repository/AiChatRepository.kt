package com.example.data.repository

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.Account
import com.example.data.model.Bill
import com.example.data.model.Goal
import com.example.data.model.Transaction
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class AiChatRepository(
    private val apiKeyProvider: () -> String = { BuildConfig.GEMINI_API_KEY }
) {

    companion object {
        private const val TAG = "AiChatRepository"
        internal const val PLACEHOLDER_GEMINI_API_KEY = "MY_GEMINI_API_KEY"
        private const val GEMINI_API_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
        private val REST_MODELS = listOf(
            "gemini-3.8-flash",
            "gemini-3.5-flash-lite",
            "gemini-3-flash-preview"
        )
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        private val SYSTEM_PROMPT = """
            You are 'My Wallet AI Advisor', an empathetic, smart personal finance and money management assistant specialized in Bangladeshi personal finances (Currency: BDT / ৳).
            You assist the user with:
            1. Analyzing their income and expenditures (Job salary, Pathao Drive, Tips, Business, Food, Family, Rental cost, etc.).
            2. Giving practical savings advice in Bangladesh (bKash, Nagad, Rocket, Bank deposits, inflation tips).
            3. Tracking budget, upcoming bills reminders, and savings goals.
            4. Answering questions concisely and accurately in both English and Bengali (বাংলা) depending on what language the user speaks.
            Be polite, proactive, structured with bullet points where appropriate, and encourage disciplined saving habits.
        """.trimIndent()

        private val sharedHttpClient: OkHttpClient by lazy {
            OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
        }

        internal fun isGeminiApiKeyConfigured(apiKey: String?): Boolean {
            val trimmed = apiKey?.trim().orEmpty()
            return trimmed.isNotEmpty() && trimmed != PLACEHOLDER_GEMINI_API_KEY
        }

        /**
         * Ensures Gemini conversation history always starts with a 'user' turn,
         * alternates strictly user -> model, and ends with a 'model' turn so the
         * caller can safely append the next 'user' prompt.
         */
        internal fun sanitizeHistory(history: List<ChatMessage>): List<ChatMessage> {
            val filtered = history
                .filter { it.text.isNotBlank() && it.text != "Thinking..." }
                .dropWhile { !it.isUser }

            val alternating = mutableListOf<ChatMessage>()
            for (msg in filtered) {
                val last = alternating.lastOrNull()
                if (last == null || last.isUser != msg.isUser) {
                    alternating.add(msg)
                } else {
                    alternating[alternating.lastIndex] = msg
                }
            }
            if (alternating.lastOrNull()?.isUser == true) {
                alternating.removeAt(alternating.lastIndex)
            }
            return alternating.takeLast(10)
        }

        internal fun buildRestRequestJson(
            promptWithContext: String,
            history: List<ChatMessage>
        ): String {
            val contents = JSONArray()
            sanitizeHistory(history).forEach { msg ->
                val part = JSONObject().put("text", msg.text)
                val turn = JSONObject()
                    .put("role", if (msg.isUser) "user" else "model")
                    .put("parts", JSONArray().put(part))
                contents.put(turn)
            }
            contents.put(
                JSONObject()
                    .put("role", "user")
                    .put("parts", JSONArray().put(JSONObject().put("text", promptWithContext)))
            )

            val systemInstruction = JSONObject()
                .put("parts", JSONArray().put(JSONObject().put("text", SYSTEM_PROMPT)))

            return JSONObject()
                .put("system_instruction", systemInstruction)
                .put("contents", contents)
                .toString()
        }

        internal fun extractTextFromGeminiJson(rawJson: String): String? {
            if (rawJson.isBlank()) return null
            val root = JSONObject(rawJson)
            val candidates = root.optJSONArray("candidates") ?: return null
            if (candidates.length() == 0) return null
            val firstCandidate = candidates.optJSONObject(0) ?: return null
            val contentObj = firstCandidate.optJSONObject("content") ?: return null
            val parts = contentObj.optJSONArray("parts") ?: return null
            val builder = StringBuilder()
            for (i in 0 until parts.length()) {
                val part = parts.optJSONObject(i) ?: continue
                val text = part.optString("text", "")
                if (text.isNotEmpty()) {
                    builder.append(text)
                }
            }
            return builder.toString().takeIf { it.isNotBlank() }
        }
    }

    private val generativeModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            systemInstruction = content {
                text(SYSTEM_PROMPT)
            }
        )
    }

    suspend fun sendChatMessage(
        userMessage: String,
        history: List<ChatMessage>,
        contextSummary: String
    ): Flow<String> = flow {
        val promptWithContext = if (contextSummary.isNotBlank()) {
            "CURRENT FINANCIAL CONTEXT:\n$contextSummary\n\nUSER INQUIRY:\n$userMessage"
        } else {
            userMessage
        }

        val cleanHistory = sanitizeHistory(history)
        var restFailureReason: String? = null
        val configuredApiKey = runCatching { apiKeyProvider().trim() }.getOrDefault("")
        if (isGeminiApiKeyConfigured(configuredApiKey)) {
            val (restResult, restError) = trySendViaGeminiApiKey(
                apiKey = configuredApiKey,
                promptWithContext = promptWithContext,
                history = cleanHistory
            )
            if (!restResult.isNullOrBlank()) {
                emit(restResult)
                return@flow
            }
            restFailureReason = restError
        }

        try {
            val contentHistory = cleanHistory.map { msg ->
                content(if (msg.isUser) "user" else "model") {
                    text(msg.text)
                }
            }

            val chatSession = generativeModel.startChat(history = contentHistory)
            var emittedAnyChunk = false

            withTimeout(60_000L) {
                val stream = chatSession.sendMessageStream(promptWithContext)
                stream.collect { chunk ->
                    val chunkText = chunk.text.orEmpty()
                    if (chunkText.isNotEmpty()) {
                        emittedAnyChunk = true
                        emit(chunkText)
                    }
                }
            }
            if (!emittedAnyChunk) {
                val diagnostic = restFailureReason ?: "Live AI returned an empty response."
                emit("⚠️ $diagnostic\n\n" + generateSmartTips(userMessage, contextSummary))
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Chat response timed out", e)
            emit("⏱️ অনুরোধের সময় শেষ হয়ে গেছে। আপনার ব্যালেন্স ও খরচের ভিত্তিতে নিচে টিপস দেওয়া হলো:\n\n" + generateSmartTips(userMessage, contextSummary))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "AI generation failed: ${e.message}", e)
            val reason = restFailureReason
                ?: e.localizedMessage?.takeIf { it.isNotBlank() }
                ?: "AI service unavailable."
            emit("⚠️ Live AI unavailable ($reason)\n\n" + generateSmartTips(userMessage, contextSummary))
        }
    }

    private suspend fun trySendViaGeminiApiKey(
        apiKey: String,
        promptWithContext: String,
        history: List<ChatMessage>
    ): Pair<String?, String?> = withContext(Dispatchers.IO) {
        val payloadJson = buildRestRequestJson(promptWithContext, history)
        var lastError: String? = null
        for (modelName in REST_MODELS) {
            try {
                val (responseText, errorMsg) = withTimeout(60_000L) {
                    val request = Request.Builder()
                        .url("$GEMINI_API_BASE_URL/$modelName:generateContent")
                        .addHeader("x-goog-api-key", apiKey)
                        .post(payloadJson.toRequestBody(JSON_MEDIA_TYPE))
                        .build()

                    sharedHttpClient.newCall(request).execute().use { response ->
                        val bodyStr = response.body?.string().orEmpty()
                        if (response.isSuccessful) {
                            extractTextFromGeminiJson(bodyStr) to null
                        } else {
                            val apiMessage = runCatching {
                                JSONObject(bodyStr).optJSONObject("error")?.optString("message")
                            }.getOrNull().orEmpty()
                            val summary = if (apiMessage.isNotBlank()) {
                                "HTTP ${response.code}: $apiMessage"
                            } else {
                                "HTTP ${response.code}"
                            }
                            Log.w(TAG, "Gemini REST request ($modelName) failed: $summary")
                            null to summary
                        }
                    }
                }
                if (!responseText.isNullOrBlank()) {
                    return@withContext responseText to null
                }
                if (!errorMsg.isNullOrBlank()) {
                    lastError = errorMsg
                }
            } catch (e: TimeoutCancellationException) {
                Log.w(TAG, "Gemini REST request ($modelName) timed out", e)
                lastError = "Request timed out"
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Gemini REST request ($modelName) error: ${e.message}")
                lastError = e.localizedMessage ?: "Network error"
            }
        }
        null to lastError
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
