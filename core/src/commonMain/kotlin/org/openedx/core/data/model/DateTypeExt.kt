package org.openedx.core.data.model

import org.jetbrains.compose.resources.DrawableResource
import org.openedx.core.Res
import org.openedx.core.core_ic_assignment
import org.openedx.core.core_ic_calendar
import org.openedx.core.core_ic_certificate
import org.openedx.core.core_ic_course_expire
import org.openedx.core.core_ic_start_end

val DateType.drawableResId: DrawableResource?
    get() = when (this) {
        DateType.TODAY_DATE -> Res.drawable.core_ic_calendar
        DateType.COURSE_START_DATE -> Res.drawable.core_ic_start_end
        DateType.COURSE_END_DATE -> Res.drawable.core_ic_start_end
        DateType.COURSE_EXPIRED_DATE -> Res.drawable.core_ic_course_expire
        DateType.ASSIGNMENT_DUE_DATE -> Res.drawable.core_ic_assignment
        DateType.CERTIFICATE_AVAILABLE_DATE -> Res.drawable.core_ic_certificate
        DateType.VERIFIED_UPGRADE_DEADLINE -> Res.drawable.core_ic_calendar
        DateType.VERIFICATION_DEADLINE_DATE -> Res.drawable.core_ic_calendar
        DateType.NONE -> null
    }
