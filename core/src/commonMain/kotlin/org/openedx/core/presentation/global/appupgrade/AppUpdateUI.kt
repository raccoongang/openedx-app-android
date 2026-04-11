package org.openedx.core.presentation.global.appupgrade

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.openedx.core.Res
import org.openedx.core.core_ic_icon_upgrade
import org.openedx.core.core_ic_warning
import org.openedx.core.core_account_settings
import org.openedx.core.core_app_update_required_description
import org.openedx.core.core_app_update_required_title
import org.openedx.core.core_app_upgrade_box_description
import org.openedx.core.core_app_upgrade_dialog_description
import org.openedx.core.core_app_upgrade_title
import org.openedx.core.core_deprecated_app_version
import org.openedx.core.core_not_now
import org.openedx.core.core_update
import org.openedx.core.ui.noRippleClickable
import org.openedx.core.ui.statusBarsInset
import org.openedx.foundation.presentation.rememberWindowSize
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appShapes
import org.openedx.core.ui.theme.appTypography

@Composable
fun AppUpgradeRequiredScreen(
    modifier: Modifier = Modifier,
    onUpdateClick: () -> Unit
) {
    AppUpgradeRequiredScreen(
        modifier = modifier,
        showAccountSettingsButton = false,
        onAccountSettingsClick = {},
        onUpdateClick = onUpdateClick
    )
}

@Composable
fun AppUpgradeRequiredScreen(
    modifier: Modifier = Modifier,
    showAccountSettingsButton: Boolean,
    onAccountSettingsClick: () -> Unit,
    onUpdateClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = MaterialTheme.appColors.background)
            .statusBarsInset(),
        contentAlignment = Alignment.TopCenter
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 12.dp),
            text = stringResource(Res.string.core_deprecated_app_version),
            color = MaterialTheme.appColors.textPrimary,
            style = MaterialTheme.appTypography.titleMedium,
            textAlign = TextAlign.Center
        )
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AppUpgradeRequiredContent(
                modifier = Modifier.padding(horizontal = 32.dp),
                showAccountSettingsButton = showAccountSettingsButton,
                onAccountSettingsClick = onAccountSettingsClick,
                onUpdateClick = onUpdateClick
            )
        }
    }
}

@Composable
fun AppUpgradeRecommendDialog(
    modifier: Modifier = Modifier,
    onNotNowClick: () -> Unit,
    onUpdateClick: () -> Unit
) {
    val windowSize = rememberWindowSize()
    val imageModifier = if (windowSize.isLandscape) {
        Modifier.size(60.dp)
    } else {
        Modifier
    }

    Surface(
        modifier = modifier,
        color = Color.Transparent
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp)
                .noRippleClickable {
                    onNotNowClick()
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 640.dp)
                    .fillMaxWidth()
                    .clip(MaterialTheme.appShapes.cardShape)
                    .noRippleClickable {}
                    .background(
                        color = MaterialTheme.appColors.background,
                        shape = MaterialTheme.appShapes.cardShape
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Image(
                        modifier = imageModifier,
                        painter = painterResource(Res.drawable.core_ic_icon_upgrade),
                        contentDescription = null
                    )
                    Text(
                        text = stringResource(Res.string.core_app_upgrade_title),
                        color = MaterialTheme.appColors.textPrimary,
                        style = MaterialTheme.appTypography.titleMedium
                    )
                    Text(
                        text = stringResource(Res.string.core_app_upgrade_dialog_description),
                        color = MaterialTheme.appColors.textPrimary,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.appTypography.bodyMedium
                    )
                    AppUpgradeDialogButtons(
                        onNotNowClick = onNotNowClick,
                        onUpdateClick = onUpdateClick
                    )
                }
            }
        }
    }
}

@Composable
fun AppUpgradeRequiredContent(
    modifier: Modifier = Modifier,
    showAccountSettingsButton: Boolean,
    onAccountSettingsClick: () -> Unit,
    onUpdateClick: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Image(
            painter = painterResource(Res.drawable.core_ic_warning),
            contentDescription = null
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(Res.string.core_app_update_required_title),
                color = MaterialTheme.appColors.textPrimary,
                style = MaterialTheme.appTypography.titleMedium
            )
            Text(
                text = stringResource(Res.string.core_app_update_required_description),
                color = MaterialTheme.appColors.textPrimary,
                textAlign = TextAlign.Center,
                style = MaterialTheme.appTypography.bodyMedium
            )
        }
        AppUpgradeRequiredButtons(
            showAccountSettingsButton = showAccountSettingsButton,
            onAccountSettingsClick = onAccountSettingsClick,
            onUpdateClick = onUpdateClick
        )
    }
}

@Composable
fun AppUpgradeRequiredButtons(
    showAccountSettingsButton: Boolean,
    onAccountSettingsClick: () -> Unit,
    onUpdateClick: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        if (showAccountSettingsButton) {
            TransparentTextButton(
                text = stringResource(Res.string.core_account_settings),
                onClick = onAccountSettingsClick
            )
        }
        DefaultTextButton(
            text = stringResource(Res.string.core_update),
            onClick = onUpdateClick
        )
    }
}

@Composable
fun AppUpgradeDialogButtons(
    onNotNowClick: () -> Unit,
    onUpdateClick: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        TransparentTextButton(
            text = stringResource(Res.string.core_not_now),
            onClick = onNotNowClick
        )
        DefaultTextButton(
            text = stringResource(Res.string.core_update),
            onClick = onUpdateClick
        )
    }
}

@Composable
fun TransparentTextButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        modifier = Modifier
            .height(42.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        elevation = null,
        shape = MaterialTheme.appShapes.navigationButtonShape,
        onClick = onClick
    ) {
        Text(
            color = MaterialTheme.appColors.textAccent,
            style = MaterialTheme.appTypography.labelLarge,
            text = text
        )
    }
}

@Composable
fun DefaultTextButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        modifier = Modifier
            .height(42.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.appColors.primaryButtonBackground
        ),
        elevation = null,
        shape = MaterialTheme.appShapes.navigationButtonShape,
        onClick = onClick
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                color = MaterialTheme.appColors.primaryButtonText,
                style = MaterialTheme.appTypography.labelLarge
            )
        }
    }
}

@Composable
fun AppUpgradeRecommendedBox(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(20.dp)
            .clickable {
                onClick()
            },
        shape = MaterialTheme.appShapes.cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.primary)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                modifier = Modifier.size(40.dp),
                painter = painterResource(Res.drawable.core_ic_icon_upgrade),
                contentDescription = null,
                tint = Color.White
            )
            Column {
                Text(
                    text = stringResource(Res.string.core_app_upgrade_title),
                    color = Color.White,
                    style = MaterialTheme.appTypography.titleMedium
                )
                Text(
                    text = stringResource(Res.string.core_app_upgrade_box_description),
                    color = Color.White,
                    style = MaterialTheme.appTypography.bodyMedium
                )
            }
        }
    }
}
