package com.example.bluetooth

import com.example.model.RemoteButton

sealed interface HidAction {
    data class Consumer(val usageId: Int) : HidAction
    data class Keyboard(val modifier: Byte, val keyCode: Byte) : HidAction
    data class AppSearchLaunch(val appName: String, val fallbackUsageId: Int) : HidAction
}

object HidKeyMapper {

    const val REPORT_ID_CONSUMER: Int = 1
    const val REPORT_ID_KEYBOARD: Int = 2

    /**
     * Composite HID Descriptor supporting:
     *  - Report ID 1: Consumer Control (16-bit usages: Power, Vol+/-, Mute, Ch+/-, Home, Back, D-Pad, Play/Pause, Search)
     *  - Report ID 2: Standard 8-byte Keyboard (Digits 0-9, F1-F4 Color Keys, Voice-to-text typing)
     */
    val HID_REPORT_DESCRIPTOR: ByteArray = byteArrayOf(
        // Report ID 1: Consumer Control
        0x05.toByte(), 0x0C.toByte(), // Usage Page (Consumer)
        0x09.toByte(), 0x01.toByte(), // Usage (Consumer Control)
        0xA1.toByte(), 0x01.toByte(), // Collection (Application)
        0x85.toByte(), REPORT_ID_CONSUMER.toByte(), // Report ID (1)
        0x15.toByte(), 0x00.toByte(), // Logical Minimum (0)
        0x26.toByte(), 0xFF.toByte(), 0x03.toByte(), // Logical Maximum (1023)
        0x19.toByte(), 0x00.toByte(), // Usage Minimum (0)
        0x2A.toByte(), 0xFF.toByte(), 0x03.toByte(), // Usage Maximum (1023)
        0x75.toByte(), 0x10.toByte(), // Report Size (16)
        0x95.toByte(), 0x01.toByte(), // Report Count (1)
        0x81.toByte(), 0x00.toByte(), // Input (Data, Array, Absolute)
        0xC0.toByte(),                // End Collection

        // Report ID 2: Keyboard
        0x05.toByte(), 0x01.toByte(), // Usage Page (Generic Desktop)
        0x09.toByte(), 0x06.toByte(), // Usage (Keyboard)
        0xA1.toByte(), 0x01.toByte(), // Collection (Application)
        0x85.toByte(), REPORT_ID_KEYBOARD.toByte(), // Report ID (2)
        0x05.toByte(), 0x07.toByte(), // Usage Page (Keyboard/Keypad)
        0x19.toByte(), 0xE0.toByte(), // Usage Minimum (Keyboard LeftControl)
        0x29.toByte(), 0xE7.toByte(), // Usage Maximum (Keyboard Right GUI)
        0x15.toByte(), 0x00.toByte(), // Logical Minimum (0)
        0x25.toByte(), 0x01.toByte(), // Logical Maximum (1)
        0x75.toByte(), 0x01.toByte(), // Report Size (1)
        0x95.toByte(), 0x08.toByte(), // Report Count (8)
        0x81.toByte(), 0x02.toByte(), // Input (Data, Variable, Absolute) - Modifier byte
        0x95.toByte(), 0x01.toByte(), // Report Count (1)
        0x75.toByte(), 0x08.toByte(), // Report Size (8)
        0x81.toByte(), 0x01.toByte(), // Input (Constant) - Reserved byte
        0x95.toByte(), 0x06.toByte(), // Report Count (6)
        0x75.toByte(), 0x08.toByte(), // Report Size (8)
        0x15.toByte(), 0x00.toByte(), // Logical Minimum (0)
        0x25.toByte(), 0x65.toByte(), // Logical Maximum (101)
        0x05.toByte(), 0x07.toByte(), // Usage Page (Keyboard/Keypad)
        0x19.toByte(), 0x00.toByte(), // Usage Minimum (0)
        0x29.toByte(), 0x65.toByte(), // Usage Maximum (101)
        0x81.toByte(), 0x00.toByte(), // Input (Data, Array) - Keycodes
        0xC0.toByte()                 // End Collection
    )

    // Consumer Control Usage IDs (USB HID Usage Tables 1.12, Page 0x0C)
    const val USAGE_POWER = 0x0030
    const val USAGE_MENU = 0x0040
    const val USAGE_MENU_PICK = 0x0041
    const val USAGE_MENU_UP = 0x0042
    const val USAGE_MENU_DOWN = 0x0043
    const val USAGE_MENU_LEFT = 0x0044
    const val USAGE_MENU_RIGHT = 0x0045
    const val USAGE_MEDIA_SELECT_TV = 0x0089
    const val USAGE_MEDIA_SELECT_GUIDE = 0x008D
    const val USAGE_CHANNEL_INC = 0x009C
    const val USAGE_CHANNEL_DEC = 0x009D
    const val USAGE_PLAY_PAUSE = 0x00CD
    const val USAGE_MUTE = 0x00E2
    const val USAGE_VOLUME_INC = 0x00E9
    const val USAGE_VOLUME_DEC = 0x00EA
    const val USAGE_AL_MEDIA_PLAYER = 0x0183
    const val USAGE_AL_INTERNET_BROWSER = 0x0196
    const val USAGE_AC_SEARCH = 0x0221
    const val USAGE_AC_HOME = 0x0223
    const val USAGE_AC_BACK = 0x0224

    // Keyboard HID Usage IDs (Page 0x07)
    const val KEY_ENTER: Byte = 0x28
    const val KEY_ESC: Byte = 0x29
    const val KEY_SPACE: Byte = 0x2C
    const val KEY_1: Byte = 0x1E
    const val KEY_2: Byte = 0x1F
    const val KEY_3: Byte = 0x20
    const val KEY_4: Byte = 0x21
    const val KEY_5: Byte = 0x22
    const val KEY_6: Byte = 0x23
    const val KEY_7: Byte = 0x24
    const val KEY_8: Byte = 0x25
    const val KEY_9: Byte = 0x26
    const val KEY_0: Byte = 0x27
    const val KEY_F1: Byte = 0x3A
    const val KEY_F2: Byte = 0x3B
    const val KEY_F3: Byte = 0x3C
    const val KEY_F4: Byte = 0x3D

    fun mapButton(button: RemoteButton): HidAction? = when (button) {
        RemoteButton.POWER -> HidAction.Consumer(USAGE_POWER)
        RemoteButton.SETTINGS_123 -> HidAction.Consumer(USAGE_MENU)
        RemoteButton.SETTINGS_MENU -> HidAction.Consumer(USAGE_MENU)
        RemoteButton.VOICE -> HidAction.Consumer(USAGE_AC_SEARCH)
        RemoteButton.DPAD_UP -> HidAction.Consumer(USAGE_MENU_UP)
        RemoteButton.DPAD_DOWN -> HidAction.Consumer(USAGE_MENU_DOWN)
        RemoteButton.DPAD_LEFT -> HidAction.Consumer(USAGE_MENU_LEFT)
        RemoteButton.DPAD_RIGHT -> HidAction.Consumer(USAGE_MENU_RIGHT)
        RemoteButton.DPAD_OK -> HidAction.Consumer(USAGE_MENU_PICK)
        RemoteButton.BACK -> HidAction.Consumer(USAGE_AC_BACK)
        RemoteButton.HOME -> HidAction.Consumer(USAGE_AC_HOME)
        RemoteButton.PLAY_PAUSE -> HidAction.Consumer(USAGE_PLAY_PAUSE)
        RemoteButton.VOL_UP -> HidAction.Consumer(USAGE_VOLUME_INC)
        RemoteButton.VOL_DOWN -> HidAction.Consumer(USAGE_VOLUME_DEC)
        RemoteButton.MUTE -> HidAction.Consumer(USAGE_MUTE)
        RemoteButton.CH_UP -> HidAction.Consumer(USAGE_CHANNEL_INC)
        RemoteButton.CH_DOWN -> HidAction.Consumer(USAGE_CHANNEL_DEC)
        RemoteButton.CH_GUIDE -> HidAction.Consumer(USAGE_MEDIA_SELECT_GUIDE)
        RemoteButton.SAMSUNG_TV_PLUS -> HidAction.AppSearchLaunch("Samsung TV Plus", USAGE_AL_MEDIA_PLAYER)
        RemoteButton.NETFLIX -> HidAction.AppSearchLaunch("Netflix", USAGE_AL_INTERNET_BROWSER)
        RemoteButton.PRIME_VIDEO -> HidAction.AppSearchLaunch("Prime Video", USAGE_AL_MEDIA_PLAYER)
        RemoteButton.DISNEY_PLUS -> HidAction.AppSearchLaunch("Disney+", USAGE_AL_MEDIA_PLAYER)
        RemoteButton.DIGIT_0 -> HidAction.Keyboard(0, KEY_0)
        RemoteButton.DIGIT_1 -> HidAction.Keyboard(0, KEY_1)
        RemoteButton.DIGIT_2 -> HidAction.Keyboard(0, KEY_2)
        RemoteButton.DIGIT_3 -> HidAction.Keyboard(0, KEY_3)
        RemoteButton.DIGIT_4 -> HidAction.Keyboard(0, KEY_4)
        RemoteButton.DIGIT_5 -> HidAction.Keyboard(0, KEY_5)
        RemoteButton.DIGIT_6 -> HidAction.Keyboard(0, KEY_6)
        RemoteButton.DIGIT_7 -> HidAction.Keyboard(0, KEY_7)
        RemoteButton.DIGIT_8 -> HidAction.Keyboard(0, KEY_8)
        RemoteButton.DIGIT_9 -> HidAction.Keyboard(0, KEY_9)
        RemoteButton.COLOR_RED -> HidAction.Keyboard(0, KEY_F1)
        RemoteButton.COLOR_GREEN -> HidAction.Keyboard(0, KEY_F2)
        RemoteButton.COLOR_YELLOW -> HidAction.Keyboard(0, KEY_F3)
        RemoteButton.COLOR_BLUE -> HidAction.Keyboard(0, KEY_F4)
        RemoteButton.INPUT_SOURCE -> HidAction.Consumer(USAGE_MEDIA_SELECT_TV)
        RemoteButton.CAST -> null
    }

    fun consumerBytes(usageId: Int): ByteArray = byteArrayOf(
        (usageId and 0xFF).toByte(),
        ((usageId ushr 8) and 0xFF).toByte()
    )

    val CONSUMER_RELEASE = byteArrayOf(0x00, 0x00)

    fun keyboardBytes(modifier: Byte, keyCode: Byte): ByteArray = byteArrayOf(
        modifier, 0x00, keyCode, 0x00, 0x00, 0x00, 0x00, 0x00
    )

    val KEYBOARD_RELEASE = ByteArray(8) { 0x00 }

    fun charToKeyboardKey(ch: Char): Pair<Byte, Byte>? {
        return when (ch) {
            in 'a'..'z' -> 0.toByte() to (0x04 + (ch - 'a')).toByte()
            in 'A'..'Z' -> 0x02.toByte() to (0x04 + (ch - 'A')).toByte()
            in '1'..'9' -> 0.toByte() to (0x1E + (ch - '1')).toByte()
            '0' -> 0.toByte() to KEY_0
            ' ' -> 0.toByte() to KEY_SPACE
            '-' -> 0.toByte() to 0x2D.toByte()
            '+' -> 0x02.toByte() to 0x2E.toByte()
            '.' -> 0.toByte() to 0x37.toByte()
            '/' -> 0.toByte() to 0x38.toByte()
            else -> null
        }
    }
}
