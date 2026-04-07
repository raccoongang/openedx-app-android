package org.openedx.shared.config

/**
 * Platform-specific config file loading.
 * Android: loads from assets/config/config.json
 * iOS: loads from Bundle resources
 */
expect fun loadConfigJson(): String
