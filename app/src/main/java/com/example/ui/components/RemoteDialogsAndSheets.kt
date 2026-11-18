package com.example.ui.components

import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.BluetoothConnected
import androidx.compose.material.icons.rounded.BluetoothSearching
import androidx.compose.material.icons.rounded.Cast
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Input
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.model.BondedTvDevice
import com.example.model.CastDirectionState
import com.example.model.DiscoveredTvStream
import com.example.model.HidConnectionState
import com.example.model.RemoteButton
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.ButtonHighlight
import com.example.ui.theme.ButtonSurface
import com.example.ui.theme.DotBlue
import com.example.ui.theme.DotGreen
import com.example.ui.theme.DotRed
import com.example.ui.theme.DotYellow
import com.example.ui.theme.PowerCrimson
import com.example.ui.theme.RemoteChassisBottom
import com.example.ui.theme.RemoteChassisTop
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoiceEmerald

@Composable
fun CastDirectionPopupDialog(
    onDismiss: () -> Unit,
    onCastPhoneToTv: () -> Unit,
    onCastTvToPhone: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = RemoteChassisTop,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, RemoteChassisTop.copy(alpha = 0.8f), RoundedCornerShape(24.dp))
                .border(1.dp, AccentCyan.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Cast,
                            contentDescription = null,
                            tint = AccentCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.cast_popup_title),
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextPrimary
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("btn_cast_popup_close")) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.cast_popup_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                CastDirectionOptionCard(
                    title = stringResource(R.string.cast_phone_to_tv_title),
                    description = stringResource(R.string.cast_phone_to_tv_desc),
                    leadingIcon = Icons.Rounded.PhoneAndroid,
                    trailingIcon = Icons.Rounded.Tv,
                    accentColor = AccentCyan,
                    testTag = "btn_cast_phone_to_tv",
                    onClick = onCastPhoneToTv
                )

                CastDirectionOptionCard(
                    title = stringResource(R.string.cast_tv_to_phone_title),
                    description = stringResource(R.string.cast_tv_to_phone_desc),
                    leadingIcon = Icons.Rounded.Tv,
                    trailingIcon = Icons.Rounded.PhoneAndroid,
                    accentColor = VoiceEmerald,
                    testTag = "btn_cast_tv_to_phone",
                    onClick = onCastTvToPhone
                )

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun CastDirectionOptionCard(
    title: String,
    description: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.horizontalGradient(
                    listOf(ButtonSurface, Color(0xFF1A1D24))
                )
            )
            .border(1.dp, accentColor.copy(alpha = 0.45f), shape)
            .testTag(testTag)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(accentColor.copy(alpha = 0.14f))
                .padding(horizontal = 10.dp, vertical = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(leadingIcon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                Text(" → ", color = accentColor, fontWeight = FontWeight.Bold)
                Icon(trailingIcon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun TvToPhoneReceiverDialog(
    state: CastDirectionState.TvToPhoneReceiver,
    onDismiss: () -> Unit,
    onRescan: () -> Unit,
    onSelectStream: (DiscoveredTvStream) -> Unit,
    onConnectCustomUrl: (String) -> Unit,
    onPlaybackReady: () -> Unit,
    onPlaybackError: (String) -> Unit,
    onStopStream: () -> Unit
) {
    var manualUrl by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = StudioBackground,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .border(1.dp, AccentCyan.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Tv,
                            contentDescription = null,
                            tint = VoiceEmerald
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TV → Phone Receiver",
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextPrimary
                        )
                    }
                    Row {
                        IconButton(onClick = onRescan, modifier = Modifier.testTag("btn_rescan_streams")) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = "Rescan TV streams",
                                tint = AccentCyan
                            )
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.testTag("btn_close_tv_receiver")) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close receiver",
                                tint = TextSecondary
                            )
                        }
                    }
                }

                // Active Video Player Surface or Idle Preview
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black)
                        .border(1.dp, ButtonHighlight, RoundedCornerShape(16.dp))
                ) {
                    val streamUrl = state.activeStreamUrl
                    if (streamUrl != null) {
                        AndroidView(
                            factory = { ctx ->
                                VideoView(ctx).apply {
                                    val controller = MediaController(ctx)
                                    controller.setAnchorView(this)
                                    setMediaController(controller)
                                    setOnPreparedListener { mp ->
                                        mp.isLooping = false
                                        onPlaybackReady()
                                        start()
                                    }
                                    setOnErrorListener { _, what, extra ->
                                        onPlaybackError("Unable to play stream (code $what/$extra). Verify TV stream endpoint.")
                                        true
                                    }
                                    setVideoURI(Uri.parse(streamUrl))
                                }
                            },
                            update = { view ->
                                if (view.tag != streamUrl) {
                                    view.tag = streamUrl
                                    view.setVideoURI(Uri.parse(streamUrl))
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                        if (state.isBuffering) {
                            CircularProgressIndicator(color = VoiceEmerald)
                        }
                        if (state.errorMessage != null) {
                            Text(
                                text = state.errorMessage,
                                color = PowerCrimson,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.78f))
                                    .padding(12.dp)
                            )
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Tv,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (state.isDiscovering) {
                                    "Scanning local Wi-Fi for TV / DLNA / RTSP streams…"
                                } else {
                                    "Select a discovered TV stream or enter a local stream URL"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }

                if (state.activeStreamUrl != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = state.activeStreamTitle ?: state.activeStreamUrl,
                            style = MaterialTheme.typography.titleSmall,
                            color = VoiceEmerald,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedButton(
                            onClick = onStopStream,
                            modifier = Modifier.testTag("btn_stop_tv_stream")
                        ) {
                            Icon(Icons.Rounded.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Stop")
                        }
                    }
                }

                // Direct LAN stream URL input (RTSP / HLS / HTTP)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = manualUrl,
                        onValueChange = { manualUrl = it },
                        placeholder = { Text("http://tv-ip:port/stream.m3u8 or rtsp://...") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_tv_stream_url")
                    )
                    Button(
                        onClick = { onConnectCustomUrl(manualUrl) },
                        enabled = manualUrl.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = VoiceEmerald),
                        modifier = Modifier.testTag("btn_connect_stream_url")
                    ) {
                        Icon(Icons.Rounded.PlayArrow, contentDescription = "Play Stream")
                    }
                }

                if (state.discoveredStreams.isNotEmpty()) {
                    Text(
                        text = "Discovered Network Endpoints",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextSecondary
                    )
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(140.dp)
                    ) {
                        items(state.discoveredStreams, key = { it.streamUrl }) { stream ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ButtonSurface)
                                    .clickable { onSelectStream(stream) }
                                    .padding(12.dp)
                            ) {
                                Icon(Icons.Rounded.Tv, contentDescription = null, tint = AccentCyan)
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(stream.name, color = TextPrimary, style = MaterialTheme.typography.titleSmall)
                                    Text(stream.streamUrl, color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BluetoothPairingSheet(
    hidState: HidConnectionState,
    bondedDevices: List<BondedTvDevice>,
    discoveredDevices: List<BondedTvDevice>,
    isScanning: Boolean,
    onDismiss: () -> Unit,
    onRequestPermissions: () -> Unit,
    onEnableBluetooth: () -> Unit,
    onMakeDiscoverable: () -> Unit,
    onScanDevices: () -> Unit,
    onConnectDevice: (String) -> Unit,
    onDisconnect: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = RemoteChassisBottom,
        contentColor = TextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when (hidState) {
                        is HidConnectionState.Connected -> Icons.Rounded.BluetoothConnected
                        is HidConnectionState.Connecting,
                        is HidConnectionState.Registering -> Icons.Rounded.BluetoothSearching
                        else -> Icons.Rounded.Bluetooth
                    },
                    contentDescription = null,
                    tint = AccentCyan
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = stringResource(R.string.bt_sheet_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary
                    )
                    Text(
                        text = stringResource(R.string.bt_sheet_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            when (hidState) {
                is HidConnectionState.PermissionRequired -> {
                    Button(
                        onClick = onRequestPermissions,
                        modifier = Modifier.fillMaxWidth().testTag("btn_bt_grant_permission")
                    ) {
                        Text(stringResource(R.string.bt_grant_permissions))
                    }
                }
                is HidConnectionState.BluetoothDisabled -> {
                    Button(
                        onClick = onEnableBluetooth,
                        modifier = Modifier.fillMaxWidth().testTag("btn_bt_enable")
                    ) {
                        Text(stringResource(R.string.bt_enable_bluetooth))
                    }
                }
                is HidConnectionState.Connected -> {
                    Surface(
                        color = AccentCyan.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Column {
                                Text("Connected TV", style = MaterialTheme.typography.labelSmall, color = AccentCyan)
                                Text(hidState.device.name, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            }
                            OutlinedButton(
                                onClick = onDisconnect,
                                modifier = Modifier.testTag("btn_bt_disconnect")
                            ) {
                                Text(stringResource(R.string.bt_disconnect), color = PowerCrimson)
                            }
                        }
                    }
                }
                else -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = onMakeDiscoverable,
                            modifier = Modifier.weight(1f).testTag("btn_bt_discoverable")
                        ) {
                            Text(stringResource(R.string.bt_make_discoverable), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        OutlinedButton(
                            onClick = onScanDevices,
                            modifier = Modifier.weight(1f).testTag("btn_bt_scan")
                        ) {
                            Text(
                                if (isScanning) "Scanning…" else stringResource(R.string.bt_scan_devices),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = ButtonHighlight.copy(alpha = 0.4f))

            Text(
                text = stringResource(R.string.bt_bonded_devices),
                style = MaterialTheme.typography.titleSmall,
                color = TextSecondary
            )

            if (bondedDevices.isEmpty() && discoveredDevices.isEmpty()) {
                Text(
                    text = stringResource(R.string.bt_no_devices),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(220.dp)
                ) {
                    items(bondedDevices, key = { "bonded_${it.address}" }) { dev ->
                        DeviceRow(device = dev, onClick = { onConnectDevice(dev.address) })
                    }
                    if (discoveredDevices.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.bt_discovered_devices),
                                style = MaterialTheme.typography.titleSmall,
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                        items(discoveredDevices, key = { "disc_${it.address}" }) { dev ->
                            DeviceRow(device = dev, onClick = { onConnectDevice(dev.address) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceRow(
    device: BondedTvDevice,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(ButtonSurface)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
                imageVector = if (device.isSamsungLikely) Icons.Rounded.Tv else Icons.Rounded.Bluetooth,
                contentDescription = null,
                tint = if (device.isSamsungLikely) AccentCyan else TextSecondary
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(device.name, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                Text(device.address, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
        }
        Text(
            text = if (device.isBonded) "Connect" else "Pair",
            style = MaterialTheme.typography.labelLarge,
            color = AccentCyan
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Keypad123ColorSheet(
    appLaunchUsesVoiceSearchMacro: Boolean,
    onToggleAppLaunchMacro: (Boolean) -> Unit,
    onPressStart: (RemoteButton) -> Unit,
    onPressEnd: (RemoteButton) -> Unit,
    onSendCustomText: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var textInput by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = RemoteChassisBottom,
        contentColor = TextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(R.string.keypad_sheet_title),
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary
            )

            // 4 Color Dots Row (Red, Green, Yellow, Blue)
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(
                    Triple(RemoteButton.COLOR_RED, DotRed, "A"),
                    Triple(RemoteButton.COLOR_GREEN, DotGreen, "B"),
                    Triple(RemoteButton.COLOR_YELLOW, DotYellow, "C"),
                    Triple(RemoteButton.COLOR_BLUE, DotBlue, "D")
                ).forEach { (btn, color, label) ->
                    TactileRemoteButton(
                        button = btn,
                        contentDescription = btn.label,
                        testTag = "btn_color_${btn.name.lowercase()}",
                        onPressStart = onPressStart,
                        onPressEnd = onPressEnd,
                        size = 52.dp,
                        glowColor = color
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(color)
                        ) {
                            Text(label, color = Color.Black, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // Numeric Keypad 1..9 + Source / 0 / Settings
            val rows = listOf(
                listOf(RemoteButton.DIGIT_1, RemoteButton.DIGIT_2, RemoteButton.DIGIT_3),
                listOf(RemoteButton.DIGIT_4, RemoteButton.DIGIT_5, RemoteButton.DIGIT_6),
                listOf(RemoteButton.DIGIT_7, RemoteButton.DIGIT_8, RemoteButton.DIGIT_9)
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                rows.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        row.forEach { digit ->
                            TactileRemoteButton(
                                button = digit,
                                contentDescription = "Number ${digit.label}",
                                testTag = "btn_${digit.name.lowercase()}",
                                onPressStart = onPressStart,
                                onPressEnd = onPressEnd,
                                size = 56.dp
                            ) {
                                Text(digit.label, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            }
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TactileRemoteButton(
                        button = RemoteButton.INPUT_SOURCE,
                        contentDescription = "Input Source",
                        testTag = "btn_input_source",
                        onPressStart = onPressStart,
                        onPressEnd = onPressEnd,
                        size = 56.dp
                    ) {
                        Icon(Icons.Rounded.Input, contentDescription = null, tint = AccentCyan)
                    }
                    TactileRemoteButton(
                        button = RemoteButton.DIGIT_0,
                        contentDescription = "Number 0",
                        testTag = "btn_digit_0",
                        onPressStart = onPressStart,
                        onPressEnd = onPressEnd,
                        size = 56.dp
                    ) {
                        Text("0", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    }
                    TactileRemoteButton(
                        button = RemoteButton.SETTINGS_MENU,
                        contentDescription = "TV Settings Menu",
                        testTag = "btn_tv_settings",
                        onPressStart = onPressStart,
                        onPressEnd = onPressEnd,
                        size = 56.dp
                    ) {
                        Icon(Icons.Rounded.Settings, contentDescription = null, tint = TextPrimary)
                    }
                }
            }

            // Direct HID Keyboard Text Typing
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = { Text("Type text to TV keyboard…") },
                    singleLine = true,
                    modifier = Modifier.weight(1f).testTag("input_hid_text")
                )
                Button(
                    onClick = {
                        onSendCustomText(textInput)
                        textInput = ""
                    },
                    enabled = textInput.isNotBlank(),
                    modifier = Modifier.testTag("btn_send_hid_text")
                ) {
                    Text("Send")
                }
            }

            // App launch macro toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Smart Search App Launch", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                    Text(
                        "Types app name via HID Search for Netflix, Prime & Disney+",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
                Switch(
                    checked = appLaunchUsesVoiceSearchMacro,
                    onCheckedChange = onToggleAppLaunchMacro
                )
            }
        }
    }
}
