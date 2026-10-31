package com.example.ui.home

import android.content.res.Configuration
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.core.os.ConfigurationCompat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TimeGreetingTest {

    @Test
    fun `test morning phase and greetings (5 to 11)`() {
        for (hour in 5..11) {
            val phase = getTimePhase(hour)
            assertEquals("Hour $hour should be MORNING", TimePhase.MORNING, phase)
            assertEquals("Hour $hour English greeting", "Good Morning", getTimeGreeting(phase, isBengali = false))
            assertEquals("Hour $hour Bengali greeting", "শুভ সকাল", getTimeGreeting(phase, isBengali = true))
            assertEquals(Icons.Filled.LightMode, phase.icon)
        }
    }

    @Test
    fun `test noon phase and greetings (12 to 14)`() {
        for (hour in 12..14) {
            val phase = getTimePhase(hour)
            assertEquals("Hour $hour should be NOON", TimePhase.NOON, phase)
            assertEquals("Hour $hour English greeting", "Good Afternoon", getTimeGreeting(phase, isBengali = false))
            assertEquals("Hour $hour Bengali greeting", "শুভ দুপুর", getTimeGreeting(phase, isBengali = true))
            assertEquals(Icons.Filled.WbSunny, phase.icon)
        }
    }

    @Test
    fun `test afternoon phase and greetings (15 to 17)`() {
        for (hour in 15..17) {
            val phase = getTimePhase(hour)
            assertEquals("Hour $hour should be AFTERNOON", TimePhase.AFTERNOON, phase)
            assertEquals("Hour $hour English greeting", "Good Afternoon", getTimeGreeting(phase, isBengali = false))
            assertEquals("Hour $hour Bengali greeting", "শুভ বিকেল", getTimeGreeting(phase, isBengali = true))
            assertEquals(Icons.Filled.WbSunny, phase.icon)
        }
    }

    @Test
    fun `test evening phase and greetings (18 to 21)`() {
        for (hour in 18..21) {
            val phase = getTimePhase(hour)
            assertEquals("Hour $hour should be EVENING", TimePhase.EVENING, phase)
            assertEquals("Hour $hour English greeting", "Good Evening", getTimeGreeting(phase, isBengali = false))
            assertEquals("Hour $hour Bengali greeting", "শুভ সন্ধ্যা", getTimeGreeting(phase, isBengali = true))
            assertEquals(Icons.Filled.WbTwilight, phase.icon)
        }
    }

    @Test
    fun `test night phase and greetings (22 to 23 and 0 to 4)`() {
        val nightHours = listOf(22, 23, 0, 1, 2, 3, 4)
        for (hour in nightHours) {
            val phase = getTimePhase(hour)
            assertEquals("Hour $hour should be NIGHT", TimePhase.NIGHT, phase)
            assertEquals("Hour $hour English greeting", "Good Night", getTimeGreeting(phase, isBengali = false))
            assertEquals("Hour $hour Bengali greeting", "শুভ রাত্রি", getTimeGreeting(phase, isBengali = true))
            assertEquals(Icons.Filled.NightsStay, phase.icon)
        }
    }

    @Test
    fun `test all 24 hours covered with exact greeting rules`() {
        for (h in 0..23) {
            val phase = getTimePhase(h)
            val en = getTimeGreeting(phase, isBengali = false)
            val bn = getTimeGreeting(phase, isBengali = true)

            when (h) {
                in 5..11 -> {
                    assertEquals(TimePhase.MORNING, phase)
                    assertEquals("Good Morning", en)
                    assertEquals("শুভ সকাল", bn)
                }
                in 12..14 -> {
                    assertEquals(TimePhase.NOON, phase)
                    assertEquals("Good Afternoon", en)
                    assertEquals("শুভ দুপুর", bn)
                }
                in 15..17 -> {
                    assertEquals(TimePhase.AFTERNOON, phase)
                    assertEquals("Good Afternoon", en)
                    assertEquals("শুভ বিকেল", bn)
                }
                in 18..21 -> {
                    assertEquals(TimePhase.EVENING, phase)
                    assertEquals("Good Evening", en)
                    assertEquals("শুভ সন্ধ্যা", bn)
                }
                else -> {
                    assertEquals(TimePhase.NIGHT, phase)
                    assertEquals("Good Night", en)
                    assertEquals("শুভ রাত্রি", bn)
                }
            }
        }
    }

    @Test
    fun `test system language detection for Bengali and English`() {
        // Bengali locale
        val bnLocale = Locale.forLanguageTag("bn-BD")
        assertTrue("Bengali locale should be detected as Bengali", isSystemBengali(bnLocale))

        val configBn = Configuration()
        configBn.setLocale(bnLocale)
        assertTrue("Configuration with Bengali locale should be detected as Bengali", isSystemBengali(configBn))

        // English locale
        val enLocale = Locale.forLanguageTag("en-US")
        assertFalse("English locale should not be detected as Bengali", isSystemBengali(enLocale))

        val configEn = Configuration()
        configEn.setLocale(enLocale)
        assertFalse("Configuration with English locale should not be detected as Bengali", isSystemBengali(configEn))

        // Other languages fallback to non-Bengali (English)
        val frLocale = Locale.forLanguageTag("fr-FR")
        assertFalse("French locale should not be detected as Bengali", isSystemBengali(frLocale))
    }
}
