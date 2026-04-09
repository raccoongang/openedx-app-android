package org.openedx.foundation.extension

import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException

actual fun Throwable.isInternetError(): Boolean {
    return this is HttpRequestTimeoutException ||
        this is ConnectTimeoutException ||
        this is SocketTimeoutException ||
        this.message?.contains("NSURLErrorDomain") == true ||
        this.cause?.isInternetError() == true
}
