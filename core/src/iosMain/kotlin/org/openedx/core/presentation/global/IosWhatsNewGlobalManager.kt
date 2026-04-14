package org.openedx.core.presentation.global

/**
 * iOS stub — WhatsNew flow is not wired into the iOS surface yet.
 *
 * TODO iOS: mirror Android's WhatsNewManagerImpl that compares saved whats_new version
 * against the bundled changelog JSON. For now always returns false so the login/register
 * flow goes directly to Main instead of the (iOS-unbuilt) WhatsNew screen.
 */
class IosWhatsNewGlobalManager : WhatsNewGlobalManager {
    override fun shouldShowWhatsNew(): Boolean = false
}
