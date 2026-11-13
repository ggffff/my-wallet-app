package com.example

import com.example.ui.dialogs.WalletDateUtils
import com.example.util.AppLanguage
import com.example.util.StringKeys
import com.example.util.formatBdt
import com.example.util.getString
import org.junit.Assert.assertEquals
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
}
