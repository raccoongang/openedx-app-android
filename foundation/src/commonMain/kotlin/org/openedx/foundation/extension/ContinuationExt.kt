package org.openedx.foundation.extension

import kotlinx.coroutines.CancellableContinuation

fun <T> CancellableContinuation<T>.safeResume(value: T, onExceptionCalled: () -> Unit = {}) {
    if (isActive) {
        try {
            resumeWith(Result.success(value))
        } catch (_: Exception) {
            onExceptionCalled()
        }
    }
}
