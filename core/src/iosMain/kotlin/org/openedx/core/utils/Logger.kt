package org.openedx.core.utils

actual class Logger actual constructor(private val tag: String) {
    actual fun d(message: () -> String) {
        println("D/$tag: ${message()}")
    }

    actual fun e(message: () -> String) {
        println("E/$tag: ${message()}")
    }

    actual fun e(throwable: Throwable, submitCrashReport: Boolean) {
        println("E/$tag: ${throwable.message}")
        throwable.printStackTrace()
    }

    actual fun i(message: () -> String) {
        println("I/$tag: ${message()}")
    }

    actual fun w(message: () -> String) {
        println("W/$tag: ${message()}")
    }
}
