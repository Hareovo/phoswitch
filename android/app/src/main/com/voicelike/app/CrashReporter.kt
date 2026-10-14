package com.voicelike.app

import android.content.Context
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Minimal crash logger: saves uncaught exception stack traces to local files
 * so the user can share them on the next launch (no internet permission needed).
 *
 * Installed from PhoswitchApp.attachBaseContext (earliest app code) so even
 * Activity class-loading crashes get captured.
 */
object CrashReporter {

    private const val FILE_NAME = "crash_log.txt"

    fun install(context: Context) {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val sw = StringWriter()
                throwable.printStackTrace(PrintWriter(sw))
                val text = buildString {
                    appendLine("Time: ${System.currentTimeMillis()}")
                    appendLine("Thread: ${thread.name}")
                    appendLine("Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}, Android ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})")
                    appendLine()
                    append(sw.toString())
                }
                // Primary: app-private files dir (always writable)
                runCatching { File(context.filesDir, FILE_NAME).writeText(text) }
                // Backup: external files dir (survives uninstall-on-reinstall data wipe? no, but readable via file manager on some devices)
                runCatching {
                    val ext = context.getExternalFilesDir(null)
                    if (ext != null) File(ext, FILE_NAME).writeText(text)
                }
            } catch (_: Throwable) {
                // Never throw from the handler
            }
            previous?.uncaughtException(thread, throwable)
        }
    }

    /** Returns the saved crash log, if any. */
    fun readLastCrash(context: Context): String? {
        // Prefer private dir, fall back to external backup
        val primary = File(context.filesDir, FILE_NAME)
        if (primary.exists() && primary.length() > 0) {
            return runCatching { primary.readText() }.getOrNull()
        }
        val ext = context.getExternalFilesDir(null)
        if (ext != null) {
            val backup = File(ext, FILE_NAME)
            if (backup.exists() && backup.length() > 0) {
                return runCatching { backup.readText() }.getOrNull()
            }
        }
        return null
    }

    fun clear(context: Context) {
        runCatching { File(context.filesDir, FILE_NAME).delete() }
        val ext = context.getExternalFilesDir(null)
        if (ext != null) runCatching { File(ext, FILE_NAME).delete() }
    }
}
