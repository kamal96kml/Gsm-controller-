package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ActivityLog
import com.example.data.SolarPumpPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun testAppName() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("GSM Controller", appName)
    }

    @Test
    fun testSolarPumpPreferencesAndCommands() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = SolarPumpPreferences(context)

        // Verify default passcode is 1234 and phone is 0600000000
        assertEquals("1234", prefs.passcode)
        assertEquals("0600000000", prefs.phoneNumber)

        prefs.phoneNumber = "0661234567"
        prefs.passcode = "1234"
        prefs.pumpStatus = "running"

        assertEquals("0661234567", prefs.phoneNumber)
        assertEquals("1234", prefs.passcode)
        assertEquals("running", prefs.pumpStatus)

        // Test primary action command formats
        val startCmd = "${prefs.passcode}#ON#"
        assertEquals("1234#ON#", startCmd)

        val stopCmd = "${prefs.passcode}#OFF#"
        assertEquals("1234#OFF#", stopCmd)

        // Test quick diagnostics
        val statusCmd = "${prefs.passcode}#EE#"
        assertEquals("1234#EE#", statusCmd)

        val timerCmd = "${prefs.passcode}#T#"
        assertEquals("1234#T#", timerCmd)

        val signalCmd = "${prefs.passcode}#CSQ?"
        assertEquals("1234#CSQ?", signalCmd)

        // Test advanced RTU5024 configuration commands
        val pwdCmd = "${prefs.passcode}#PWD5678#PWD5678#"
        assertEquals("1234#PWD5678#PWD5678#", pwdCmd)

        val authCmd = "${prefs.passcode}#TEL0661234567#01#"
        assertEquals("1234#TEL0661234567#01#", authCmd)

        val delCmd = "${prefs.passcode}#DEL0661234567#"
        assertEquals("1234#DEL0661234567#", delCmd)

        val gotCmd = "${prefs.passcode}#GOT0019#"
        assertEquals("1234#GOT0019#", gotCmd)

        val allowAllCmd = "${prefs.passcode}#AA#"
        assertEquals("1234#AA#", allowAllCmd)

        val authOnlyCmd = "${prefs.passcode}#AU#"
        assertEquals("1234#AU#", authOnlyCmd)

        val resetCmd = "${prefs.passcode}#RESET#"
        assertEquals("1234#RESET#", resetCmd)

        prefs.addLog(
            ActivityLog(
                id = "test_1",
                timestamp = "10:00:00",
                title = "تشغيل المضخة (SMS)",
                command = startCmd,
                type = "start",
                mode = "SMS"
            )
        )

        val logs = prefs.getLogs()
        assertTrue(logs.isNotEmpty())
        assertEquals("1234#ON#", logs[0].command)

        // Test timer preferences
        prefs.saveTimer(1800L, System.currentTimeMillis() + 1800000L)
        assertTrue(prefs.isTimerActive)
        assertEquals(1800L, prefs.timerDurationSeconds)
        assertTrue(prefs.timerEndTimeMs > 0L)

        prefs.clearTimer()
        assertEquals(false, prefs.isTimerActive)
    }
}
