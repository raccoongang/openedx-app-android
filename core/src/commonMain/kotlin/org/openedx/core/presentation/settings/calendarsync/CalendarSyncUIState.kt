package org.openedx.core.presentation.settings.calendarsync

import org.openedx.core.domain.model.CourseDateBlock

data class CalendarSyncUIState(
    val isCalendarSyncEnabled: Boolean = false,
    val calendarTitle: String = "",
    val courseDates: List<CourseDateBlock> = listOf(),
    val dialogType: CalendarSyncDialogType = CalendarSyncDialogType.NONE,
    val isSynced: Boolean = false,
    val checkForOutOfSync: Boolean = false,
    val uiMessage: String = "",
) {
    val isDialogVisible: Boolean
        get() = dialogType != CalendarSyncDialogType.NONE
}
