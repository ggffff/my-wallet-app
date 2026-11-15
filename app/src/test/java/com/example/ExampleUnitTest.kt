package com.example

import com.example.data.repository.AiChatRepository
import com.example.ui.dialogs.WalletDateUtils
import com.example.util.AppLanguage
import com.example.util.StringKeys
import com.example.util.formatBdt
import com.example.util.getString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun bdtFormatting_isCorrect() {
        val formatted = formatBdt(5000.0, AppLanguage.ENGLISH)
        assertTrue(formatted.contains("৳") && formatted.contains("5,000"))
    }

    @Test
    fun localization_isCorrect() {
        val enAppName = getString(StringKeys.APP_NAME, AppLanguage.ENGLISH)
        val bnAppName = getString(StringKeys.APP_NAME, AppLanguage.BANGLA)
        assertEquals("My Wallet", enAppName)
        assertEquals("আমার ওয়ালেট", bnAppName)
    }

    @Test
    fun walletDateUtils_roundTripsDateWithoutTimezoneShift() {
        val sampleDate = "2026-11-12"
        val millis = WalletDateUtils.parseDateToUtcMillis(sampleDate)
        assertNotNull(millis)
        assertEquals(sampleDate, WalletDateUtils.formatUtcMillisToDateStr(millis!!))
        assertNull(WalletDateUtils.parseDateToUtcMillis(""))
        assertNull(WalletDateUtils.parseDateToUtcMillis("invalid-date"))
    }

    @Test
    fun defaultLanguage_isEnglish() {
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromCode(null))
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromCode(""))
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromCode("en"))
        assertEquals(AppLanguage.BANGLADESH, AppLanguage.fromCode("bn"))
    }

    @Test
    fun geminiApiKeyGuard_rejectsBlankAndPlaceholderKeys() {
        assertFalse(AiChatRepository.isGeminiApiKeyConfigured(null))
        assertFalse(AiChatRepository.isGeminiApiKeyConfigured(""))
        assertFalse(AiChatRepository.isGeminiApiKeyConfigured("   "))
        assertFalse(AiChatRepository.isGeminiApiKeyConfigured(AiChatRepository.PLACEHOLDER_GEMINI_API_KEY))
        assertTrue(AiChatRepository.isGeminiApiKeyConfigured("test-configured-key"))
    }

    @Test
    fun sanitizeHistory_enforcesUserStartAndAlternatingTurns() {
        val raw = listOf(
            com.example.data.repository.ChatMessage(text = "Welcome!", isUser = false),
            com.example.data.repository.ChatMessage(text = "How much did I spend?", isUser = true),
            com.example.data.repository.ChatMessage(text = "You spent 500 BDT.", isUser = false),
            com.example.data.repository.ChatMessage(text = "Duplicate user turn", isUser = true),
            com.example.data.repository.ChatMessage(text = "Thinking...", isUser = false)
        )
        val sanitized = AiChatRepository.sanitizeHistory(raw)
        assertEquals(2, sanitized.size)
        assertTrue(sanitized[0].isUser)
        assertEquals("How much did I spend?", sanitized[0].text)
        assertFalse(sanitized[1].isUser)
        assertEquals("You spent 500 BDT.", sanitized[1].text)
    }
}
