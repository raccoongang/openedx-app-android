package org.openedx.core.utils

expect class Logger(tag: String) {
    fun d(message: () -> String)
    fun e(message: () -> String)
    fun e(throwable: Throwable, submitCrashReport: Boolean = false)
    fun i(message: () -> String)
    fun w(message: () -> String)
}
