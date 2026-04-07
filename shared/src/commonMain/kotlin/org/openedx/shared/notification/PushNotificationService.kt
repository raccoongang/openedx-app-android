package org.openedx.shared.notification

/**
 * Common push notification service interface.
 * Platform-specific implementations handle FCM (Android) / APNs (iOS).
 */
interface PushNotificationService {
    fun registerForPushNotifications()
    fun getDeviceToken(): String?
    suspend fun syncToken(token: String)
}
