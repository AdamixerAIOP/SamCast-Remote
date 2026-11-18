package com.example

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.RemoteScreen
import com.example.ui.theme.SamCastRemoteTheme

class MainActivity : ComponentActivity() {

    private val viewModel: RemoteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SamCastRemoteTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                val btPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) {
                    viewModel.onBluetoothPermissionsResult()
                }

                val enableBluetoothLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) {
                    viewModel.onBluetoothPermissionsResult()
                }

                val discoverableLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) {
                    viewModel.onBluetoothPermissionsResult()
                }

                val audioPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { granted ->
                    if (granted) {
                        viewModel.startVoiceInput()
                    }
                }

                val mediaProjectionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                        viewModel.onPhoneToTvProjectionGranted(result.resultCode, result.data!!)
                    } else {
                        viewModel.onPhoneToTvProjectionCancelled()
                    }
                }

                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) {
                    val mpm = getSystemService(MediaProjectionManager::class.java)
                    if (mpm != null) {
                        mediaProjectionLauncher.launch(mpm.createScreenCaptureIntent())
                    }
                }

                val launchPhoneToTvCast: () -> Unit = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        val mpm = getSystemService(MediaProjectionManager::class.java)
                        if (mpm != null) {
                            mediaProjectionLauncher.launch(mpm.createScreenCaptureIntent())
                        }
                    }
                }

                RemoteScreen(
                    uiState = uiState,
                    onButtonPressStart = viewModel::onButtonPressStart,
                    onButtonPressEnd = viewModel::onButtonPressEnd,
                    onSettings123Click = viewModel::onSettings123Click,
                    onVoiceClick = {
                        if (viewModel.voiceManager.hasRecordAudioPermission()) {
                            viewModel.startVoiceInput()
                        } else {
                            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    onCastClick = viewModel::openCastPopup,
                    onOpenBluetoothSheet = {
                        if (!viewModel.hidController.hasBluetoothPermissions()) {
                            btPermissionLauncher.launch(viewModel.hidController.requiredPermissions())
                        }
                        viewModel.openBluetoothSheet()
                    },
                    onDismissBluetoothSheet = viewModel::dismissBluetoothSheet,
                    onRequestBtPermissions = {
                        btPermissionLauncher.launch(viewModel.hidController.requiredPermissions())
                    },
                    onEnableBluetooth = {
                        enableBluetoothLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                    },
                    onMakeDiscoverable = {
                        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 180)
                        }
                        discoverableLauncher.launch(intent)
                    },
                    onScanBtDevices = viewModel::startBluetoothScan,
                    onConnectBtDevice = viewModel::connectToDevice,
                    onDisconnectBt = viewModel::disconnectDevice,
                    onDismissKeypadSheet = viewModel::dismissKeypadSheet,
                    onToggleAppLaunchMacro = viewModel::toggleAppLaunchMacro,
                    onSendCustomText = viewModel::sendCustomTextToTv,
                    onDismissCastPopupOrOverlay = viewModel::dismissCastPopupOrOverlay,
                    onChoosePhoneToTv = launchPhoneToTvCast,
                    onChooseTvToPhone = viewModel::startTvToPhoneReceiver,
                    onOpenSystemCastAgain = viewModel::openSystemCastSettingsAgain,
                    onStopPhoneToTvCast = viewModel::stopPhoneToTvCast,
                    onRescanTvStreams = viewModel::startTvToPhoneReceiver,
                    onSelectTvStream = viewModel::selectTvStream,
                    onConnectCustomStreamUrl = viewModel::connectCustomStreamUrl,
                    onPlaybackReady = viewModel::onStreamPlaybackReady,
                    onPlaybackError = viewModel::onStreamPlaybackError,
                    onStopActiveTvStream = viewModel::stopActiveTvStream
                )
            }
        }
    }
}
