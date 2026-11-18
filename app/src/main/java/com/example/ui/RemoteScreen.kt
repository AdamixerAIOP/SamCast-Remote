package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.BluetoothConnected
import androidx.compose.material.icons.rounded.CastConnected
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.CastDirectionState
import com.example.model.DiscoveredTvStream
import com.example.model.HidConnectionState
import com.example.model.RemoteButton
import com.example.model.RemoteUiState
import com.example.model.VoiceState
import com.example.ui.components.AppShortcutDiamond
import com.example.ui.components.BluetoothPairingSheet
import com.example.ui.components.CastActionButton
import com.example.ui.components.CastDirectionPopupDialog
import com.example.ui.components.DualRockerControl
import com.example.ui.components.Keypad123ColorSheet
import com.example.ui.components.NavigationMediaRow
import com.example.ui.components.PowerButton
import com.example.ui.components.SamsungDpadRing
import com.example.ui.components.Settings123Button
import com.example.ui.components.TvToPhoneReceiverDialog
import com.example.ui.components.VoiceMicButton
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.ButtonSurface
import com.example.ui.theme.PowerCrimson
import com.example.ui.theme.RemoteChassisBorder
import com.example.ui.theme.RemoteChassisBottom
import com.example.ui.theme.RemoteChassisTop
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioBackgroundRadial
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoiceEmerald

@Composable
fun RemoteScreen(
    uiState: RemoteUiState,
    onButtonPressStart: (RemoteButton) -> Unit,
    onButtonPressEnd: (RemoteButton) -> Unit,
    onSettings123Click: () -> Unit,
    onVoiceClick: () -> Unit,
    onCastClick: () -> Unit,
    onOpenBluetoothSheet: () -> Unit,
    onDismissBluetoothSheet: () -> Unit,
    onRequestBtPermissions: () -> Unit,
    onEnableBluetooth: () -> Unit,
    onMakeDiscoverable: () -> Unit,
    onScanBtDevices: () -> Unit,
    onConnectBtDevice: (String) -> Unit,
    onDisconnectBt: () -> Unit,
    onDismissKeypadSheet: () -> Unit,
    onToggleAppLaunchMacro: (Boolean) -> Unit,
    onSendCustomText: (String) -> Unit,
    onDismissCastPopupOrOverlay: () -> Unit,
    onChoosePhoneToTv: () -> Unit,
    onChooseTvToPhone: () -> Unit,
    onOpenSystemCastAgain: () -> Unit,
    onStopPhoneToTvCast: () -> Unit,
    onRescanTvStreams: () -> Unit,
    onSelectTvStream: (DiscoveredTvStream) -> Unit,
    onConnectCustomStreamUrl: (String) -> Unit,
    onPlaybackReady: () -> Unit,
    onPlaybackError: (String) -> Unit,
    onStopActiveTvStream: () -> Unit
) {
    val isCastActive = uiState.castState is CastDirectionState.PhoneToTvActive ||
        uiState.castState is CastDirectionState.TvToPhoneReceiver

    Scaffold(
        containerColor = StudioBackground,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(StudioBackgroundRadial, StudioBackground),
                        radius = 1200f
                    )
                )
                .padding(innerPadding)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                // Top Bluetooth Status Bar
                BluetoothStatusBar(
                    hidState = uiState.hidState,
                    onClick = onOpenBluetoothSheet,
                    modifier = Modifier.widthIn(max = 420.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Active Voice or Phone-to-TV Cast Status Strip
                AnimatedVisibility(
                    visible = uiState.voiceState !is VoiceState.Idle,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    VoiceStatusStrip(voiceState = uiState.voiceState)
                }

                val phoneCastState = uiState.castState as? CastDirectionState.PhoneToTvActive
                AnimatedVisibility(
                    visible = phoneCastState != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    if (phoneCastState != null) {
                        PhoneToTvActiveStrip(
                            resolutionLabel = phoneCastState.resolutionLabel,
                            onOpenSystemCast = onOpenSystemCastAgain,
                            onStopCast = onStopPhoneToTvCast
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Matte Black Samsung Smart Remote Chassis
                Box(
                    modifier = Modifier
                        .widthIn(min = 276.dp, max = 312.dp)
                        .fillMaxWidth()
                        .shadow(
                            elevation = 24.dp,
                            shape = RoundedCornerShape(40.dp),
                            ambientColor = Color.Black,
                            spotColor = Color.Black
                        )
                        .clip(RoundedCornerShape(40.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(RemoteChassisTop, RemoteChassisBottom)
                            )
                        )
                        .border(1.2.dp, RemoteChassisBorder, RoundedCornerShape(40.dp))
                        .padding(horizontal = 24.dp, vertical = 22.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Row 1: Power button top-left
                        Row(
                            horizontalArrangement = Arrangement.Start,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            PowerButton(
                                onPressStart = onButtonPressStart,
                                onPressEnd = onButtonPressEnd,
                                contentDescription = stringResource(R.string.cd_power)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Row 2: 123/Settings (left), Mic LED pinhole (center), Voice Mic (right)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Settings123Button(
                                onClick = onSettings123Click,
                                contentDescription = stringResource(R.string.cd_settings_123)
                            )

                            // Mic pinhole indicator matching physical remote photo
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (uiState.voiceState) {
                                            is VoiceState.Listening -> VoiceEmerald
                                            is VoiceState.SendingText -> AccentCyan
                                            else -> Color(0xFF0A0B0E)
                                        }
                                    )
                            )

                            VoiceMicButton(
                                isListening = uiState.voiceState is VoiceState.Listening,
                                onClick = onVoiceClick,
                                contentDescription = stringResource(R.string.cd_voice)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Row 3: Circular D-Pad + Center OK
                        SamsungDpadRing(
                            pressedButton = uiState.pressedButton,
                            onPressStart = onButtonPressStart,
                            onPressEnd = onButtonPressEnd,
                            cdUp = stringResource(R.string.cd_dpad_up),
                            cdDown = stringResource(R.string.cd_dpad_down),
                            cdLeft = stringResource(R.string.cd_dpad_left),
                            cdRight = stringResource(R.string.cd_dpad_right),
                            cdOk = stringResource(R.string.cd_dpad_ok)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Row 4: Back, Home, Play/Pause
                        NavigationMediaRow(
                            onPressStart = onButtonPressStart,
                            onPressEnd = onButtonPressEnd,
                            cdBack = stringResource(R.string.cd_back),
                            cdHome = stringResource(R.string.cd_home),
                            cdPlayPause = stringResource(R.string.cd_play_pause)
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Row 5: Dual Metallic Volume & Channel Rocker Bar
                        DualRockerControl(
                            onPressStart = onButtonPressStart,
                            onPressEnd = onButtonPressEnd,
                            cdVolUp = stringResource(R.string.cd_volume_up),
                            cdVolDown = stringResource(R.string.cd_volume_down),
                            cdMute = stringResource(R.string.cd_volume_mute),
                            cdChUp = stringResource(R.string.cd_channel_up),
                            cdChDown = stringResource(R.string.cd_channel_down),
                            cdGuide = stringResource(R.string.cd_channel_guide)
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Row 6: App Shortcut Diamond (Samsung TV Plus, Netflix, Prime Video, Disney+)
                        AppShortcutDiamond(
                            onPressStart = onButtonPressStart,
                            onPressEnd = onButtonPressEnd,
                            cdSamsungTvPlus = stringResource(R.string.cd_samsung_tv_plus),
                            cdNetflix = stringResource(R.string.cd_netflix),
                            cdPrimeVideo = stringResource(R.string.cd_prime_video),
                            cdDisneyPlus = stringResource(R.string.cd_disney_plus)
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Row 7: Dedicated Cast Button (triggers Phone <-> TV popup)
                        CastActionButton(
                            isCastActive = isCastActive,
                            onClick = onCastClick,
                            contentDescription = stringResource(R.string.cd_cast_button)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Bottom Hardware Wordmark matching photo
                        Text(
                            text = "SAMSUNG",
                            color = Color(0xFF9CA3AF),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 3.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Subtle command feedback footer
                uiState.lastCommandLabel?.let { feedback ->
                    Surface(
                        color = ButtonSurface.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("text_last_command")
                    ) {
                        Text(
                            text = feedback,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (uiState.lastCommandSuccess) AccentCyan else TextSecondary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        )
                    }
                }
            }
        }
    }

    // Popups & Bottom Sheets
    when (val castState = uiState.castState) {
        is CastDirectionState.ChoosingDirection -> {
            CastDirectionPopupDialog(
                onDismiss = onDismissCastPopupOrOverlay,
                onCastPhoneToTv = onChoosePhoneToTv,
                onCastTvToPhone = onChooseTvToPhone
            )
        }
        is CastDirectionState.TvToPhoneReceiver -> {
            TvToPhoneReceiverDialog(
                state = castState,
                onDismiss = onDismissCastPopupOrOverlay,
                onRescan = onRescanTvStreams,
                onSelectStream = onSelectTvStream,
                onConnectCustomUrl = onConnectCustomStreamUrl,
                onPlaybackReady = onPlaybackReady,
                onPlaybackError = onPlaybackError,
                onStopStream = onStopActiveTvStream
            )
        }
        else -> {}
    }

    if (uiState.isBluetoothSheetOpen) {
        BluetoothPairingSheet(
            hidState = uiState.hidState,
            bondedDevices = uiState.bondedDevices,
            discoveredDevices = uiState.discoveredDevices,
            isScanning = uiState.isScanningBluetooth,
            onDismiss = onDismissBluetoothSheet,
            onRequestPermissions = onRequestBtPermissions,
            onEnableBluetooth = onEnableBluetooth,
            onMakeDiscoverable = onMakeDiscoverable,
            onScanDevices = onScanBtDevices,
            onConnectDevice = onConnectBtDevice,
            onDisconnect = onDisconnectBt
        )
    }

    if (uiState.isKeypadSheetOpen) {
        Keypad123ColorSheet(
            appLaunchUsesVoiceSearchMacro = uiState.appLaunchUsesVoiceSearchMacro,
            onToggleAppLaunchMacro = onToggleAppLaunchMacro,
            onPressStart = onButtonPressStart,
            onPressEnd = onButtonPressEnd,
            onSendCustomText = onSendCustomText,
            onDismiss = onDismissKeypadSheet
        )
    }
}

@Composable
private fun BluetoothStatusBar(
    hidState: HidConnectionState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (label, dotColor, isConnected) = when (hidState) {
        is HidConnectionState.Connected -> Triple(
            stringResource(R.string.bt_status_connected, hidState.device.name),
            VoiceEmerald,
            true
        )
        is HidConnectionState.Connecting -> Triple(
            stringResource(R.string.bt_status_connecting, hidState.deviceName),
            AccentCyan,
            false
        )
        is HidConnectionState.ReadyToPair -> Triple(
            if (hidState.isDiscoverable) "Discoverable • Pair from TV" else stringResource(R.string.bt_status_ready),
            AccentCyan,
            false
        )
        is HidConnectionState.Registering -> Triple(
            stringResource(R.string.bt_status_registering),
            AccentCyan,
            false
        )
        is HidConnectionState.BluetoothDisabled -> Triple(
            stringResource(R.string.bt_status_disabled),
            PowerCrimson,
            false
        )
        is HidConnectionState.PermissionRequired -> Triple(
            stringResource(R.string.bt_status_permission_needed),
            PowerCrimson,
            false
        )
        is HidConnectionState.Unsupported -> Triple(
            stringResource(R.string.bt_status_unsupported),
            PowerCrimson,
            false
        )
        is HidConnectionState.Error -> Triple(
            hidState.message,
            PowerCrimson,
            false
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(ButtonSurface.copy(alpha = 0.9f))
            .border(1.dp, RemoteChassisBorder, RoundedCornerShape(24.dp))
            .testTag("btn_bluetooth_status")
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(Modifier.width(10.dp))
            Icon(
                imageVector = if (isConnected) Icons.Rounded.BluetoothConnected else Icons.Rounded.Bluetooth,
                contentDescription = null,
                tint = dotColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = if (isConnected) "Manage" else "Pair TV",
            style = MaterialTheme.typography.labelSmall,
            color = AccentCyan
        )
    }
}

@Composable
private fun VoiceStatusStrip(voiceState: VoiceState) {
    val text = when (voiceState) {
        is VoiceState.Listening -> voiceState.partialText.ifBlank { stringResource(R.string.voice_listening) }
        is VoiceState.SendingText -> "Sending \"${voiceState.query}\" to TV…"
        is VoiceState.Error -> voiceState.message
        VoiceState.Idle -> ""
    }
    Surface(
        color = VoiceEmerald.copy(alpha = 0.14f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .widthIn(max = 420.dp)
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Icon(Icons.Rounded.GraphicEq, contentDescription = null, tint = VoiceEmerald, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(text = text, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
        }
    }
}

@Composable
private fun PhoneToTvActiveStrip(
    resolutionLabel: String,
    onOpenSystemCast: () -> Unit,
    onStopCast: () -> Unit
) {
    Surface(
        color = AccentCyan.copy(alpha = 0.14f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .widthIn(max = 420.dp)
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .border(1.dp, AccentCyan.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(Icons.Rounded.CastConnected, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("Phone → TV Active", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                    Text(resolutionLabel, style = MaterialTheme.typography.labelSmall, color = AccentCyan)
                }
            }
            TextButton(onClick = onOpenSystemCast) {
                Text("Select TV", color = AccentCyan)
            }
            OutlinedButton(
                onClick = onStopCast,
                modifier = Modifier.testTag("btn_stop_phone_cast")
            ) {
                Icon(Icons.Rounded.Stop, contentDescription = null, tint = PowerCrimson, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Stop", color = PowerCrimson)
            }
        }
    }
}
