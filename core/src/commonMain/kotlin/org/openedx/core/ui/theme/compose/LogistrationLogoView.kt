package org.openedx.core.ui.theme.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.openedx.core.Res
import org.openedx.core.core_ic_logo
import org.openedx.core.ui.theme.appColors

@Composable
fun LogistrationLogoView() {
    Image(
        modifier = Modifier
            .padding(top = 64.dp, bottom = 20.dp)
            .wrapContentWidth(),
        painter = painterResource(Res.drawable.core_ic_logo),
        contentDescription = null,
        colorFilter = ColorFilter.tint(MaterialTheme.appColors.primary)
    )
}
