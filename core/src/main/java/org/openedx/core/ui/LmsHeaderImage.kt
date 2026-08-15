package org.openedx.core.ui

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.openedx.core.R
import org.openedx.core.lmsdirectory.LmsImageSource
import org.openedx.core.lmsdirectory.LmsThemeController

/**
 * Header image for the auth screens (sign-in / register / reset password). When the LMS
 * Directory feature has a selected platform with a custom login background, shows that
 * image; otherwise the stock gradient header. Mirrors iOS's `LmsHeaderBackground`, so a
 * branded platform looks the same across sign-in, register, reset and the settings screens.
 */
@Composable
fun LmsHeaderImage(modifier: Modifier = Modifier) {
    val background = LmsImageSource.model(LmsThemeController.loginBackgroundUrl)
    if (background != null) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(background)
                .placeholder(R.drawable.core_top_header)
                .error(R.drawable.core_top_header)
                // No crossfade: the image is prefetched while the learner is still
                // choosing a platform, so it is already decoded by the time this is
                // built. Fading it in would put back the appearing-image effect that
                // prefetching exists to remove.
                .crossfade(false)
                .build(),
            modifier = modifier,
            contentScale = ContentScale.FillBounds,
            contentDescription = null,
        )
    } else {
        Image(
            modifier = modifier,
            painter = painterResource(id = R.drawable.core_top_header),
            contentScale = ContentScale.FillBounds,
            contentDescription = null,
        )
    }
}
