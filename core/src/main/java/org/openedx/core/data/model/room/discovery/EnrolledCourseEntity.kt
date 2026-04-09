package org.openedx.core.data.model.room.discovery

import kotlinx.serialization.Serializable

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.openedx.core.data.model.DateType
import org.openedx.core.data.model.room.MediaDb
import org.openedx.core.domain.model.Certificate
import org.openedx.core.domain.model.CourseAccessDetails
import org.openedx.core.domain.model.CourseAssignments
import org.openedx.core.domain.model.CourseDateBlock
import org.openedx.core.domain.model.CourseSharingUtmParameters
import org.openedx.core.domain.model.CourseStatus
import org.openedx.core.domain.model.CoursewareAccess
import org.openedx.core.domain.model.EnrolledCourse
import org.openedx.core.domain.model.EnrolledCourseData
import org.openedx.core.domain.model.EnrollmentDetails
import org.openedx.core.domain.model.Progress
import org.openedx.core.utils.InstantUtils

@Entity(tableName = "course_enrolled_table")
data class EnrolledCourseEntity(
    @PrimaryKey
    @ColumnInfo("courseId")
    val courseId: String,
    @ColumnInfo("auditAccessExpires")
    val auditAccessExpires: String,
    @ColumnInfo("created")
    val created: String,
    @ColumnInfo("mode")
    val mode: String,
    @ColumnInfo("isActive")
    val isActive: Boolean,
    @Embedded
    val course: EnrolledCourseDataDb,
    @Embedded
    val certificate: CertificateDb?,
    @Embedded
    val progress: ProgressDb,
    @Embedded
    val courseStatus: CourseStatusDb?,
    @Embedded
    val courseAssignments: CourseAssignmentsDb?,
) {

    fun mapToDomain(): EnrolledCourse {
        return EnrolledCourse(
            InstantUtils.iso8601ToInstant(auditAccessExpires),
            created,
            mode,
            isActive,
            course.mapToDomain(),
            certificate?.mapToDomain(),
            progress.mapToDomain(),
            courseStatus?.mapToDomain(),
            courseAssignments?.mapToDomain()
        )
    }
}

@Serializable
data class EnrolledCourseDataDb(
    @ColumnInfo("id")
    val id: String,
    @ColumnInfo("name")
    val name: String,
    @ColumnInfo("number")
    val number: String,
    @ColumnInfo("org")
    val org: String,
    @ColumnInfo("start")
    val start: String,
    @ColumnInfo("startDisplay")
    val startDisplay: String,
    @ColumnInfo("startType")
    val startType: String,
    @ColumnInfo("end")
    val end: String,
    @ColumnInfo("dynamicUpgradeDeadline")
    val dynamicUpgradeDeadline: String,
    @ColumnInfo("subscriptionId")
    val subscriptionId: String,
    @Embedded
    val coursewareAccess: CoursewareAccessDb?,
    @Embedded
    val media: MediaDb?,
    @ColumnInfo(name = "course_image_link")
    val courseImage: String,
    @ColumnInfo("courseAbout")
    val courseAbout: String,
    @Embedded
    val courseSharingUtmParameters: CourseSharingUtmParametersDb,
    @ColumnInfo("courseUpdates")
    val courseUpdates: String,
    @ColumnInfo("courseHandouts")
    val courseHandouts: String,
    @ColumnInfo("discussionUrl")
    val discussionUrl: String,
    @ColumnInfo("videoOutline")
    val videoOutline: String,
    @ColumnInfo("isSelfPaced")
    val isSelfPaced: Boolean,
) {
    fun mapToDomain(): EnrolledCourseData {
        return EnrolledCourseData(
            id,
            name,
            number,
            org,
            InstantUtils.iso8601ToInstant(start),
            startDisplay,
            startType,
            InstantUtils.iso8601ToInstant(end),
            dynamicUpgradeDeadline,
            subscriptionId,
            coursewareAccess?.mapToDomain(),
            media?.mapToDomain(),
            courseImage,
            courseAbout,
            courseSharingUtmParameters.mapToDomain(),
            courseUpdates,
            courseHandouts,
            discussionUrl,
            videoOutline,
            isSelfPaced
        )
    }
}

@Serializable
data class CoursewareAccessDb(
    @ColumnInfo("hasAccess")
    val hasAccess: Boolean,
    @ColumnInfo("errorCode")
    val errorCode: String,
    @ColumnInfo("developerMessage")
    val developerMessage: String,
    @ColumnInfo("userMessage")
    val userMessage: String,
    @ColumnInfo("additionalContextUserMessage")
    val additionalContextUserMessage: String,
    @ColumnInfo("userFragment")
    val userFragment: String,
) {

    fun mapToDomain(): CoursewareAccess {
        return CoursewareAccess(
            hasAccess,
            errorCode,
            developerMessage,
            userMessage,
            additionalContextUserMessage,
            userFragment
        )
    }
}

@Serializable
data class CertificateDb(
    @ColumnInfo("certificateURL")
    val certificateURL: String?,
) {
    fun mapToDomain() = Certificate(certificateURL)
}

@Serializable
data class CourseSharingUtmParametersDb(
    @ColumnInfo("facebook")
    val facebook: String,
    @ColumnInfo("twitter")
    val twitter: String,
) {
    fun mapToDomain() = CourseSharingUtmParameters(
        facebook,
        twitter
    )
}

@Serializable
data class ProgressDb(
    @ColumnInfo("assignments_completed")
    val assignmentsCompleted: Int,
    @ColumnInfo("total_assignments_count")
    val totalAssignmentsCount: Int,
) {
    companion object {
        val DEFAULT_PROGRESS = ProgressDb(0, 0)
    }

    fun mapToDomain() = Progress(assignmentsCompleted, totalAssignmentsCount)
}

@Serializable
data class CourseStatusDb(
    @ColumnInfo("lastVisitedModuleId")
    val lastVisitedModuleId: String,
    @ColumnInfo("lastVisitedModulePath")
    val lastVisitedModulePath: List<String>,
    @ColumnInfo("lastVisitedBlockId")
    val lastVisitedBlockId: String,
    @ColumnInfo("lastVisitedUnitDisplayName")
    val lastVisitedUnitDisplayName: String,
) {
    fun mapToDomain() = CourseStatus(
        lastVisitedModuleId,
        lastVisitedModulePath,
        lastVisitedBlockId,
        lastVisitedUnitDisplayName
    )
}

@Serializable
data class CourseAssignmentsDb(
    @ColumnInfo("futureAssignments")
    val futureAssignments: List<CourseDateBlockDb>?,
    @ColumnInfo("pastAssignments")
    val pastAssignments: List<CourseDateBlockDb>?,
) {
    fun mapToDomain() = CourseAssignments(
        futureAssignments = futureAssignments?.map { it.mapToDomain() },
        pastAssignments = pastAssignments?.map { it.mapToDomain() }
    )
}

@Serializable
data class CourseDateBlockDb(
    @ColumnInfo("title")
    val title: String = "",
    @ColumnInfo("description")
    val description: String = "",
    @ColumnInfo("link")
    val link: String = "",
    @ColumnInfo("blockId")
    val blockId: String = "",
    @ColumnInfo("learnerHasAccess")
    val learnerHasAccess: Boolean = false,
    @ColumnInfo("complete")
    val complete: Boolean = false,
    @ColumnInfo("date")
    val date: Long,
    @ColumnInfo("dateType")
    val dateType: DateType = DateType.NONE,
    @ColumnInfo("assignmentType")
    val assignmentType: String? = "",
) {
    fun mapToDomain() = CourseDateBlock(
        title = title,
        description = description,
        link = link,
        blockId = blockId,
        learnerHasAccess = learnerHasAccess,
        complete = complete,
        date = kotlinx.datetime.Instant.fromEpochMilliseconds(date),
        dateType = dateType,
        assignmentType = assignmentType
    )
}

@Serializable
data class EnrollmentDetailsDB(
    @ColumnInfo("created")
    var created: String?,
    @ColumnInfo("mode")
    var mode: String?,
    @ColumnInfo("isActive")
    var isActive: Boolean,
    @ColumnInfo("upgradeDeadline")
    var upgradeDeadline: String?,
) {
    fun mapToDomain() = EnrollmentDetails(
        InstantUtils.iso8601ToInstant(created ?: ""),
        mode,
        isActive,
        InstantUtils.iso8601ToInstant(upgradeDeadline ?: "")
    )
}

@Serializable
data class CourseAccessDetailsDb(
    @ColumnInfo("hasUnmetPrerequisites")
    val hasUnmetPrerequisites: Boolean,
    @ColumnInfo("isTooEarly")
    val isTooEarly: Boolean,
    @ColumnInfo("isStaff")
    val isStaff: Boolean,
    @ColumnInfo("auditAccessExpires")
    var auditAccessExpires: String?,
    @Embedded
    val coursewareAccess: CoursewareAccessDb?,
) {
    fun mapToDomain(): CourseAccessDetails {
        return CourseAccessDetails(
            hasUnmetPrerequisites = hasUnmetPrerequisites,
            isTooEarly = isTooEarly,
            isStaff = isStaff,
            auditAccessExpires = InstantUtils.iso8601ToInstant(auditAccessExpires ?: ""),
            coursewareAccess = coursewareAccess?.mapToDomain()
        )
    }
}
