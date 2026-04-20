package org.openedx.shared.ui

import androidx.compose.runtime.Composable

/**
 * Puts the current screen into immersive fullscreen while this composable is
 * present in the composition. On Android this hides the status and navigation
 * bars so the video player's own controls (progress bar, fullscreen-exit
 * button) remain visible at the edges of the screen even in landscape. iOS has
 * no navigation bar overlap, so this is a no-op there.
 */
@Composable
expect fun EnterFullscreenImmersiveMode()
