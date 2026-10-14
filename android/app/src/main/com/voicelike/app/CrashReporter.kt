package com.voicelike.app

import android.content.Context
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Minimal crash logger: saves uncaught exception stack traces to a local file
 * so the user can share it on the next launch (no internet permission needed).
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
                File(context.filesDir, FILE_NAME).writeText(text)
            } catch (_: Throwable) {
                // Never throw from the handler
            }
            previous?.uncaughtException(thread, throwable)
        }
    }

    /** Returns the saved crash log, if any. */
    fun readLastCrash(context: Context): String? {
        val f = File(context.filesDir, FILE_NAME)
        return if (f.exists() && f.length() > 0) {
            runCatching { f.readText() }.getOrNull()
        } else null

    }

    fun clear(context: Context) {
        runCatching { File(context.filesDir, FILE_NAME).delete() }
    }
}
