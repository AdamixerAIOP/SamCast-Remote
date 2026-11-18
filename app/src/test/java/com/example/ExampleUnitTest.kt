package com.example

import com.example.bluetooth.HidAction
import com.example.bluetooth.HidKeyMapper
import com.example.model.RemoteButton
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun allRemoteHardwareButtonsHaveHidMappings() {
        val hardwareButtons = RemoteButton.entries.filter { it != RemoteButton.CAST }
        for (btn in hardwareButtons) {
            assertNotNull("Missing HID mapping for $btn", HidKeyMapper.mapButton(btn))
        }
    }

    @Test
    fun consumerAndKeyboardByteEncodingsMatchUsbHidSpec() {
        val power = HidKeyMapper.mapButton(RemoteButton.POWER) as HidAction.Consumer
        assertEquals(0x0030, power.usageId)
        assertArrayEquals(byteArrayOf(0x30, 0x00), HidKeyMapper.consumerBytes(power.usageId))

        val volUp = HidKeyMapper.mapButton(RemoteButton.VOL_UP) as HidAction.Consumer
        assertArrayEquals(byteArrayOf(0xE9.toByte(), 0x00), HidKeyMapper.consumerBytes(volUp.usageId))

        val digit1 = HidKeyMapper.mapButton(RemoteButton.DIGIT_1) as HidAction.Keyboard
        assertEquals(HidKeyMapper.KEY_1, digit1.keyCode)

        val charA = HidKeyMapper.charToKeyboardKey('A')
        assertNotNull(charA)
        assertTrue(charA!!.first == 0x02.toByte())
    }
}
