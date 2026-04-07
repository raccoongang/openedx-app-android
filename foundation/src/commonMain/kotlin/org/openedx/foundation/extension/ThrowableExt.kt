package org.openedx.foundation.extension

import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

fun Throwable.isInternetError(): Boolean {
    return this is UnknownHostException ||
        this is SocketTimeoutException ||
        this is ConnectException ||
        this is SocketException ||
        this is SSLException ||
        this.cause?.isInternetError() == true
}
