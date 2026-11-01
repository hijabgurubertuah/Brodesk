package com.example.services

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import java.net.URLEncoder

object UrlUtils {
    fun parseInputToUrl(input: String, searchEngine: String): String {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return ""

        // Check if input looks like a URL (contains dot, no spaces, valid TLD/host)
        val isUrlPattern = (trimmed.contains(".") || trimmed.startsWith("localhost")) && !trimmed.contains(" ")
        
        return if (isUrlPattern) {
            if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
                "https://$trimmed"
            } else {
                trimmed
            }
        } else {
            val encodedQuery = URLEncoder.encode(trimmed, "UTF-8")
            when (searchEngine.lowercase()) {
                "bing" -> "https://www.bing.com/search?q=$encodedQuery"
                "duckduckgo" -> "https://duckduckgo.com/?q=$encodedQuery"
                else -> "https://www.google.com/search?q=$encodedQuery"
            }
        }
    }

    fun getDomain(url: String): String {
        return try {
            val uri = java.net.URI(url)
            uri.host ?: url
        } catch (_: Exception) {
            url
        }
    }

    fun getUserAgent(browserIdentity: String, isDesktopMode: Boolean): String {
        if (!isDesktopMode) {
            return "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36"
        }
        return when (browserIdentity) {
            "Chrome macOS" -> "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"
            "Firefox Linux" -> "Mozilla/5.0 (X11; Linux x86_64; rv:132.0) Gecko/20100101 Firefox/132.0"
            else -> "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"
        }
    }
}

class VibrationService(context: Context) {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun vibrateShort(enabled: Boolean) {
        if (!enabled || vibrator == null || !vibrator.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(30L, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(30L)
        }
    }

    fun vibrateMedium(enabled: Boolean) {
        if (!enabled || vibrator == null || !vibrator.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(70L, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(70L)
        }
    }
}
