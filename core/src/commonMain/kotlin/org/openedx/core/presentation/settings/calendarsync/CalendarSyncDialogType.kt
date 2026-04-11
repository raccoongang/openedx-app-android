package org.openedx.core.presentation.settings.calendarsync

import org.jetbrains.compose.resources.StringResource
import org.openedx.core.Res
import org.openedx.core.core_cancel
import org.openedx.core.core_label_do_not_allow
import org.openedx.core.core_label_done
import org.openedx.core.core_label_remove
import org.openedx.core.core_label_remove_course_calendar
import org.openedx.core.core_label_update_now
import org.openedx.core.core_label_view_events
import org.openedx.core.core_message_add_course_calendar
import org.openedx.core.core_message_calendar_out_of_date
import org.openedx.core.core_message_course_calendar_added
import org.openedx.core.core_message_remove_course_calendar
import org.openedx.core.core_message_request_calendar_permission
import org.openedx.core.core_ok
import org.openedx.core.core_title_add_course_calendar
import org.openedx.core.core_title_calendar_out_of_date
import org.openedx.core.core_title_remove_course_calendar
import org.openedx.core.core_title_request_calendar_permission
import org.openedx.core.core_title_syncing_calendar

enum class CalendarSyncDialogType(
    val titleRes: StringResource? = null,
    val messageRes: StringResource? = null,
    val positiveButtonRes: StringResource? = null,
    val negativeButtonRes: StringResource? = null,
) {
    SYNC_DIALOG(
        titleRes = Res.string.core_title_add_course_calendar,
        messageRes = Res.string.core_message_add_course_calendar,
        positiveButtonRes = Res.string.core_ok,
        negativeButtonRes = Res.string.core_cancel
    ),
    UN_SYNC_DIALOG(
        titleRes = Res.string.core_title_remove_course_calendar,
        messageRes = Res.string.core_message_remove_course_calendar,
        positiveButtonRes = Res.string.core_label_remove,
        negativeButtonRes = Res.string.core_cancel
    ),
    PERMISSION_DIALOG(
        titleRes = Res.string.core_title_request_calendar_permission,
        messageRes = Res.string.core_message_request_calendar_permission,
        positiveButtonRes = Res.string.core_ok,
        negativeButtonRes = Res.string.core_label_do_not_allow
    ),
    EVENTS_DIALOG(
        messageRes = Res.string.core_message_course_calendar_added,
        positiveButtonRes = Res.string.core_label_view_events,
        negativeButtonRes = Res.string.core_label_done
    ),
    OUT_OF_SYNC_DIALOG(
        titleRes = Res.string.core_title_calendar_out_of_date,
        messageRes = Res.string.core_message_calendar_out_of_date,
        positiveButtonRes = Res.string.core_label_update_now,
        negativeButtonRes = Res.string.core_label_remove_course_calendar,
    ),
    LOADING_DIALOG(
        titleRes = Res.string.core_title_syncing_calendar
    ),
    NONE
}
