package com.voicelike.app

import android.app.Application
import android.content.Context

/**
 * Custom Application: installs the crash reporter as early as possible
 * (attachBaseContext runs before any Activity / ContentProvider init),
 * so crashes during class loading or provider init are also captured.
 */
class PhoswitchApp : Application() {

    override fun attachBaseContext(base: Context) {
        // Wrap super() so even failures there get captured
        try {
            super.attachBaseContext(base)
        } finally {
            CrashReporter.install(this)
        }
    }

    override fun onCreate() {
        super.onCreate()
    }
}
