package org.openedx.core.presentation.dialog.downloaddialog

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.openedx.core.Res
import org.openedx.core.core_cancel
import org.openedx.core.core_close
import org.openedx.core.course_confirm_download
import org.openedx.core.core_device_storage_full
import org.openedx.core.core_download
import org.openedx.core.core_download_confirm_dialog_description
import org.openedx.core.core_download_device_storage_full_dialog_description
import org.openedx.core.core_download_failed
import org.openedx.core.core_download_failed_dialog_description
import org.openedx.core.core_download_no_internet_dialog_description
import org.openedx.core.core_download_on_cellural
import org.openedx.core.core_download_on_cellural_dialog_description
import org.openedx.core.core_download_remove_dialog_description
import org.openedx.core.core_download_remove_offline_content
import org.openedx.core.core_download_wifi_required_dialog_description
import org.openedx.core.core_error_try_again
import org.openedx.core.core_ic_error
import org.openedx.core.core_ic_warning
import org.openedx.core.core_no_internet_connection
import org.openedx.core.core_remove
import org.openedx.core.core_used_free_storage
import org.openedx.core.core_wifi_required
import org.openedx.core.presentation.dialog.DefaultDialogBox
import org.openedx.core.system.StorageManager
import org.openedx.core.ui.AutoSizeText
import org.openedx.core.ui.IconText
import org.openedx.core.ui.OpenEdXButton
import org.openedx.core.ui.OpenEdXOutlinedButton
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appTypography
import org.openedx.foundation.extension.toFileSize

private val LIST_MAX_HEIGHT = 200.dp
private const val STORAGE_BAR_MIN_SIZE = 0.1f

@Composable
fun DownloadDialogHost(
    manager: DownloadDialogManager,
    storageManager: StorageManager,
) {
    val pending by manager.pendingDialog.collectAsState()
    val current = pending ?: return

    when (current.dialogType) {
        DownloadDialogType.CONFIRM_DOWNLOAD,
        DownloadDialogType.DOWNLOAD_ON_CELLULAR,
        DownloadDialogType.REMOVE_DOWNLOAD -> DownloadConfirmDialogView(
            dialogType = current.dialogType,
            uiState = current.uiState,
            onConfirmClick = {
                current.uiState.saveDownloadModels()
                current.onConfirm()
            },
            onRemoveClick = {
                current.uiState.removeDownloadModels()
                manager.dismissDialog()
            },
            onCancelClick = current.onDismiss,
        )

        DownloadDialogType.NO_CONNECTION,
        DownloadDialogType.WIFI_REQUIRED,
        DownloadDialogType.DOWNLOAD_FAILED -> DownloadErrorDialogView(
            dialogType = current.dialogType,
            uiState = current.uiState,
            onTryAgainClick = {
                current.uiState.saveDownloadModels()
                manager.dismissDialog()
            },
            onCancelClick = current.onDismiss,
        )

        DownloadDialogType.STORAGE_ERROR -> DownloadStorageErrorDialogView(
            uiState = current.uiState,
            freeSpace = storageManager.getFreeStorage(),
            totalSpace = storageManager.getTotalStorage(),
            onCancelClick = current.onDismiss,
        )
    }
}

@Composable
private fun DownloadConfirmDialogView(
    dialogType: DownloadDialogType,
    uiState: DownloadDialogUIState,
    onConfirmClick: () -> Unit,
    onRemoveClick: () -> Unit,
    onCancelClick: () -> Unit,
) {
    val sizeSumString = uiState.sizeSum.toFileSize(1, false)
    val title: String
    val description: String
    val icon: ImageVector?
    when (dialogType) {
        DownloadDialogType.CONFIRM_DOWNLOAD -> {
            title = stringResource(Res.string.course_confirm_download)
            description = stringResource(Res.string.core_download_confirm_dialog_description, sizeSumString)
            icon = null
        }
        DownloadDialogType.DOWNLOAD_ON_CELLULAR -> {
            title = stringResource(Res.string.core_download_on_cellural)
            description = stringResource(Res.string.core_download_on_cellural_dialog_description, sizeSumString)
            icon = null
        }
        DownloadDialogType.REMOVE_DOWNLOAD -> {
            title = stringResource(Res.string.core_download_remove_offline_content)
            description = stringResource(Res.string.core_download_remove_dialog_description, sizeSumString)
            icon = null
        }
        else -> return
    }

    DefaultDialogBox(onDismissClick = onCancelClick) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DialogHeader(
                title = title,
                iconPainter = if (dialogType == DownloadDialogType.DOWNLOAD_ON_CELLULAR) {
                    Res.drawable.core_ic_warning
                } else {
                    null
                },
            )
            DownloadItemsList(uiState.downloadDialogItems)
            Text(
                text = description,
                style = MaterialTheme.appTypography.bodyMedium,
                color = MaterialTheme.appColors.textDark,
            )
            when (dialogType) {
                DownloadDialogType.REMOVE_DOWNLOAD -> OpenEdXButton(
                    text = stringResource(Res.string.core_remove),
                    backgroundColor = MaterialTheme.appColors.error,
                    onClick = onRemoveClick,
                    content = {
                        IconText(
                            text = stringResource(Res.string.core_remove),
                            icon = Icons.Rounded.Delete,
                            color = MaterialTheme.appColors.primaryButtonText,
                            textStyle = MaterialTheme.appTypography.labelLarge,
                        )
                    },
                )
                else -> OpenEdXButton(
                    text = stringResource(Res.string.core_download),
                    backgroundColor = MaterialTheme.appColors.secondaryButtonBackground,
                    onClick = onConfirmClick,
                    content = {
                        IconText(
                            text = stringResource(Res.string.core_download),
                            icon = Icons.Outlined.CloudDownload,
                            color = MaterialTheme.appColors.primaryButtonText,
                            textStyle = MaterialTheme.appTypography.labelLarge,
                        )
                    },
                )
            }
            OpenEdXOutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.core_cancel),
                backgroundColor = MaterialTheme.appColors.background,
                borderColor = MaterialTheme.appColors.primaryButtonBackground,
                textColor = MaterialTheme.appColors.primaryButtonBackground,
                onClick = onCancelClick,
            )
        }
    }
}

@Composable
private fun DownloadErrorDialogView(
    dialogType: DownloadDialogType,
    uiState: DownloadDialogUIState,
    onTryAgainClick: () -> Unit,
    onCancelClick: () -> Unit,
) {
    val title: String
    val description: String
    when (dialogType) {
        DownloadDialogType.NO_CONNECTION -> {
            title = stringResource(Res.string.core_no_internet_connection)
            description = stringResource(Res.string.core_download_no_internet_dialog_description)
        }
        DownloadDialogType.WIFI_REQUIRED -> {
            title = stringResource(Res.string.core_wifi_required)
            description = stringResource(Res.string.core_download_wifi_required_dialog_description)
        }
        DownloadDialogType.DOWNLOAD_FAILED -> {
            title = stringResource(Res.string.core_download_failed)
            description = stringResource(Res.string.core_download_failed_dialog_description)
        }
        else -> return
    }
    val dismissText = if (dialogType == DownloadDialogType.DOWNLOAD_FAILED) {
        stringResource(Res.string.core_cancel)
    } else {
        stringResource(Res.string.core_close)
    }

    DefaultDialogBox(onDismissClick = onCancelClick) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DialogHeader(title = title, iconPainter = Res.drawable.core_ic_error)
            DownloadItemsList(uiState.downloadDialogItems)
            Text(
                text = description,
                style = MaterialTheme.appTypography.bodyMedium,
                color = MaterialTheme.appColors.textDark,
            )
            if (dialogType == DownloadDialogType.DOWNLOAD_FAILED) {
                OpenEdXButton(
                    text = stringResource(Res.string.core_error_try_again),
                    backgroundColor = MaterialTheme.appColors.secondaryButtonBackground,
                    onClick = onTryAgainClick,
                )
            }
            OpenEdXOutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                text = dismissText,
                backgroundColor = MaterialTheme.appColors.background,
                borderColor = MaterialTheme.appColors.primaryButtonBackground,
                textColor = MaterialTheme.appColors.primaryButtonBackground,
                onClick = onCancelClick,
            )
        }
    }
}

@Composable
private fun DownloadStorageErrorDialogView(
    uiState: DownloadDialogUIState,
    freeSpace: Long,
    totalSpace: Long,
    onCancelClick: () -> Unit,
) {
    DefaultDialogBox(onDismissClick = onCancelClick) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DialogHeader(
                title = stringResource(Res.string.core_device_storage_full),
                iconPainter = Res.drawable.core_ic_error,
            )
            val scaledItems = remember(uiState.downloadDialogItems) {
                uiState.downloadDialogItems.map { it.copy(size = it.size * DownloadDialogManager.DOWNLOAD_SIZE_FACTOR) }
            }
            DownloadItemsList(scaledItems)
            StorageBar(
                freeSpace = freeSpace,
                totalSpace = totalSpace,
                requiredSpace = uiState.sizeSum * DownloadDialogManager.DOWNLOAD_SIZE_FACTOR,
            )
            Text(
                text = stringResource(Res.string.core_download_device_storage_full_dialog_description),
                style = MaterialTheme.appTypography.bodyMedium,
                color = MaterialTheme.appColors.textDark,
            )
            OpenEdXOutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.core_cancel),
                backgroundColor = MaterialTheme.appColors.background,
                borderColor = MaterialTheme.appColors.primaryButtonBackground,
                textColor = MaterialTheme.appColors.primaryButtonBackground,
                onClick = onCancelClick,
            )
        }
    }
}

@Composable
private fun DialogHeader(
    title: String,
    iconPainter: org.jetbrains.compose.resources.DrawableResource?,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        if (iconPainter != null) {
            Image(
                painter = painterResource(iconPainter),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        AutoSizeText(
            text = title,
            style = MaterialTheme.appTypography.titleLarge,
            color = MaterialTheme.appColors.textDark,
            minSize = MaterialTheme.appTypography.titleLarge.fontSize.value - 1,
        )
    }
}

@Composable
private fun DownloadItemsList(items: List<DownloadDialogItem>) {
    if (items.isEmpty()) return
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .heightIn(max = LIST_MAX_HEIGHT)
            .verticalScroll(scrollState),
    ) {
        items.forEach { DownloadDialogItem(downloadDialogItem = it) }
    }
}

@Composable
private fun StorageBar(
    freeSpace: Long,
    totalSpace: Long,
    requiredSpace: Long,
) {
    val cornerRadius = 2.dp
    val boxPadding = 1.dp
    val usedSpace = totalSpace - freeSpace
    val safeRequired = if (requiredSpace <= 0) 1L else requiredSpace
    val freePercentage = freeSpace / safeRequired.toFloat() + STORAGE_BAR_MIN_SIZE
    val reqPercentage = (safeRequired - freeSpace).coerceAtLeast(0L) / safeRequired.toFloat() + STORAGE_BAR_MIN_SIZE

    val animReqPercentage = remember { Animatable(Float.MIN_VALUE) }
    LaunchedEffect(reqPercentage) {
        animReqPercentage.animateTo(
            targetValue = reqPercentage,
            animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing),
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .background(MaterialTheme.appColors.background)
                .clip(RoundedCornerShape(cornerRadius))
                .border(
                    2.dp,
                    MaterialTheme.appColors.cardViewBorder,
                    RoundedCornerShape(cornerRadius * 2),
                )
                .padding(2.dp)
                .background(MaterialTheme.appColors.background),
        ) {
            Box(
                modifier = Modifier
                    .weight(freePercentage)
                    .fillMaxHeight()
                    .padding(top = boxPadding, bottom = boxPadding, start = boxPadding, end = boxPadding / 2)
                    .clip(RoundedCornerShape(topStart = cornerRadius, bottomStart = cornerRadius))
                    .background(MaterialTheme.appColors.cardViewBorder),
            )
            Box(
                modifier = Modifier
                    .weight(animReqPercentage.value)
                    .fillMaxHeight()
                    .padding(top = boxPadding, bottom = boxPadding, end = boxPadding, start = boxPadding / 2)
                    .clip(RoundedCornerShape(topEnd = cornerRadius, bottomEnd = cornerRadius))
                    .background(MaterialTheme.appColors.error),
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(
                    Res.string.core_used_free_storage,
                    usedSpace.toFileSize(1, false),
                    freeSpace.toFileSize(1, false),
                ),
                style = MaterialTheme.appTypography.labelSmall,
                color = MaterialTheme.appColors.textFieldHint,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = requiredSpace.toFileSize(1, false),
                style = MaterialTheme.appTypography.labelSmall,
                color = MaterialTheme.appColors.error,
            )
        }
    }
}
