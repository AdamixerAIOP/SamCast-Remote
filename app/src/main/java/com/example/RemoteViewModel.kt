package com.example

import android.app.Application
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluetooth.BluetoothHidController
import com.example.cast.CastDiscoveryController
import com.example.cast.CastForegroundService
import com.example.model.CastDirectionState
import com.example.model.DiscoveredTvStream
import com.example.model.HidConnectionState
import com.example.model.RemoteButton
import com.example.model.RemoteUiState
import com.example.model.VoiceState
import com.example.voice.VoiceInputManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RemoteViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application.applicationContext
    val hidController = BluetoothHidController(appContext)
    val castDiscovery = CastDiscoveryController(appContext)
    val voiceManager: VoiceInputManager = VoiceInputManager(appContext) { spokenText ->
        val sent = hidController.typeTextAndSubmit(spokenText, prefixWithSearch = true)
        _localState.update {
            it.copy(
                lastCommandLabel = if (sent) "Voice: \"$spokenText\"" else "Connect TV to send \"$spokenText\"",
                lastCommandSuccess = sent
            )
        }
        viewModelScope.launch {
            delay(1800L)
            voiceManager.resetState()
        }
    }

    private val _localState = MutableStateFlow(RemoteUiState())
    val uiState: StateFlow<RemoteUiState> = _localState.asStateFlow()

    init {
        hidController.initialize()

        viewModelScope.launch {
            combine(
                hidController.connectionState,
                hidController.bondedDevices,
                hidController.discoveredDevices,
                hidController.isScanning,
                voiceManager.voiceState
            ) { hidState, bonded, discovered, scanning, voice ->
                Tuple5(hidState, bonded, discovered, scanning, voice)
            }.collect { (hidState, bonded, discovered, scanning, voice) ->
                _localState.update { current ->
                    current.copy(
                        hidState = hidState,
                        bondedDevices = bonded,
                        discoveredDevices = discovered,
                        isScanningBluetooth = scanning,
                        voiceState = voice
                    )
                }
            }
        }

        viewModelScope.launch {
            combine(
                CastForegroundService.isCasting,
                CastForegroundService.resolutionLabel,
                castDiscovery.streams,
                castDiscovery.isDiscovering
            ) { isCasting, resLabel, streams, discovering ->
                Quad(isCasting, resLabel, streams, discovering)
            }.collect { (isCasting, resLabel, streams, discovering) ->
                _localState.update { current ->
                    val updatedCast = when (val cs = current.castState) {
                        is CastDirectionState.PhoneToTvActive -> {
                            if (!isCasting && cs.projectionActive) {
                                CastDirectionState.Idle
                            } else {
                                cs.copy(projectionActive = isCasting, resolutionLabel = resLabel)
                            }
                        }
                        is CastDirectionState.TvToPhoneReceiver -> cs.copy(
                            isDiscovering = discovering,
                            discoveredStreams = streams
                        )
                        else -> if (isCasting) {
                            CastDirectionState.PhoneToTvActive(
                                projectionActive = true,
                                systemCastOpened = false,
                                resolutionLabel = resLabel
                            )
                        } else {
                            cs
                        }
                    }
                    current.copy(castState = updatedCast)
                }
            }
        }
    }

    fun onBluetoothPermissionsResult() {
        hidController.initialize()
    }

    fun onButtonPressStart(button: RemoteButton) {
        if (button == RemoteButton.CAST) {
            openCastPopup()
            return
        }
        if (button == RemoteButton.SETTINGS_123) {
            _localState.update { it.copy(pressedButton = button) }
            return
        }
        val sent = hidController.sendPress(button, _localState.value.appLaunchUsesVoiceSearchMacro)
        _localState.update {
            it.copy(
                pressedButton = button,
                lastCommandLabel = if (sent) {
                    "Sent: ${button.label}"
                } else {
                    "${button.label} • Pair TV over Bluetooth to control"
                },
                lastCommandSuccess = sent
            )
        }
    }

    fun onButtonPressEnd(button: RemoteButton) {
        _localState.update { state ->
            if (state.pressedButton == button) state.copy(pressedButton = null) else state
        }
        if (button == RemoteButton.CAST || button == RemoteButton.SETTINGS_123) return
        hidController.sendRelease(button)
    }

    fun onSettings123Click() {
        hidController.sendPress(RemoteButton.SETTINGS_123, false)
        hidController.sendRelease(RemoteButton.SETTINGS_123)
        _localState.update { it.copy(isKeypadSheetOpen = true, lastCommandLabel = "123 / Color Keypad") }
    }

    fun dismissKeypadSheet() {
        _localState.update { it.copy(isKeypadSheetOpen = false) }
    }

    fun toggleAppLaunchMacro(enabled: Boolean) {
        _localState.update { it.copy(appLaunchUsesVoiceSearchMacro = enabled) }
    }

    fun openBluetoothSheet() {
        hidController.refreshBondedDevices()
        _localState.update { it.copy(isBluetoothSheetOpen = true) }
    }

    fun dismissBluetoothSheet() {
        hidController.stopDiscovery()
        _localState.update { it.copy(isBluetoothSheetOpen = false) }
    }

    fun startBluetoothScan() {
        hidController.startDiscovery()
    }

    fun connectToDevice(address: String) {
        hidController.connect(address)
    }

    fun disconnectDevice() {
        hidController.disconnect()
    }

    fun sendCustomTextToTv(text: String) {
        if (text.isBlank()) return
        val sent = hidController.typeTextAndSubmit(text.trim(), prefixWithSearch = false)
        _localState.update {
            it.copy(
                lastCommandLabel = if (sent) "Typed: \"$text\"" else "Pair TV to send text",
                lastCommandSuccess = sent
            )
        }
    }

    fun startVoiceInput() {
        voiceManager.startListening()
    }

    fun stopVoiceInput() {
        voiceManager.stopListening()
    }

    // ---- Bidirectional Cast Flows ----

    fun openCastPopup() {
        _localState.update { it.copy(castState = CastDirectionState.ChoosingDirection) }
    }

    fun dismissCastPopupOrOverlay() {
        castDiscovery.stopTvStreamDiscovery()
        _localState.update { it.copy(castState = CastDirectionState.Idle) }
    }

    fun onPhoneToTvProjectionGranted(resultCode: Int, data: Intent) {
        val startIntent = CastForegroundService.createStartIntent(appContext, resultCode, data)
        ContextCompat.startForegroundService(appContext, startIntent)
        val pickerOpened = castDiscovery.openSystemCastPicker()
        _localState.update {
            it.copy(
                castState = CastDirectionState.PhoneToTvActive(
                    projectionActive = true,
                    systemCastOpened = pickerOpened,
                    resolutionLabel = CastForegroundService.resolutionLabel.value
                ),
                lastCommandLabel = "Casting Phone → TV",
                lastCommandSuccess = true
            )
        }
    }

    fun onPhoneToTvProjectionCancelled() {
        _localState.update {
            it.copy(
                castState = CastDirectionState.Idle,
                lastCommandLabel = "Phone → TV cast cancelled",
                lastCommandSuccess = false
            )
        }
    }

    fun openSystemCastSettingsAgain() {
        castDiscovery.openSystemCastPicker()
    }

    fun stopPhoneToTvCast() {
        appContext.startService(CastForegroundService.createStopIntent(appContext))
        _localState.update {
            it.copy(
                castState = CastDirectionState.Idle,
                lastCommandLabel = "Stopped Phone → TV cast",
                lastCommandSuccess = true
            )
        }
    }

    fun startTvToPhoneReceiver() {
        castDiscovery.startTvStreamDiscovery()
        _localState.update {
            it.copy(
                castState = CastDirectionState.TvToPhoneReceiver(
                    isDiscovering = true,
                    discoveredStreams = castDiscovery.streams.value
                ),
                lastCommandLabel = "Listening for TV → Phone stream",
                lastCommandSuccess = true
            )
        }
    }

    fun selectTvStream(stream: DiscoveredTvStream) {
        _localState.update { current ->
            val receiver = current.castState as? CastDirectionState.TvToPhoneReceiver
                ?: CastDirectionState.TvToPhoneReceiver()
            current.copy(
                castState = receiver.copy(
                    activeStreamUrl = stream.streamUrl,
                    activeStreamTitle = stream.name,
                    isBuffering = true,
                    isPlaying = false,
                    errorMessage = null
                )
            )
        }
    }

    fun connectCustomStreamUrl(url: String, title: String = "Samsung TV Stream") {
        val normalized = url.trim()
        if (normalized.isEmpty()) return
        _localState.update { current ->
            val receiver = current.castState as? CastDirectionState.TvToPhoneReceiver
                ?: CastDirectionState.TvToPhoneReceiver()
            current.copy(
                castState = receiver.copy(
                    activeStreamUrl = normalized,
                    activeStreamTitle = title,
                    isBuffering = true,
                    isPlaying = false,
                    errorMessage = null
                )
            )
        }
    }

    fun onStreamPlaybackReady() {
        _localState.update { current ->
            val receiver = current.castState as? CastDirectionState.TvToPhoneReceiver ?: return@update current
            current.copy(
                castState = receiver.copy(isBuffering = false, isPlaying = true, errorMessage = null)
            )
        }
    }

    fun onStreamPlaybackError(message: String) {
        _localState.update { current ->
            val receiver = current.castState as? CastDirectionState.TvToPhoneReceiver ?: return@update current
            current.copy(
                castState = receiver.copy(isBuffering = false, isPlaying = false, errorMessage = message)
            )
        }
    }

    fun stopActiveTvStream() {
        _localState.update { current ->
            val receiver = current.castState as? CastDirectionState.TvToPhoneReceiver ?: return@update current
            current.copy(
                castState = receiver.copy(
                    activeStreamUrl = null,
                    activeStreamTitle = null,
                    isBuffering = false,
                    isPlaying = false,
                    errorMessage = null
                )
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        castDiscovery.stopTvStreamDiscovery()
        voiceManager.release()
        hidController.release()
    }

    private data class Tuple5<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)
    private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
}
