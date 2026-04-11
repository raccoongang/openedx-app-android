package org.openedx.core.presentation.settings.calendarsync

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.openedx.core.Res
import org.openedx.core.core_title_syncing_calendar
import org.openedx.core.config.Config
import org.openedx.core.presentation.global.appupgrade.TransparentTextButton
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appShapes
import org.openedx.core.ui.theme.appTypography
import org.openedx.foundation.extension.takeIfNotEmpty
import org.koin.compose.koinInject
import androidx.compose.ui.window.DialogProperties as AlertDialogProperties

@Composable
fun CalendarSyncDialog(
    syncDialogType: CalendarSyncDialogType,
    calendarTitle: String,
    syncDialogPosAction: (CalendarSyncDialogType) -> Unit,
    syncDialogNegAction: (CalendarSyncDialogType) -> Unit,
    dismissSyncDialog: (CalendarSyncDialogType) -> Unit,
) {
    when (syncDialogType) {
        CalendarSyncDialogType.SYNC_DIALOG,
        CalendarSyncDialogType.UN_SYNC_DIALOG,
        -> {
            CalendarAlertDialog(
                dialogProperties = DialogProperties(
                    title = stringResource(syncDialogType.titleRes!!),
                    message = stringResource(syncDialogType.messageRes!!, calendarTitle),
                    positiveButton = stringResource(syncDialogType.positiveButtonRes!!),
                    negativeButton = stringResource(syncDialogType.negativeButtonRes!!),
                    positiveAction = { syncDialogPosAction(syncDialogType) },
                    negativeAction = { syncDialogNegAction(syncDialogType) },
                ),
                onDismiss = { dismissSyncDialog(syncDialogType) },
            )
        }

        CalendarSyncDialogType.PERMISSION_DIALOG -> {
            val config: Config = koinInject()
            val platformName = config.getPlatformName()
            CalendarAlertDialog(
                dialogProperties = DialogProperties(
                    title = stringResource(syncDialogType.titleRes!!, platformName),
                    message = stringResource(syncDialogType.messageRes!!, platformName, platformName),
                    positiveButton = stringResource(syncDialogType.positiveButtonRes!!),
                    negativeButton = stringResource(syncDialogType.negativeButtonRes!!),
                    positiveAction = { syncDialogPosAction(syncDialogType) },
                    negativeAction = { syncDialogNegAction(syncDialogType) },
                ),
                onDismiss = { dismissSyncDialog(syncDialogType) }
            )
        }

        CalendarSyncDialogType.EVENTS_DIALOG -> {
            CalendarAlertDialog(
                dialogProperties = DialogProperties(
                    title = "",
                    message = stringResource(syncDialogType.messageRes!!, calendarTitle),
                    positiveButton = stringResource(syncDialogType.positiveButtonRes!!),
                    negativeButton = stringResource(syncDialogType.negativeButtonRes!!),
                    positiveAction = { syncDialogPosAction(syncDialogType) },
                    negativeAction = { syncDialogNegAction(syncDialogType) },
                ),
                onDismiss = { dismissSyncDialog(syncDialogType) }
            )
        }

        CalendarSyncDialogType.OUT_OF_SYNC_DIALOG -> {
            CalendarAlertDialog(
                dialogProperties = DialogProperties(
                    title = stringResource(syncDialogType.titleRes!!, calendarTitle),
                    message = stringResource(syncDialogType.messageRes!!),
                    positiveButton = stringResource(syncDialogType.positiveButtonRes!!),
                    negativeButton = stringResource(syncDialogType.negativeButtonRes!!),
                    positiveAction = { syncDialogPosAction(syncDialogType) },
                    negativeAction = { syncDialogNegAction(syncDialogType) },
                ),
                onDismiss = { dismissSyncDialog(syncDialogType) }
            )
        }

        CalendarSyncDialogType.LOADING_DIALOG -> {
            SyncDialog()
        }

        CalendarSyncDialogType.NONE -> {
        }
    }
}

@Composable
private fun CalendarAlertDialog(dialogProperties: DialogProperties, onDismiss: () -> Unit) {
    AlertDialog(
        modifier = Modifier.background(
            color = MaterialTheme.appColors.background,
            shape = MaterialTheme.appShapes.cardShape
        ),
        shape = MaterialTheme.appShapes.cardShape,
        containerColor = MaterialTheme.appColors.background,

        properties = AlertDialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
        onDismissRequest = onDismiss,

        title = dialogProperties.title.takeIfNotEmpty()?.let {
            {
                Text(
                    text = dialogProperties.title,
                    color = MaterialTheme.appColors.textPrimary,
                    style = MaterialTheme.appTypography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        text = {
            Text(
                text = dialogProperties.message,
                color = MaterialTheme.appColors.textPrimary,
                style = MaterialTheme.appTypography.bodyMedium
            )
        },
        confirmButton = {
            TransparentTextButton(
                text = dialogProperties.positiveButton
            ) {
                onDismiss()
                dialogProperties.positiveAction.invoke()
            }
        },
        dismissButton = {
            TransparentTextButton(
                text = dialogProperties.negativeButton
            ) {
                onDismiss()
                dialogProperties.negativeAction.invoke()
            }
        },
    )
}

@Composable
private fun SyncDialog() {
    Dialog(
        onDismissRequest = { },
        properties = androidx.compose.ui.window.DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
        content = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.appShapes.cardShape),
                shape = MaterialTheme.appShapes.cardShape,
                color = MaterialTheme.appColors.background,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.core_title_syncing_calendar),
                        color = MaterialTheme.appColors.textPrimary,
                        style = MaterialTheme.appTypography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(30.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.appColors.primary)
                    }
                }
            }
        }
    )
}
