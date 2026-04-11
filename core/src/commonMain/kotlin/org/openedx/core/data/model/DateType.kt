package org.openedx.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class DateType {
    @SerialName("todays-date")
    TODAY_DATE,

    @SerialName("course-start-date")
    COURSE_START_DATE,

    @SerialName("course-end-date")
    COURSE_END_DATE,

    @SerialName("course-expired-date")
    COURSE_EXPIRED_DATE,

    @SerialName("assignment-due-date")
    ASSIGNMENT_DUE_DATE,

    @SerialName("certificate-available-date")
    CERTIFICATE_AVAILABLE_DATE,

    @SerialName("verified-upgrade-deadline")
    VERIFIED_UPGRADE_DEADLINE,

    @SerialName("verification-deadline-date")
    VERIFICATION_DEADLINE_DATE,

    NONE,
}
