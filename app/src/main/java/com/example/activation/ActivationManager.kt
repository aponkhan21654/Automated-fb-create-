package com.example.activation

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import java.security.MessageDigest
import java.util.Locale

object ActivationManager {

    private const val PREFS_NAME = "device_activation_prefs"
    private const val KEY_EXPIRY = "activation_expiry_timestamp"
    private const val KEY_ACTIVATED_CODE = "activated_code"
    private const val KEY_IS_BANNED = "is_device_banned"
    
    // Master secret key used for generating/validating activation codes cryptographic signatures
    private const val MASTER_SECRET = "FB_AUTO_PRO_SECRET_KEY_2026_SECURE_SALT"

    fun getDeviceId(context: Context): String {
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        return androidId?.uppercase(Locale.ROOT) ?: "UNKNOWN_DEVICE"
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isBanned(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_IS_BANNED, false)
    }

    fun isActivated(context: Context): Boolean {
        if (isBanned(context)) return false
        val expiry = getPrefs(context).getLong(KEY_EXPIRY, 0L)
        return System.currentTimeMillis() < expiry
    }

    fun revokeActivation(context: Context) {
        getPrefs(context).edit()
            .putLong(KEY_EXPIRY, 0L)
            .remove(KEY_ACTIVATED_CODE)
            .apply()
    }

    fun banCurrentDevice(context: Context) {
        getPrefs(context).edit()
            .putBoolean(KEY_IS_BANNED, true)
            .putLong(KEY_EXPIRY, 0L)
            .remove(KEY_ACTIVATED_CODE)
            .apply()
    }

    fun unbanCurrentDevice(context: Context) {
        getPrefs(context).edit()
            .putBoolean(KEY_IS_BANNED, false)
            .apply()
    }

    fun getExpiryTimestamp(context: Context): Long {
        return getPrefs(context).getLong(KEY_EXPIRY, 0L)
    }

    fun getRemainingDays(context: Context): Long {
        if (isBanned(context)) return 0L
        val expiry = getExpiryTimestamp(context)
        val now = System.currentTimeMillis()
        if (expiry <= now) return 0L
        val diffMs = expiry - now
        return diffMs / (1000 * 60 * 60 * 24)
    }

    /**
     * Generates a Ban Key for a device ID.
     */
    fun generateBanKey(deviceId: String): String {
        val cleanDeviceId = deviceId.trim().uppercase(Locale.ROOT)
        val rawString = "$cleanDeviceId:BAN:$MASTER_SECRET"
        val hash = sha256(rawString)
        val shortHash = hash.take(8).uppercase(Locale.ROOT)
        return "BAN-$shortHash"
    }

    /**
     * Generates a 16-character Activation Key for a device ID and duration in days.
     */
    fun generateActivationKey(deviceId: String, days: Int): String {
        val cleanDeviceId = deviceId.trim().uppercase(Locale.ROOT)
        val rawString = "$cleanDeviceId:$days:$MASTER_SECRET"
        val hash = sha256(rawString)
        val shortHash = hash.take(8).uppercase(Locale.ROOT)
        // Format: ACT-<DAYS>-<SHORTHASH> e.g. ACT30-A8F2E901
        return "ACT$days-$shortHash"
    }

    /**
     * Validates and applies the activation key or ban key.
     */
    fun activateWithKey(context: Context, key: String): Boolean {
        val cleanKey = key.trim().uppercase(Locale.ROOT)
        val deviceId = getDeviceId(context)

        // Check if Ban Key
        val expectedBanKey = generateBanKey(deviceId)
        if (cleanKey == expectedBanKey) {
            banCurrentDevice(context)
            return true
        }

        // Parse key format: ACT<DAYS>-<HASH>
        val regex = Regex("""^ACT(\d+)-([A-F0-9]{8})$""")
        val match = regex.find(cleanKey) ?: return false

        val daysStr = match.groupValues[1]
        val days = daysStr.toIntOrNull() ?: return false

        // Verify key against expected key for this device
        val expectedKey = generateActivationKey(deviceId, days)
        if (cleanKey == expectedKey) {
            val now = System.currentTimeMillis()
            val durationMs = if (days >= 9999) {
                100L * 365 * 24 * 60 * 60 * 1000 // 100 years (Lifetime)
            } else {
                days.toLong() * 24 * 60 * 60 * 1000
            }

            // Extend existing expiry if active, or start from now
            val currentExpiry = getExpiryTimestamp(context)
            val newExpiry = if (currentExpiry > now) {
                currentExpiry + durationMs
            } else {
                now + durationMs
            }

            getPrefs(context).edit()
                .putBoolean(KEY_IS_BANNED, false)
                .putLong(KEY_EXPIRY, newExpiry)
                .putString(KEY_ACTIVATED_CODE, cleanKey)
                .apply()
            return true
        }
        return false
    }

    fun setManualExpiry(context: Context, expiryTimestamp: Long) {
        getPrefs(context).edit().putLong(KEY_EXPIRY, expiryTimestamp).apply()
    }

    private fun sha256(input: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(input.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
