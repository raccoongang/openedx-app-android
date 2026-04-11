package org.openedx.core.presentation.settings.calendarsync

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.SyncDisabled
import androidx.compose.material.icons.rounded.EventRepeat
import androidx.compose.material.icons.rounded.FreeCancellation
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.StringResource
import org.openedx.core.Res
import org.openedx.core.core_calendar_sync_failed
import org.openedx.core.core_offline
import org.openedx.core.core_synced_to_calendar
import org.openedx.core.core_syncing_failed
import org.openedx.core.core_syncing_to_calendar
import org.openedx.core.core_to_sync
import org.openedx.core.ui.theme.appColors

enum class CalendarSyncState(
    val title: StringResource,
    val longTitle: StringResource,
    val icon: ImageVector
) {
    OFFLINE(
        Res.string.core_offline,
        Res.string.core_offline,
        Icons.Default.SyncDisabled
    ),
    SYNC_FAILED(
        Res.string.core_syncing_failed,
        Res.string.core_calendar_sync_failed,
        Icons.Rounded.FreeCancellation
    ),
    SYNCED(
        Res.string.core_to_sync,
        Res.string.core_synced_to_calendar,
        Icons.Rounded.EventRepeat
    ),
    SYNCHRONIZATION(
        Res.string.core_syncing_to_calendar,
        Res.string.core_syncing_to_calendar,
        Icons.Default.CloudSync
    );

    val tint: Color
        @Composable
        @ReadOnlyComposable
        get() = when (this) {
            OFFLINE -> MaterialTheme.appColors.textFieldHint
            SYNC_FAILED -> MaterialTheme.appColors.error
            SYNCED -> MaterialTheme.appColors.successGreen
            SYNCHRONIZATION -> MaterialTheme.appColors.primary
        }
}
