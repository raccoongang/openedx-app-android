package org.openedx.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.core.R

enum class DateType(val drawableResId: Int? = null) {
    @SerialName("todays-date")
    TODAY_DATE(R.drawable.core_ic_calendar),

    @SerialName("course-start-date")
    COURSE_START_DATE(R.drawable.core_ic_start_end),

    @SerialName("course-end-date")
    COURSE_END_DATE(R.drawable.core_ic_start_end),

    @SerialName("course-expired-date")
    COURSE_EXPIRED_DATE(R.drawable.core_ic_course_expire),

    @SerialName("assignment-due-date")
    ASSIGNMENT_DUE_DATE(R.drawable.core_ic_assignment),

    @SerialName("certificate-available-date")
    CERTIFICATE_AVAILABLE_DATE(R.drawable.core_ic_certificate),

    @SerialName("verified-upgrade-deadline")
    VERIFIED_UPGRADE_DEADLINE(R.drawable.core_ic_calendar),

    @SerialName("verification-deadline-date")
    VERIFICATION_DEADLINE_DATE(R.drawable.core_ic_calendar),

    NONE,
}
