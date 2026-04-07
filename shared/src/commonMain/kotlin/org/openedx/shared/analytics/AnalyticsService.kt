package org.openedx.shared.analytics

/**
 * Platform-specific analytics service.
 * Android: Firebase Analytics
 * iOS: Firebase Analytics (via native SDK)
 */
interface AnalyticsService {
    fun logEvent(name: String, params: Map<String, Any?> = emptyMap())
    fun logScreenEvent(screenName: String, params: Map<String, Any?> = emptyMap())
    fun setUserId(userId: Long)
}
