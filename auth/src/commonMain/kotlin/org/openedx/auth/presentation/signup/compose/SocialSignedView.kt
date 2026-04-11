package org.openedx.auth.presentation.signup.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.openedx.auth.*
import org.openedx.auth.data.model.AuthType
import org.openedx.core.config.Config
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appShapes
import org.openedx.core.Res as coreRes
import org.openedx.core.core_ic_check

private object SocialSignedViewHelper : KoinComponent {
    val config: Config by inject()
}

@Composable
fun SocialSignedView(authType: AuthType) {
    val appName = remember { SocialSignedViewHelper.config.getPlatformName() }
    Column(
        modifier = Modifier
            .background(
                color = MaterialTheme.appColors.authSSOSuccessBackground,
                shape = MaterialTheme.appShapes.buttonShape
            )
            .padding(20.dp)
    ) {
        Row {
            Icon(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(20.dp),
                painter = painterResource(coreRes.drawable.core_ic_check),
                tint = MaterialTheme.appColors.successBackground,
                contentDescription = ""
            )

            Text(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.appColors.primary,
                text = stringResource(
                    Res.string.auth_social_signed_title,
                    authType.methodName
                )
            )
        }

        Text(
            modifier = Modifier.padding(top = 8.dp, start = 28.dp),
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            text = stringResource(
                Res.string.auth_social_signed_desc,
                appName
            )
        )
    }
}
