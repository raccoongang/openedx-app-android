package org.openedx.shared.calendar

import androidx.compose.runtime.Composable

/**
 * Returns a trigger lambda that requests calendar read/write permission.
 * - Android: launches the WRITE_CALENDAR + READ_CALENDAR runtime permission dialog.
 * - iOS: calls EKEventStore.requestAccess, which shows the system prompt on first call.
 *
 * [onResult] is invoked with `true` when both permissions are granted.
 */
@Composable
expect fun rememberCalendarPermissionLauncher(
    onResult: (granted: Boolean) -> Unit,
): () -> Unit
