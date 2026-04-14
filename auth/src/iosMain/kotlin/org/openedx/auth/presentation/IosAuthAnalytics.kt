package org.openedx.auth.presentation

/**
 * iOS no-op AuthAnalytics.
 *
 * TODO iOS: wire to Firebase / Braze / segment when those SDKs land on iOS side
 * of the CMP migration. Android uses AnalyticsManager from app module.
 */
class IosAuthAnalytics : AuthAnalytics {
    override fun setUserIdForSession(userId: Long) = Unit
    override fun logEvent(event: String, params: Map<String, Any?>) = Unit
    override fun logScreenEvent(screenName: String, params: Map<String, Any?>) = Unit
}
