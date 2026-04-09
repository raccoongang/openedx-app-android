package org.openedx.core.system

sealed class EdxError : Exception() {
    class InvalidGrantException : EdxError()
    class UserNotActiveException : EdxError()
    class ValidationException(val error: String) : EdxError()
    data class UnknownException(val error: String) : EdxError()
}
