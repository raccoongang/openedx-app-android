package org.openedx.core.utils

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.openedx.core.BuildConfig
import org.openedx.core.config.Config

actual class Logger actual constructor(private val tag: String) : KoinComponent {

    private val config by inject<Config>()

    actual fun d(message: () -> String) {
        if (BuildConfig.DEBUG) Log.d(tag, message())
    }

    actual fun e(message: () -> String) {
        if (BuildConfig.DEBUG) Log.e(tag, message())
    }

    actual fun e(throwable: Throwable, submitCrashReport: Boolean) {
        if (BuildConfig.DEBUG) throwable.printStackTrace()
        if (submitCrashReport && config.getFirebaseConfig().enabled) {
            FirebaseCrashlytics.getInstance().recordException(throwable)
        }
    }

    actual fun i(message: () -> String) {
        if (BuildConfig.DEBUG) Log.i(tag, message())
    }

    actual fun w(message: () -> String) {
        if (BuildConfig.DEBUG) Log.w(tag, message())
    }
}
