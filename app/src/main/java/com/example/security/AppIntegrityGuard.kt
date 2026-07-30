package com.example.security

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import java.io.File
import java.security.MessageDigest

object AppIntegrityGuard {

    private const val TAG = "AppIntegrityGuard"
    
    // Official expected package name
    private const val EXPECTED_PACKAGE_NAME = "com.twcreatefb.app"

    /**
     * Checks whether the application package or environment shows signs of unauthorized modification.
     * Returns Valid if integrity checks pass, or Tampered if modified.
     */
    fun verifyIntegrity(context: Context): IntegrityResult {
        // 1. Package Name Check
        if (context.packageName != EXPECTED_PACKAGE_NAME && !context.packageName.startsWith("com.aistudio")) {
            return IntegrityResult.Tampered("Package name mismatch: detected ${context.packageName}")
        }

        // 2. Check for Hooking Frameworks (Frida / Xposed / Substrate)
        if (isFridaDetected()) {
            return IntegrityResult.Tampered("Frida hooking framework detected!")
        }

        if (isXposedDetected()) {
            return IntegrityResult.Tampered("Xposed module/hooking framework detected!")
        }

        // 3. Check for Tampering / Patching tools (Lucky Patcher, MT Manager, App Cloner, etc.)
        val patcherTool = detectPatcherTools(context)
        if (patcherTool != null) {
            return IntegrityResult.Tampered("Tampering tool detected: $patcherTool")
        }

        // 4. Virtual Environment / Dual App Cloner Check
        if (isVirtualEnvironmentDetected(context)) {
            return IntegrityResult.Tampered("Virtual/Cloned app environment detected!")
        }

        return IntegrityResult.Valid
    }

    private fun isFridaDetected(): Boolean {
        // Check for common Frida files and paths
        val fridaPaths = arrayOf(
            "/data/local/tmp/frida-server",
            "/data/local/tmp/re.frida.server",
            "/sdcard/frida-server",
            "/data/local/tmp/frida-agent.so"
        )
        for (path in fridaPaths) {
            if (File(path).exists()) return true
        }

        // Check running threads/maps for frida
        try {
            val mapsFile = File("/proc/self/maps")
            if (mapsFile.exists()) {
                val content = mapsFile.readText()
                if (content.contains("frida") || content.contains("gadget")) {
                    return true
                }
            }
        } catch (e: Exception) {
            // Ignore
        }

        return false
    }

    private fun isXposedDetected(): Boolean {
        try {
            ClassLoader.getSystemClassLoader().loadClass("de.robv.android.xposed.XposedBridge")
            return true
        } catch (e: ClassNotFoundException) {
            // Expected
        }

        try {
            val stackTrace = Throwable().stackTrace
            for (element in stackTrace) {
                if (element.className.contains("de.robv.android.xposed")) {
                    return true
                }
            }
        } catch (e: Exception) {
            // Ignore
        }

        return false
    }

    private fun detectPatcherTools(context: Context): String? {
        val suspiciousPackages = mapOf(
            "com.dimonvideo.luckypatcher" to "Lucky Patcher",
            "com.chelpus.lackypatch" to "Lucky Patcher",
            "net.mclean.luckypatcher" to "Lucky Patcher",
            "bin.mt.plus" to "MT Manager",
            "com.applisto.appcloner" to "App Cloner",
            "io.va.exposed" to "VirtualXposed",
            "com.lbe.parallel" to "Parallel Space",
            "com.doubleopen.parallel" to "Dual Space"
        )

        val pm = context.packageManager
        for ((pkg, name) in suspiciousPackages) {
            try {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(pkg, 0)
                return name
            } catch (e: PackageManager.NameNotFoundException) {
                // Not installed
            }
        }
        return null
    }

    private fun isVirtualEnvironmentDetected(context: Context): Boolean {
        val filesDir = context.filesDir.absolutePath
        // If app files dir path contains multiple package names or unusual virtual paths
        if (filesDir.contains("/virtual/") || filesDir.contains("/parallel/") || filesDir.contains("com.lbe.parallel")) {
            return true
        }
        return false
    }

    sealed class IntegrityResult {
        object Valid : IntegrityResult()
        data class Tampered(val reason: String) : IntegrityResult()
    }
}
