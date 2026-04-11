package org.openedx.core.ui

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import org.openedx.core.Res
import org.openedx.core.core_top_header

fun Modifier.settingsHeaderBackground(): Modifier = composed {
    return@composed this
        .paint(
            painter = painterResource(Res.drawable.core_top_header),
            contentScale = ContentScale.FillWidth,
            alignment = Alignment.TopCenter
        )
}
