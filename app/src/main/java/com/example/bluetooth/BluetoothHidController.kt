package com.example.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppQosSettings
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.example.model.BondedTvDevice
import com.example.model.HidConnectionState
import com.example.model.RemoteButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

class BluetoothHidController(
    private val context: Context
) {
    companion object {
        private const val TAG = "BluetoothHidController"
        private const val HID_NAME = "SamCast Smart Remote"
        private const val HID_DESCRIPTION = "Bluetooth Smart TV Remote"
        private const val HID_PROVIDER = "SamCast"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val executor = Executors.newSingleThreadExecutor()

    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(BluetoothManager::class.java)
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private var hidDevice: BluetoothHidDevice? = null
    private var activeDevice: BluetoothDevice? = null
    private var isAppRegistered = false
    private var receiverRegistered = false

    private val _connectionState = MutableStateFlow<HidConnectionState>(HidConnectionState.Registering)
    val connectionState: StateFlow<HidConnectionState> = _connectionState.asStateFlow()

    private val _bondedDevices = MutableStateFlow<List<BondedTvDevice>>(emptyList())
    val bondedDevices: StateFlow<List<BondedTvDevice>> = _bondedDevices.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<BondedTvDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<BondedTvDevice>> = _discoveredDevices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val rawDiscoveredMap = linkedMapOf<String, BluetoothDevice>()

    private val btReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(ctx: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                    if (state == BluetoothAdapter.STATE_ON) {
                        initialize()
                    } else if (state == BluetoothAdapter.STATE_OFF) {
                        activeDevice = null
                        _connectionState.value = HidConnectionState.BluetoothDisabled
                    }
                }
                BluetoothAdapter.ACTION_SCAN_MODE_CHANGED -> {
                    val mode = intent.getIntExtra(
                        BluetoothAdapter.EXTRA_SCAN_MODE,
                        BluetoothAdapter.SCAN_MODE_NONE
                    )
                    val current = _connectionState.value
                    if (current is HidConnectionState.ReadyToPair) {
                        _connectionState.value = HidConnectionState.ReadyToPair(
                            isDiscoverable = mode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE
                        )
                    }
                }
                BluetoothDevice.ACTION_FOUND -> {
                    if (!hasBluetoothPermissions()) return
                    val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }
                    device?.let { dev ->
                        val addr = dev.address ?: return
                        rawDiscoveredMap[addr] = dev
                        _discoveredDevices.value = rawDiscoveredMap.values.map { it.toBondedTvDevice() }
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    _isScanning.value = false
                }
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    refreshBondedDevices()
                    val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }
                    val bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE)
                    if (bondState == BluetoothDevice.BOND_BONDED && device != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        connect(device.address)
                    }
                }
            }
        }
    }

    fun requiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE
            )
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    fun hasBluetoothPermissions(): Boolean {
        return requiredPermissions().all { perm ->
            ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled == true

    @SuppressLint("MissingPermission")
    fun initialize() {
        registerReceiverIfNeeded()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            _connectionState.value = HidConnectionState.Unsupported
            return
        }
        if (bluetoothAdapter == null) {
            _connectionState.value = HidConnectionState.Unsupported
            return
        }
        if (!hasBluetoothPermissions()) {
            _connectionState.value = HidConnectionState.PermissionRequired
            return
        }
        if (!bluetoothAdapter.isEnabled) {
            _connectionState.value = HidConnectionState.BluetoothDisabled
            return
        }

        refreshBondedDevices()

        if (hidDevice != null && isAppRegistered) {
            if (activeDevice != null) {
                _connectionState.value = HidConnectionState.Connected(activeDevice!!.toBondedTvDevice())
            } else {
                _connectionState.value = HidConnectionState.ReadyToPair(
                    isDiscoverable = bluetoothAdapter.scanMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE
                )
            }
            return
        }

        _connectionState.value = HidConnectionState.Registering
        bluetoothAdapter.getProfileProxy(context, serviceListener, BluetoothProfile.HID_DEVICE)
    }

    @SuppressLint("MissingPermission")
    fun refreshBondedDevices() {
        if (!hasBluetoothPermissions() || bluetoothAdapter == null) {
            _bondedDevices.value = emptyList()
            return
        }
        val bonded = bluetoothAdapter.bondedDevices
            ?.map { it.toBondedTvDevice() }
            ?.sortedWith(compareByDescending<BondedTvDevice> { it.isSamsungLikely }.thenBy { it.name })
            .orEmpty()
        _bondedDevices.value = bonded
    }

    @SuppressLint("MissingPermission")
    fun startDiscovery() {
        if (!hasBluetoothPermissions() || bluetoothAdapter == null || !bluetoothAdapter.isEnabled) return
        rawDiscoveredMap.clear()
        _discoveredDevices.value = emptyList()
        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }
        _isScanning.value = bluetoothAdapter.startDiscovery()
    }

    @SuppressLint("MissingPermission")
    fun stopDiscovery() {
        if (!hasBluetoothPermissions() || bluetoothAdapter == null) return
        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }
        _isScanning.value = false
    }

    @SuppressLint("MissingPermission")
    fun connect(address: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return
        if (!hasBluetoothPermissions() || bluetoothAdapter == null) {
            _connectionState.value = HidConnectionState.PermissionRequired
            return
        }
        val device = bluetoothAdapter.bondedDevices?.firstOrNull { it.address == address }
            ?: rawDiscoveredMap[address]
            ?: return

        stopDiscovery()

        if (device.bondState != BluetoothDevice.BOND_BONDED) {
            _connectionState.value = HidConnectionState.Connecting(device.safeName())
            device.createBond()
            return
        }

        val proxy = hidDevice
        if (proxy == null || !isAppRegistered) {
            _connectionState.value = HidConnectionState.Error("HID profile not registered yet")
            initialize()
            return
        }

        _connectionState.value = HidConnectionState.Connecting(device.safeName())
        val initiated = proxy.connect(device)
        if (!initiated) {
            _connectionState.value = HidConnectionState.Error("Unable to initiate connection to ${device.safeName()}")
        }
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return
        val device = activeDevice ?: return
        hidDevice?.disconnect(device)
        activeDevice = null
        _connectionState.value = HidConnectionState.ReadyToPair(
            isDiscoverable = bluetoothAdapter?.scanMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE
        )
    }

    @SuppressLint("MissingPermission")
    fun sendPress(button: RemoteButton, useAppSearchMacro: Boolean): Boolean {
        val action = HidKeyMapper.mapButton(button) ?: return false
        return when (action) {
            is HidAction.Consumer -> sendConsumerPress(action.usageId)
            is HidAction.Keyboard -> sendKeyboardPress(action.modifier, action.keyCode)
            is HidAction.AppSearchLaunch -> {
                if (useAppSearchMacro) {
                    launchAppViaSearchMacro(action.appName)
                } else {
                    sendConsumerPress(action.fallbackUsageId)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun sendRelease(button: RemoteButton) {
        val action = HidKeyMapper.mapButton(button) ?: return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return
        val proxy = hidDevice ?: return
        val device = activeDevice ?: return
        if (!hasBluetoothPermissions()) return

        when (action) {
            is HidAction.Consumer -> proxy.sendReport(
                device,
                HidKeyMapper.REPORT_ID_CONSUMER,
                HidKeyMapper.CONSUMER_RELEASE
            )
            is HidAction.Keyboard -> proxy.sendReport(
                device,
                HidKeyMapper.REPORT_ID_KEYBOARD,
                HidKeyMapper.KEYBOARD_RELEASE
            )
            is HidAction.AppSearchLaunch -> {
                proxy.sendReport(
                    device,
                    HidKeyMapper.REPORT_ID_CONSUMER,
                    HidKeyMapper.CONSUMER_RELEASE
                )
            }
        }
    }

    fun typeTextAndSubmit(query: String, prefixWithSearch: Boolean = true): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return false
        val device = activeDevice ?: return false
        val proxy = hidDevice ?: return false
        if (!hasBluetoothPermissions()) return false

        scope.launch {
            if (prefixWithSearch) {
                tapConsumer(proxy, device, HidKeyMapper.USAGE_AC_SEARCH)
                delay(420L)
            }
            for (ch in query) {
                val mapped = HidKeyMapper.charToKeyboardKey(ch) ?: continue
                tapKeyboard(proxy, device, mapped.first, mapped.second)
                delay(28L)
            }
            delay(80L)
            tapKeyboard(proxy, device, 0, HidKeyMapper.KEY_ENTER)
        }
        return true
    }

    private fun launchAppViaSearchMacro(appName: String): Boolean {
        return typeTextAndSubmit(appName, prefixWithSearch = true)
    }

    @SuppressLint("MissingPermission")
    private fun sendConsumerPress(usageId: Int): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return false
        val proxy = hidDevice ?: return false
        val device = activeDevice ?: return false
        if (!hasBluetoothPermissions()) return false
        return proxy.sendReport(
            device,
            HidKeyMapper.REPORT_ID_CONSUMER,
            HidKeyMapper.consumerBytes(usageId)
        )
    }

    @SuppressLint("MissingPermission")
    private fun sendKeyboardPress(modifier: Byte, keyCode: Byte): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return false
        val proxy = hidDevice ?: return false
        val device = activeDevice ?: return false
        if (!hasBluetoothPermissions()) return false
        return proxy.sendReport(
            device,
            HidKeyMapper.REPORT_ID_KEYBOARD,
            HidKeyMapper.keyboardBytes(modifier, keyCode)
        )
    }

    @SuppressLint("MissingPermission")
    private suspend fun tapConsumer(proxy: BluetoothHidDevice, device: BluetoothDevice, usageId: Int) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return
        proxy.sendReport(device, HidKeyMapper.REPORT_ID_CONSUMER, HidKeyMapper.consumerBytes(usageId))
        delay(30L)
        proxy.sendReport(device, HidKeyMapper.REPORT_ID_CONSUMER, HidKeyMapper.CONSUMER_RELEASE)
    }

    @SuppressLint("MissingPermission")
    private suspend fun tapKeyboard(
        proxy: BluetoothHidDevice,
        device: BluetoothDevice,
        modifier: Byte,
        keyCode: Byte
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return
        proxy.sendReport(device, HidKeyMapper.REPORT_ID_KEYBOARD, HidKeyMapper.keyboardBytes(modifier, keyCode))
        delay(24L)
        proxy.sendReport(device, HidKeyMapper.REPORT_ID_KEYBOARD, HidKeyMapper.KEYBOARD_RELEASE)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private val serviceListener = object : BluetoothProfile.ServiceListener {
        @SuppressLint("MissingPermission")
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
            if (profile != BluetoothProfile.HID_DEVICE || proxy !is BluetoothHidDevice) return
            hidDevice = proxy
            val sdp = BluetoothHidDeviceAppSdpSettings(
                HID_NAME,
                HID_DESCRIPTION,
                HID_PROVIDER,
                BluetoothHidDevice.SUBCLASS1_COMBO,
                HidKeyMapper.HID_REPORT_DESCRIPTOR
            )
            val outQos = BluetoothHidDeviceAppQosSettings(
                BluetoothHidDeviceAppQosSettings.SERVICE_BEST_EFFORT,
                800,
                9,
                0,
                11250,
                BluetoothHidDeviceAppQosSettings.MAX
            )
            val registered = proxy.registerApp(sdp, null, outQos, executor, hidCallback)
            if (!registered) {
                _connectionState.value = HidConnectionState.Error("Failed to register HID remote profile")
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                hidDevice = null
                activeDevice = null
                isAppRegistered = false
                _connectionState.value = HidConnectionState.Registering
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private val hidCallback = object : BluetoothHidDevice.Callback() {
        @SuppressLint("MissingPermission")
        override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
            isAppRegistered = registered
            if (registered) {
                if (pluggedDevice != null) {
                    activeDevice = pluggedDevice
                    _connectionState.value = HidConnectionState.Connected(pluggedDevice.toBondedTvDevice())
                } else {
                    _connectionState.value = HidConnectionState.ReadyToPair(
                        isDiscoverable = bluetoothAdapter?.scanMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE
                    )
                }
            } else {
                activeDevice = null
                _connectionState.value = HidConnectionState.Error("Bluetooth HID remote unregistered")
            }
        }

        @SuppressLint("MissingPermission")
        override fun onConnectionStateChanged(device: BluetoothDevice?, state: Int) {
            when (state) {
                BluetoothProfile.STATE_CONNECTED -> {
                    activeDevice = device
                    if (device != null) {
                        _connectionState.value = HidConnectionState.Connected(device.toBondedTvDevice())
                    }
                }
                BluetoothProfile.STATE_CONNECTING -> {
                    if (device != null) {
                        _connectionState.value = HidConnectionState.Connecting(device.safeName())
                    }
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    if (activeDevice?.address == device?.address) {
                        activeDevice = null
                        _connectionState.value = HidConnectionState.ReadyToPair(
                            isDiscoverable = bluetoothAdapter?.scanMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE
                        )
                    }
                }
            }
        }
    }

    private fun registerReceiverIfNeeded() {
        if (receiverRegistered) return
        val filter = IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        }
        ContextCompat.registerReceiver(context, btReceiver, filter, ContextCompat.RECEIVER_EXPORTED)
        receiverRegistered = true
    }

    @SuppressLint("MissingPermission")
    fun release() {
        stopDiscovery()
        if (receiverRegistered) {
            try {
                context.unregisterReceiver(btReceiver)
            } catch (e: IllegalArgumentException) {
                Log.w(TAG, "Receiver already unregistered", e)
            }
            receiverRegistered = false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            hidDevice?.let { proxy ->
                if (isAppRegistered) {
                    proxy.unregisterApp()
                }
                bluetoothAdapter?.closeProfileProxy(BluetoothProfile.HID_DEVICE, proxy)
            }
        }
        executor.shutdown()
        scope.cancel()
    }

    @SuppressLint("MissingPermission")
    private fun BluetoothDevice.safeName(): String {
        return try {
            name?.takeIf { it.isNotBlank() } ?: address ?: "Unknown TV"
        } catch (_: SecurityException) {
            address ?: "Unknown TV"
        }
    }

    @SuppressLint("MissingPermission")
    private fun BluetoothDevice.toBondedTvDevice(): BondedTvDevice {
        val label = safeName()
        val likelySamsung = label.contains("samsung", ignoreCase = true) ||
            label.contains("tv", ignoreCase = true) ||
            label.contains("qled", ignoreCase = true) ||
            label.contains("oled", ignoreCase = true) ||
            label.contains("frame", ignoreCase = true) ||
            label.contains("crystal", ignoreCase = true)
        val bonded = try {
            bondState == BluetoothDevice.BOND_BONDED
        } catch (_: SecurityException) {
            false
        }
        return BondedTvDevice(
            name = label,
            address = address ?: "00:00:00:00:00:00",
            isSamsungLikely = likelySamsung,
            isBonded = bonded
        )
    }
}
