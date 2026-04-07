package org.openedx.core.system

import android.webkit.CookieManager
import io.ktor.client.statement.HttpResponse
import io.ktor.http.setCookie
import org.openedx.core.config.Config
import org.openedx.core.data.api.CookiesApi
import java.util.concurrent.TimeUnit

class AppCookieManager(private val config: Config, private val api: CookiesApi) {

    companion object {
        private val FRESHNESS_INTERVAL = TimeUnit.HOURS.toMillis(1)
    }

    private var authSessionCookieExpiration: Long = -1
    private var response: HttpResponse? = null

    suspend fun tryToRefreshSessionCookie() {
        try {
            response = api.userCookies()
            clearWebViewCookie()
            val cookieManager = CookieManager.getInstance()
            response?.setCookie()?.forEach { cookie ->
                cookieManager.setCookie(
                    config.getApiHostURL(),
                    "${cookie.name}=${cookie.value}"
                )
            }
            authSessionCookieExpiration = System.currentTimeMillis() + FRESHNESS_INTERVAL
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clearWebViewCookie() {
        CookieManager.getInstance().removeAllCookies(null)
        authSessionCookieExpiration = -1
    }

    fun isSessionCookieMissingOrExpired(): Boolean {
        return authSessionCookieExpiration < System.currentTimeMillis()
    }
}
