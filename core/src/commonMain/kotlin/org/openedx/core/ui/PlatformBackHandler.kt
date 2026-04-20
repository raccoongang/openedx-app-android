package org.openedx.core.ui

import androidx.compose.runtime.Composable

/**
 * Cross-platform system back-button handler.
 *
 * Android: delegates to `androidx.activity.compose.BackHandler`.
 * iOS: no-op (system navigation gesture is handled by UINavigationController).
 */
@Composable
expect fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit)
