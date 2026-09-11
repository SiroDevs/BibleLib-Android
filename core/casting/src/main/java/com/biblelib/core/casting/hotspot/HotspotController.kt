package com.biblelib.core.casting.hotspot

import android.content.Context
import android.net.wifi.SoftApConfiguration
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import kotlin.random.Random

data class HotspotInfo(
    val ssid: String,
    val password: String?,
    val isOpen: Boolean,
)

sealed interface HotspotOutcome {
    data class Success(val info: HotspotInfo) : HotspotOutcome
    data class Failure(val message: String) : HotspotOutcome
}

class HotspotController(context: Context) {

    private val appContext = context.applicationContext
    private val wifiManager = appContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val mainHandler = Handler(Looper.getMainLooper())

    private var reservation: WifiManager.LocalOnlyHotspotReservation? = null
    private var pendingRetry: Runnable? = null

    // Bumped on every start()/stop() so a retry scheduled by a stale attempt
    // never fires after the caller has moved on.
    private var requestGeneration = 0

    val isActive: Boolean get() = reservation != null

    /**
     * LocalOnlyHotspot commonly fails on the very first call right after Wi-Fi
     * state changes (radio still settling, previous reservation still tearing
     * down, etc.), so a failed attempt is retried automatically before it's
     * reported back as an error.
     */
    fun start(onResult: (HotspotOutcome) -> Unit) {
        stop()
        val generation = ++requestGeneration
        attemptStart(generation, attempt = 1, onResult)
    }

    fun stop() {
        requestGeneration++
        pendingRetry?.let { mainHandler.removeCallbacks(it) }
        pendingRetry = null
        reservation?.close()
        reservation = null
    }

    private fun attemptStart(generation: Int, attempt: Int, onResult: (HotspotOutcome) -> Unit) {
        val fallbackSsid = generateSsid()

        val callback = object : WifiManager.LocalOnlyHotspotCallback() {
            override fun onStarted(res: WifiManager.LocalOnlyHotspotReservation) {
                if (generation != requestGeneration) return
                reservation = res
                onResult(HotspotOutcome.Success(resolveInfo(res, fallbackSsid)))
            }

            override fun onStopped() {
                if (generation == requestGeneration) reservation = null
            }

            override fun onFailed(reason: Int) {
                if (generation != requestGeneration) return
                reservation = null
                retryOrFail(generation, attempt, onResult) { failureMessage(reason) }
            }
        }

        try {
            @Suppress("DEPRECATION")
            wifiManager.startLocalOnlyHotspot(callback, null)
        } catch (e: Exception) {
            retryOrFail(generation, attempt, onResult) { e.message ?: "Couldn't start the hotspot" }
        }
    }

    private fun retryOrFail(
        generation: Int,
        attempt: Int,
        onResult: (HotspotOutcome) -> Unit,
        message: () -> String,
    ) {
        if (attempt >= MAX_ATTEMPTS) {
            onResult(HotspotOutcome.Failure(message()))
            return
        }
        val retry = Runnable {
            if (generation == requestGeneration) attemptStart(generation, attempt + 1, onResult)
        }
        pendingRetry = retry
        mainHandler.postDelayed(retry, RETRY_DELAY_MS)
    }

    private fun generateSsid(): String = "BibleLib Casting-${Random.nextInt(1000, 9999)}"

    private fun resolveInfo(
        res: WifiManager.LocalOnlyHotspotReservation,
        fallbackSsid: String,
    ): HotspotInfo {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val config = res.softApConfiguration
            if (config != null) {
                val isOpen = config.securityType == SoftApConfiguration.SECURITY_TYPE_OPEN
                return HotspotInfo(
                    ssid = config.ssid ?: fallbackSsid,
                    password = if (isOpen) null else config.passphrase,
                    isOpen = isOpen,
                )
            }
        }

        @Suppress("DEPRECATION")
        val legacy = res.wifiConfiguration
        @Suppress("DEPRECATION")
        val legacySsid = legacy?.SSID?.trim('"')
        @Suppress("DEPRECATION")
        val legacyPassword = legacy?.preSharedKey?.trim('"')
        return HotspotInfo(
            ssid = legacySsid ?: fallbackSsid,
            password = legacyPassword,
            isOpen = legacyPassword.isNullOrEmpty(),
        )
    }

    private fun failureMessage(reason: Int): String = when (reason) {
        WifiManager.LocalOnlyHotspotCallback.ERROR_NO_CHANNEL -> "No Wi-Fi channel is available right now"
        WifiManager.LocalOnlyHotspotCallback.ERROR_GENERIC -> "The hotspot couldn't be started"
        WifiManager.LocalOnlyHotspotCallback.ERROR_INCOMPATIBLE_MODE ->
            "Wi-Fi is busy with another connection, like tethering"
        WifiManager.LocalOnlyHotspotCallback.ERROR_TETHERING_DISALLOWED ->
            "Hotspot use is disabled on this device by policy"
        else -> "The hotspot couldn't be started (code $reason)"
    }

    private companion object {
        const val MAX_ATTEMPTS = 3
        const val RETRY_DELAY_MS = 600L
    }
}
