package org.openedx.core.ui.theme.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.openedx.core.ui.theme.OpenEdXTheme

@Preview(widthDp = 375, heightDp = 400)
@Composable
fun SignInLogoViewPreview() {
    OpenEdXTheme {
        SignInLogoView()
    }
}
