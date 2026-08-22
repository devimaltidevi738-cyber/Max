package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.engine.AuraNluParser
import com.example.model.ActionCategory
import com.example.model.ConfirmationLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("MAX", appName)
    }

    @Test
    fun `test MAX identity and intro response`() {
        val parser = AuraNluParser()
        val introIntent = parser.parse("tum kaun ho")
        assertEquals(ActionCategory.SYSTEM_AUTOMATION, introIntent.category)
        assertEquals("ASSISTANT_INTRO", introIntent.actionName)
        assertEquals("Ji, main MAX hoon. Boliye.", introIntent.spokenResponseHindi)
    }

    @Test
    fun `test NLU parsing for Hindi navigation and app commands`() {
        val parser = AuraNluParser()

        val navIntent = parser.parse("Home pe jao")
        assertEquals(ActionCategory.NAVIGATION, navIntent.category)
        assertEquals("NAV_HOME", navIntent.actionName)

        val appIntent = parser.parse("WhatsApp kholo")
        assertEquals(ActionCategory.APPS, appIntent.category)
        assertEquals("WhatsApp", appIntent.primaryParam)

        val alarmIntent = parser.parse("Kal subah 7 baje ka alarm lagao")
        assertEquals(ActionCategory.ALARM_TIMER, alarmIntent.category)
        assertEquals("SET_ALARM", alarmIntent.actionName)

        val callIntent = parser.parse("Rahul ko call karo")
        assertEquals(ActionCategory.CALLS, callIntent.category)
        assertEquals(ConfirmationLevel.LEVEL_2_IMPORTANT, callIntent.confirmationLevel)
    }

    @Test
    fun `test multi-step command parsing`() {
        val parser = AuraNluParser()
        val multiIntent = parser.parse("Volume 50 percent karo aur YouTube kholo")
        assertTrue(multiIntent.isMultiStep)
        assertEquals(2, multiIntent.subIntents.size)
        assertEquals(ActionCategory.AUDIO_VOLUME, multiIntent.subIntents[0].category)
        assertEquals(ActionCategory.APPS, multiIntent.subIntents[1].category)
    }

    @Test
    fun `test Call Control Hindi and English voice commands`() {
        val parser = AuraNluParser()

        // 1. Answer Call
        val answer1 = parser.parse("MAX, call receive karo")
        assertEquals("ANSWER_CALL", answer1.actionName)
        assertEquals(ActionCategory.CALLS, answer1.category)

        val answer2 = parser.parse("Call utha lo")
        assertEquals("ANSWER_CALL", answer2.actionName)

        // 2. Reject Call
        val reject1 = parser.parse("MAX, call reject karo")
        assertEquals("REJECT_CALL", reject1.actionName)
        assertEquals(ActionCategory.CALLS, reject1.category)

        val reject2 = parser.parse("Call reject kar do")
        assertEquals("REJECT_CALL", reject2.actionName)

        // 3. End Call
        val endCall = parser.parse("Call kaat do")
        assertEquals("END_CALL", endCall.actionName)
        assertEquals(ActionCategory.CALLS, endCall.category)

        // 4. Speakerphone Control
        val speakerOn = parser.parse("Speaker on karo")
        assertEquals("SPEAKER_ON", speakerOn.actionName)

        val speakerOff = parser.parse("Speaker off karo")
        assertEquals("SPEAKER_OFF", speakerOff.actionName)
    }
}
