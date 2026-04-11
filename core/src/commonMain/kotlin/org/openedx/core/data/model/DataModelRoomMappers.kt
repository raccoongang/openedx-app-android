package org.openedx.core.data.model

import org.openedx.core.data.model.room.AssignmentProgressDb
import org.openedx.core.data.model.room.BlockDb
import org.openedx.core.data.model.room.CourseStructureEntity
import org.openedx.core.data.model.room.MediaDb
import org.openedx.core.data.model.room.OfflineDownloadDb
import org.openedx.core.data.model.room.discovery.CertificateDb
import org.openedx.core.data.model.room.discovery.CourseAccessDetailsDb
import org.openedx.core.data.model.room.discovery.CourseAssignmentsDb
import org.openedx.core.data.model.room.discovery.CourseDateBlockDb
import org.openedx.core.data.model.room.discovery.CourseSharingUtmParametersDb
import org.openedx.core.data.model.room.discovery.CourseStatusDb
import org.openedx.core.data.model.room.discovery.CoursewareAccessDb
import org.openedx.core.data.model.room.discovery.EnrolledCourseDataDb
import org.openedx.core.data.model.room.discovery.EnrolledCourseEntity
import org.openedx.core.data.model.room.discovery.EnrollmentDetailsDB
import org.openedx.core.data.model.room.discovery.ProgressDb
import org.openedx.core.utils.InstantUtils
import org.openedx.core.data.model.room.DownloadCoursePreview as EntityDownloadCoursePreview

fun AssignmentProgress.mapToRoomEntity() = AssignmentProgressDb(
    assignmentType = assignmentType,
    numPointsEarned = numPointsEarned,
    numPointsPossible = numPointsPossible,
    shortLabel = shortLabel
)

fun Certificate.mapToRoomEntity() = CertificateDb(certificateURL)

fun CourseAccessDetails.mapToRoomEntity(): CourseAccessDetailsDb =
    CourseAccessDetailsDb(
        hasUnmetPrerequisites = hasUnmetPrerequisites,
        isTooEarly = isTooEarly,
        isStaff = isStaff,
        auditAccessExpires = auditAccessExpires,
        coursewareAccess = coursewareAccess?.mapToRoomEntity()
    )

fun CourseAssignments.mapToRoomEntity() = CourseAssignmentsDb(
    futureAssignments = futureAssignments?.mapNotNull {
        it.mapToRoomEntity()
    },
    pastAssignments = pastAssignments?.mapNotNull {
        it.mapToRoomEntity()
    }
)

fun CourseDateBlock.mapToRoomEntity(): CourseDateBlockDb? {
    InstantUtils.iso8601ToInstant(date)?.let {
        return CourseDateBlockDb(
            complete = complete,
            date = it.toEpochMilliseconds(),
            assignmentType = assignmentType,
            dateType = dateType,
            description = description,
            learnerHasAccess = learnerHasAccess,
            link = link,
            title = title,
            blockId = blockId
        )
    } ?: return null
}

fun CourseSharingUtmParameters.mapToRoomEntity() = CourseSharingUtmParametersDb(
    facebook = facebook ?: "",
    twitter = twitter ?: ""
)

fun CourseStatus.mapToRoomEntity() = CourseStatusDb(
    lastVisitedModuleId = lastVisitedModuleId ?: "",
    lastVisitedModulePath = lastVisitedModulePath ?: emptyList(),
    lastVisitedBlockId = lastVisitedBlockId ?: "",
    lastVisitedUnitDisplayName = lastVisitedUnitDisplayName ?: ""
)

fun CourseStructureModel.mapToRoomEntity(): CourseStructureEntity {
    return CourseStructureEntity(
        root,
        blocks = blockData.map { BlockDb.createFrom(it.value) },
        id = id ?: "",
        name = name ?: "",
        number = number ?: "",
        org = org ?: "",
        start = start ?: "",
        startDisplay = startDisplay ?: "",
        startType = startType ?: "",
        end = end ?: "",
        coursewareAccess = coursewareAccess?.mapToRoomEntity(),
        media = MediaDb.createFrom(media),
        certificate = certificate?.mapToRoomEntity(),
        isSelfPaced = isSelfPaced ?: false,
        progress = progress?.mapToRoomEntity() ?: ProgressDb.DEFAULT_PROGRESS,
    )
}

fun CoursewareAccess.mapToRoomEntity(): CoursewareAccessDb {
    return CoursewareAccessDb(
        hasAccess = hasAccess ?: false,
        errorCode = errorCode ?: "",
        developerMessage = developerMessage ?: "",
        userMessage = userMessage ?: "",
        additionalContextUserMessage = additionalContextUserMessage ?: "",
        userFragment = userFragment ?: ""
    )
}

fun DownloadCoursePreview.mapToRoomEntity(): EntityDownloadCoursePreview {
    return EntityDownloadCoursePreview(
        id = id,
        name = name,
        image = image,
        totalSize = totalSize,
    )
}

fun EnrolledCourse.mapToRoomEntity(): EnrolledCourseEntity {
    return EnrolledCourseEntity(
        courseId = course?.id ?: "",
        auditAccessExpires = auditAccessExpires ?: "",
        created = created ?: "",
        mode = mode ?: "",
        isActive = isActive ?: false,
        course = course?.mapToRoomEntity()!!,
        certificate = certificate?.mapToRoomEntity(),
        progress = progress?.mapToRoomEntity() ?: ProgressDb.DEFAULT_PROGRESS,
        courseStatus = courseStatus?.mapToRoomEntity(),
        courseAssignments = courseAssignments?.mapToRoomEntity()
    )
}

fun EnrolledCourseData.mapToRoomEntity(): EnrolledCourseDataDb {
    return EnrolledCourseDataDb(
        id = id.orEmpty(),
        name = name.orEmpty(),
        number = number.orEmpty(),
        org = org.orEmpty(),
        start = start.orEmpty(),
        startDisplay = startDisplay.orEmpty(),
        startType = startType.orEmpty(),
        end = end.orEmpty(),
        dynamicUpgradeDeadline = dynamicUpgradeDeadline.orEmpty(),
        subscriptionId = subscriptionId.orEmpty(),
        coursewareAccess = coursewareAccess?.mapToRoomEntity(),
        media = MediaDb.createFrom(media),
        courseImage = courseImage.orEmpty(),
        courseAbout = courseAbout.orEmpty(),
        courseSharingUtmParameters = courseSharingUtmParameters?.mapToRoomEntity()!!,
        courseUpdates = courseUpdates.orEmpty(),
        courseHandouts = courseHandouts.orEmpty(),
        discussionUrl = discussionUrl.orEmpty(),
        videoOutline = videoOutline.orEmpty(),
        isSelfPaced = isSelfPaced ?: false
    )
}

fun EnrollmentDetails.mapToRoomEntity() = EnrollmentDetailsDB(
    created = created,
    mode = mode,
    isActive = isActive,
    upgradeDeadline = upgradeDeadline,
)

fun OfflineDownload.mapToRoomEntity() = OfflineDownloadDb(
    fileUrl = fileUrl ?: "",
    lastModified = lastModified,
    fileSize = fileSize ?: 0
)

fun Progress.mapToRoomEntity() = ProgressDb(
    assignmentsCompleted = assignmentsCompleted ?: 0,
    totalAssignmentsCount = totalAssignmentsCount ?: 0
)
