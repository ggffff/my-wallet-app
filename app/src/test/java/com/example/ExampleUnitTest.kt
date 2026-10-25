package com.example

import com.example.util.AppLanguage
import com.example.util.StringKeys
import com.example.util.formatBdt
import com.example.util.getString
import org.junit.Assert.assertEquals
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
}
