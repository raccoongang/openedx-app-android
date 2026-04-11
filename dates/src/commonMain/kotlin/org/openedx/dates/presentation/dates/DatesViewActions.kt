package org.openedx.dates.presentation.dates

import org.openedx.core.domain.model.CourseDate

interface DatesViewActions {
    object OpenSettings : DatesViewActions
    class OpenEvent(val date: CourseDate) : DatesViewActions
    object LoadMore : DatesViewActions
    object SwipeRefresh : DatesViewActions
    object ShiftDueDate : DatesViewActions
}
