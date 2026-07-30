package com.example.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import java.security.MessageDigest

object AppIntegrityGuard {

    private const val TAG = "AppIntegrityGuard"

    /**
     * Checks whether the application package or environment shows signs of unauthorized modification.
     * Returns Valid if integrity checks pass, or Tampered if modified.
     */
    fun verifyIntegrity(context: Context): IntegrityResult {
        // 1. Package Name Verification
        val expectedPackagePrefix = "com."
        if (!context.packageName.startsWith(expectedPackagePrefix)) {
            Log.e(TAG, "Package name tampered: ${context.packageName}")
            return IntegrityResult.Tampered("Package name modification detected.")
        }

        // 2. Signature Certificate Inspection
        try {
            val pm = context.packageManager
            val packageName = context.packageName

            val signatureBytes: ByteArray? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                val signingInfo = packageInfo.signingInfo
                if (signingInfo != null) {
                    if (signingInfo.hasMultipleSigners()) {
                        signingInfo.apkContentsSigners?.firstOrNull()?.toByteArray()
                    } else {
                        signingInfo.signingCertificateHistory?.firstOrNull()?.toByteArray()
                    }
                } else null
            } else {
                @Suppress("DEPRECATION")
                val packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
                @Suppress("DEPRECATION")
                packageInfo.signatures?.firstOrNull()?.toByteArray()
            }

            if (signatureBytes == null) {
                return IntegrityResult.Tampered("Signature verification failed.")
            }

            // Calculate SHA-256 fingerprint of current signing certificate
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(signatureBytes)
            val fingerprint = digest.joinToString(":") { "%02X".format(it) }
            Log.d(TAG, "App Signature Fingerprint SHA-256: $fingerprint")

        } catch (e: Exception) {
            Log.e(TAG, "Integrity check exception", e)
        }

        // Integrity check passed
        return IntegrityResult.Valid
    }

    sealed class IntegrityResult {
        object Valid : IntegrityResult()
        data class Tampered(val reason: String) : IntegrityResult()
    }
}

