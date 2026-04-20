package org.openedx.core.presentation.dialog.alert

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.openedx.core.Res
import org.openedx.core.core_authorization
import org.openedx.core.core_authorization_request
import org.openedx.core.core_cancel
import org.openedx.core.core_continue
import org.openedx.core.core_ok
import org.openedx.core.core_register
import org.openedx.core.core_sign_in
import org.openedx.core.presentation.dialog.DefaultDialogBox
import org.openedx.core.presentation.global.appupgrade.DefaultTextButton
import org.openedx.core.presentation.global.appupgrade.TransparentTextButton
import org.openedx.core.ui.OpenEdXButton
import org.openedx.core.ui.OpenEdXOutlinedButton
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appTypography

@Composable
fun ActionDialog(
    title: String,
    message: String,
    onCancelClick: () -> Unit,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DefaultDialogBox(
        modifier = modifier,
        onDismissClick = onCancelClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(
                text = title,
                color = MaterialTheme.appColors.textPrimary,
                style = MaterialTheme.appTypography.titleMedium,
            )
            Text(
                text = message,
                color = MaterialTheme.appColors.textPrimary,
                textAlign = TextAlign.Center,
                style = MaterialTheme.appTypography.bodyMedium,
            )
            Row {
                TransparentTextButton(
                    text = stringResource(Res.string.core_cancel),
                    onClick = onCancelClick,
                )
                DefaultTextButton(
                    text = stringResource(Res.string.core_continue),
                    onClick = onContinueClick,
                )
            }
        }
    }
}

@Composable
fun InfoDialog(
    title: String,
    message: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DefaultDialogBox(
        modifier = modifier,
        onDismissClick = onClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(
                text = title,
                color = MaterialTheme.appColors.textPrimary,
                style = MaterialTheme.appTypography.titleMedium,
            )
            Text(
                text = message,
                color = MaterialTheme.appColors.textPrimary,
                textAlign = TextAlign.Center,
                style = MaterialTheme.appTypography.bodyMedium,
            )
            DefaultTextButton(
                text = stringResource(Res.string.core_ok),
                onClick = onClick,
            )
        }
    }
}

@Composable
fun AuthorizationDialog(
    onCancelClick: () -> Unit,
    onSignInClick: () -> Unit,
    onRegisterClick: () -> Unit,
    showRegisterButton: Boolean = true,
    modifier: Modifier = Modifier,
) {
    DefaultDialogBox(
        modifier = modifier,
        onDismissClick = onCancelClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                contentAlignment = Alignment.CenterEnd,
                modifier = Modifier.fillMaxWidth(),
            ) {
                IconButton(
                    modifier = Modifier.size(24.dp),
                    onClick = onCancelClick,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(Res.string.core_cancel),
                        tint = MaterialTheme.appColors.primary,
                    )
                }
            }
            Spacer(Modifier.size(12.dp))
            Text(
                text = stringResource(Res.string.core_authorization),
                color = MaterialTheme.appColors.textPrimary,
                style = MaterialTheme.appTypography.titleLarge,
            )
            Spacer(Modifier.size(8.dp))
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.core_authorization_request),
                color = MaterialTheme.appColors.textPrimary,
                style = MaterialTheme.appTypography.bodyMedium,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.size(24.dp))
            Row {
                OpenEdXOutlinedButton(
                    modifier = Modifier.weight(1f),
                    borderColor = MaterialTheme.appColors.primaryButtonBackground,
                    textColor = MaterialTheme.appColors.primaryButtonBackground,
                    text = stringResource(Res.string.core_sign_in),
                    onClick = onSignInClick,
                )
                if (showRegisterButton) {
                    Spacer(Modifier.width(16.dp))
                    OpenEdXButton(
                        modifier = Modifier.weight(1f),
                        text = stringResource(Res.string.core_register),
                        onClick = onRegisterClick,
                    )
                }
            }
        }
    }
}
