package org.openedx.core.domain.model

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.StringResource
import org.openedx.core.Res
import org.openedx.core.core_date_type_completed
import org.openedx.core.core_date_type_next_week
import org.openedx.core.core_date_type_none
import org.openedx.core.core_date_type_past_due
import org.openedx.core.core_date_type_this_week
import org.openedx.core.core_date_type_today
import org.openedx.core.core_date_type_upcoming
import org.openedx.core.ui.theme.appColors

val DatesSection.stringRes: StringResource
    get() = when (this) {
        DatesSection.COMPLETED -> Res.string.core_date_type_completed
        DatesSection.PAST_DUE -> Res.string.core_date_type_past_due
        DatesSection.TODAY -> Res.string.core_date_type_today
        DatesSection.THIS_WEEK -> Res.string.core_date_type_this_week
        DatesSection.NEXT_WEEK -> Res.string.core_date_type_next_week
        DatesSection.UPCOMING -> Res.string.core_date_type_upcoming
        DatesSection.NONE -> Res.string.core_date_type_none
    }

val DatesSection.color: Color
    @Composable
    get() {
        return when (this) {
            DatesSection.COMPLETED -> MaterialTheme.appColors.cardViewBackground
            DatesSection.PAST_DUE -> MaterialTheme.appColors.datesSectionBarPastDue
            DatesSection.TODAY -> MaterialTheme.appColors.datesSectionBarToday
            DatesSection.THIS_WEEK -> MaterialTheme.appColors.datesSectionBarThisWeek
            DatesSection.NEXT_WEEK -> MaterialTheme.appColors.datesSectionBarNextWeek
            DatesSection.UPCOMING -> MaterialTheme.appColors.datesSectionBarUpcoming
            else -> MaterialTheme.appColors.background
        }
    }
