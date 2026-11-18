package com.example.cast

import android.content.Context
import android.content.Intent
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.example.model.DiscoveredTvStream
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.InetAddress

class CastDiscoveryController(private val context: Context) {

    companion object {
        private const val TAG = "CastDiscoveryController"
        private val SERVICE_TYPES = listOf("_rtsp._tcp.", "_http._tcp.", "_googlecast._tcp.")
    }

    private val nsdManager: NsdManager? = context.getSystemService(NsdManager::class.java)
    private val wifiManager: WifiManager? =
        context.applicationContext.getSystemService(WifiManager::class.java)
    private var multicastLock: WifiManager.MulticastLock? = null

    private val activeListeners = mutableListOf<NsdManager.DiscoveryListener>()
    private val resolvedStreams = linkedMapOf<String, DiscoveredTvStream>()

    private val _streams = MutableStateFlow<List<DiscoveredTvStream>>(emptyList())
    val streams: StateFlow<List<DiscoveredTvStream>> = _streams.asStateFlow()

    private val _isDiscovering = MutableStateFlow(false)
    val isDiscovering: StateFlow<Boolean> = _isDiscovering.asStateFlow()

    fun openSystemCastPicker(): Boolean {
        val candidates = listOf(
            Intent("android.settings.WIFI_DISPLAY_SETTINGS"),
            Intent(Settings.ACTION_CAST_SETTINGS),
            Intent(Settings.ACTION_SETTINGS)
        )
        for (intent in candidates) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                return true
            }
        }
        return false
    }

    fun startTvStreamDiscovery() {
        stopTvStreamDiscovery()
        val manager = nsdManager ?: return

        try {
            multicastLock = wifiManager?.createMulticastLock("SamCastNsdLock")?.apply {
                setReferenceCounted(false)
                acquire()
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "Multicast lock unavailable", e)
        }

        _isDiscovering.value = true
        for (serviceType in SERVICE_TYPES) {
            val listener = object : NsdManager.DiscoveryListener {
                override fun onDiscoveryStarted(regType: String) {}

                override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                    resolveService(manager, serviceInfo)
                }

                override fun onServiceLost(serviceInfo: NsdServiceInfo) {
                    val key = "${serviceInfo.serviceType}:${serviceInfo.serviceName}"
                    if (resolvedStreams.remove(key) != null) {
                        _streams.value = resolvedStreams.values.toList()
                    }
                }

                override fun onDiscoveryStopped(serviceType: String) {}

                override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                    Log.w(TAG, "NSD start failed for $serviceType: $errorCode")
                }

                override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {}
            }
            try {
                manager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, listener)
                activeListeners += listener
            } catch (e: Exception) {
                Log.w(TAG, "Unable to discover $serviceType", e)
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun resolveService(manager: NsdManager, serviceInfo: NsdServiceInfo) {
        try {
            manager.resolveService(serviceInfo, object : NsdManager.ResolveListener {
                override fun onResolveFailed(info: NsdServiceInfo, errorCode: Int) {}

                override fun onServiceResolved(info: NsdServiceInfo) {
                    val hostAddress: InetAddress = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        info.hostAddresses.firstOrNull() ?: info.host
                    } else {
                        info.host
                    } ?: return
                    val host = hostAddress.hostAddress ?: return
                    val port = info.port
                    val scheme = if (info.serviceType.contains("rtsp")) "rtsp" else "http"
                    val url = "$scheme://$host:$port/"
                    val key = "${info.serviceType}:${info.serviceName}"
                    resolvedStreams[key] = DiscoveredTvStream(
                        name = info.serviceName,
                        host = host,
                        port = port,
                        streamUrl = url,
                        serviceType = info.serviceType
                    )
                    _streams.value = resolvedStreams.values.toList()
                }
            })
        } catch (e: Exception) {
            Log.w(TAG, "Resolve service failed", e)
        }
    }

    fun stopTvStreamDiscovery() {
        val manager = nsdManager
        if (manager != null) {
            for (listener in activeListeners) {
                try {
                    manager.stopServiceDiscovery(listener)
                } catch (_: Exception) {
                }
            }
        }
        activeListeners.clear()
        try {
            if (multicastLock?.isHeld == true) {
                multicastLock?.release()
            }
        } catch (_: Exception) {
        }
        multicastLock = null
        _isDiscovering.value = false
    }
}
