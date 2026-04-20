package org.openedx.shared.stubs

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.setCookie
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.openedx.core.ApiConstants
import org.openedx.core.config.Config
import org.openedx.core.system.AppCookieManager
import platform.Foundation.NSDate
import platform.Foundation.NSHTTPCookie
import platform.Foundation.NSHTTPCookieDomain
import platform.Foundation.NSHTTPCookieExpires
import platform.Foundation.NSHTTPCookieName
import platform.Foundation.NSHTTPCookiePath
import platform.Foundation.NSHTTPCookieSecure
import platform.Foundation.NSHTTPCookieStorage
import platform.Foundation.NSHTTPCookieValue
import platform.Foundation.NSURL
import platform.Foundation.dateWithTimeIntervalSinceNow
import platform.Foundation.timeIntervalSince1970
import platform.WebKit.WKWebsiteDataStore
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

/**
 * iOS AppCookieManager — POSTs `/oauth2/login/` (Authorization header injected via HttpClient
 * defaultRequest), grabs `Set-Cookie` headers from the response, and seeds them into both
 * `NSHTTPCookieStorage` (for NSURLSession) and `WKWebsiteDataStore.default().httpCookieStore`
 * (for WKWebView). Mirrors the Android CookieManager flow.
 */
@OptIn(ExperimentalForeignApi::class)
class IosAppCookieManager(
    private val config: Config,
    private val client: HttpClient,
) : AppCookieManager {

    private var authSessionCookieExpiration: Long = -1
    private val cookieJar = mutableMapOf<String, String>()

    /** Snapshot of the cookies seeded by the most recent refresh. */
    val sessionCookieHeader: String
        get() = cookieJar.entries.joinToString("; ") { "${it.key}=${it.value}" }

    override suspend fun tryToRefreshSessionCookie() {
        try {
            val response: HttpResponse = client.post(ApiConstants.URL_LOGIN)
            val hostUrl = config.getApiHostURL()
            val domain = NSURL(string = hostUrl)?.host ?: return

            val cookies = response.setCookie()
            if (cookies.isEmpty()) return

            cookies.forEach { cookie ->
                cookieJar[cookie.name] = cookie.value
            }

            // Build NSHTTPCookies and seed both stores. WKHTTPCookieStore.setCookie is async —
            // we must await it on the main thread before letting the WebView load.
            val nsCookies = cookies.mapNotNull { cookie ->
                val props = mutableMapOf<Any?, Any>()
                props[NSHTTPCookieName] = cookie.name
                props[NSHTTPCookieValue] = cookie.value
                props[NSHTTPCookieDomain] = cookie.domain?.ifEmpty { null } ?: domain
                props[NSHTTPCookiePath] = cookie.path?.ifEmpty { null } ?: "/"
                if (cookie.secure) props[NSHTTPCookieSecure] = "TRUE"
                cookie.maxAge?.takeIf { it > 0 }?.let { max ->
                    props[NSHTTPCookieExpires] = NSDate.dateWithTimeIntervalSinceNow(max.toDouble())
                }
                @Suppress("UNCHECKED_CAST")
                NSHTTPCookie.cookieWithProperties(props as Map<Any?, *>)
            }
            nsCookies.forEach { NSHTTPCookieStorage.sharedHTTPCookieStorage.setCookie(it) }
            // WKHTTPCookieStore must be touched on the main thread.
            withContext(Dispatchers.Main) {
                val store = WKWebsiteDataStore.defaultDataStore().httpCookieStore
                nsCookies.forEach { cookie ->
                    suspendCoroutine<Unit> { cont ->
                        store.setCookie(cookie) { cont.resume(Unit) }
                    }
                }
            }
            authSessionCookieExpiration = (NSDate().timeIntervalSince1970 * 1000)
                .toLong() + FRESHNESS_INTERVAL_MS
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun clearWebViewCookie() {
        cookieJar.clear()
        val store = WKWebsiteDataStore.defaultDataStore().httpCookieStore
        store.getAllCookies { cookies ->
            cookies?.forEach { cookie ->
                (cookie as? NSHTTPCookie)?.let { store.deleteCookie(it, completionHandler = null) }
            }
        }
        NSHTTPCookieStorage.sharedHTTPCookieStorage.cookies?.forEach { cookie ->
            (cookie as? NSHTTPCookie)?.let { NSHTTPCookieStorage.sharedHTTPCookieStorage.deleteCookie(it) }
        }
        authSessionCookieExpiration = -1
    }

    override fun isSessionCookieMissingOrExpired(): Boolean {
        val now = (NSDate().timeIntervalSince1970 * 1000).toLong()
        return authSessionCookieExpiration < now
    }

    private companion object {
        const val FRESHNESS_INTERVAL_MS = 60L * 60L * 1000L
    }
}
