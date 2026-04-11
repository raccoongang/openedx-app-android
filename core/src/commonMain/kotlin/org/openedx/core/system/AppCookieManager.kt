package org.openedx.core.system

interface AppCookieManager {
    suspend fun tryToRefreshSessionCookie()
    fun clearWebViewCookie()
    fun isSessionCookieMissingOrExpired(): Boolean
}
