package org.openedx.core.ui.theme.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.openedx.core.Res
import org.openedx.core.core_ic_logo

@Composable
fun SignInLogoView() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(fraction = 0.2f),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(Res.drawable.core_ic_logo),
            contentDescription = null,
            modifier = Modifier.padding(top = 20.dp)
        )
    }
}
