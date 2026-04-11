package org.openedx.foundation.extension

expect fun Throwable.isInternetError(): Boolean

/**
 * A common exception to represent a no-connection error in commonMain code.
 * Both Android and iOS [isInternetError] implementations recognize this exception.
 */
class NoConnectionException : Exception("No internet connection")
