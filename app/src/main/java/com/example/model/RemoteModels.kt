package com.example.model

enum class RemoteButton(val label: String) {
    POWER("Power"),
    SETTINGS_123("123 / Settings"),
    VOICE("Voice"),
    DPAD_UP("Up"),
    DPAD_DOWN("Down"),
    DPAD_LEFT("Left"),
    DPAD_RIGHT("Right"),
    DPAD_OK("OK"),
    BACK("Back"),
    HOME("Home"),
    PLAY_PAUSE("Play / Pause"),
    VOL_UP("Volume +"),
    VOL_DOWN("Volume -"),
    MUTE("Mute"),
    CH_UP("Channel Up"),
    CH_DOWN("Channel Down"),
    CH_GUIDE("Channel Guide"),
    SAMSUNG_TV_PLUS("Samsung TV Plus"),
    NETFLIX("Netflix"),
    PRIME_VIDEO("Prime Video"),
    DISNEY_PLUS("Disney+"),
    CAST("Cast"),
    DIGIT_0("0"),
    DIGIT_1("1"),
    DIGIT_2("2"),
    DIGIT_3("3"),
    DIGIT_4("4"),
    DIGIT_5("5"),
    DIGIT_6("6"),
    DIGIT_7("7"),
    DIGIT_8("8"),
    DIGIT_9("9"),
    COLOR_RED("Red"),
    COLOR_GREEN("Green"),
    COLOR_YELLOW("Yellow"),
    COLOR_BLUE("Blue"),
    INPUT_SOURCE("Source"),
    SETTINGS_MENU("TV Settings")
}

data class BondedTvDevice(
    val name: String,
    val address: String,
    val isSamsungLikely: Boolean,
    val isBonded: Boolean
)

sealed interface HidConnectionState {
    data object Unsupported : HidConnectionState
    data object PermissionRequired : HidConnectionState
    data object BluetoothDisabled : HidConnectionState
    data object Registering : HidConnectionState
    data class ReadyToPair(val isDiscoverable: Boolean = false) : HidConnectionState
    data class Connecting(val deviceName: String) : HidConnectionState
    data class Connected(val device: BondedTvDevice) : HidConnectionState
    data class Error(val message: String) : HidConnectionState
}

sealed interface VoiceState {
    data object Idle : VoiceState
    data class Listening(val partialText: String = "") : VoiceState
    data class SendingText(val query: String) : VoiceState
    data class Error(val message: String) : VoiceState
}

data class DiscoveredTvStream(
    val name: String,
    val host: String,
    val port: Int,
    val streamUrl: String,
    val serviceType: String
)

sealed interface CastDirectionState {
    data object Idle : CastDirectionState
    data object ChoosingDirection : CastDirectionState
    data class PhoneToTvActive(
        val projectionActive: Boolean,
        val systemCastOpened: Boolean,
        val resolutionLabel: String = "1080p Mirror"
    ) : CastDirectionState
    data class TvToPhoneReceiver(
        val isDiscovering: Boolean = false,
        val discoveredStreams: List<DiscoveredTvStream> = emptyList(),
        val activeStreamUrl: String? = null,
        val activeStreamTitle: String? = null,
        val isPlaying: Boolean = false,
        val isBuffering: Boolean = false,
        val errorMessage: String? = null
    ) : CastDirectionState
}

data class RemoteUiState(
    val hidState: HidConnectionState = HidConnectionState.Registering,
    val bondedDevices: List<BondedTvDevice> = emptyList(),
    val discoveredDevices: List<BondedTvDevice> = emptyList(),
    val isScanningBluetooth: Boolean = false,
    val pressedButton: RemoteButton? = null,
    val lastCommandLabel: String? = null,
    val lastCommandSuccess: Boolean = true,
    val voiceState: VoiceState = VoiceState.Idle,
    val castState: CastDirectionState = CastDirectionState.Idle,
    val isKeypadSheetOpen: Boolean = false,
    val isBluetoothSheetOpen: Boolean = false,
    val appLaunchUsesVoiceSearchMacro: Boolean = true
)
