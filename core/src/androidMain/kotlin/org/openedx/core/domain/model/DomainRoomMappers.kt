package org.openedx.core.domain.model

import org.openedx.core.data.model.room.BannerImageDb
import org.openedx.core.data.model.room.CourseEnrollmentDetailsEntity
import org.openedx.core.data.model.room.CourseImageDb
import org.openedx.core.data.model.room.CourseInfoOverviewDb
import org.openedx.core.data.model.room.CourseVideoDb
import org.openedx.core.data.model.room.ImageDb
import org.openedx.core.data.model.room.MediaDb
import org.openedx.core.data.model.room.discovery.CertificateDb
import org.openedx.core.data.model.room.discovery.CourseAccessDetailsDb
import org.openedx.core.data.model.room.discovery.CourseSharingUtmParametersDb
import org.openedx.core.data.model.room.discovery.CoursewareAccessDb
import org.openedx.core.data.model.room.discovery.EnrollmentDetailsDB
import kotlinx.datetime.Instant

// Certificate -> CertificateDb
fun Certificate.mapToRoomEntity() = CertificateDb(certificateURL)

// CourseAccessDetails -> CourseAccessDetailsDb
fun CourseAccessDetails.mapToRoomEntity(): CourseAccessDetailsDb =
    CourseAccessDetailsDb(
        hasUnmetPrerequisites = hasUnmetPrerequisites,
        isTooEarly = isTooEarly,
        isStaff = isStaff,
        auditAccessExpires = auditAccessExpires?.toString(),
        coursewareAccess = coursewareAccess?.mapToEntity()
    )

// CourseEnrollmentDetails -> CourseEnrollmentDetailsEntity
fun CourseEnrollmentDetails.mapToEntity() = CourseEnrollmentDetailsEntity(
    id = id,
    courseUpdates = courseUpdates,
    courseHandouts = courseHandouts,
    discussionUrl = discussionUrl,
    courseAccessDetails = courseAccessDetails.mapToRoomEntity(),
    certificate = certificate?.mapToRoomEntity(),
    enrollmentDetails = enrollmentDetails.mapToEntity(),
    courseInfoOverview = courseInfoOverview.mapToEntity()
)

// CourseInfoOverview -> CourseInfoOverviewDb
fun CourseInfoOverview.mapToEntity() = CourseInfoOverviewDb(
    name = name,
    number = number,
    org = org,
    start = start?.toEpochMilliseconds(),
    startDisplay = startDisplay ?: "",
    startType = startType,
    end = end?.toEpochMilliseconds(),
    isSelfPaced = isSelfPaced,
    media = media?.mapToEntity(),
    courseSharingUtmParameters = courseSharingUtmParameters.mapToEntity(),
    courseAbout = courseAbout
)

// CourseSharingUtmParameters -> CourseSharingUtmParametersDb
fun CourseSharingUtmParameters.mapToEntity() = CourseSharingUtmParametersDb(
    facebook = facebook,
    twitter = twitter
)

// CoursewareAccess -> CoursewareAccessDb
fun CoursewareAccess.mapToEntity() = CoursewareAccessDb(
    hasAccess = hasAccess,
    errorCode = errorCode,
    developerMessage = developerMessage,
    userMessage = userMessage,
    additionalContextUserMessage = additionalContextUserMessage,
    userFragment = userFragment
)

// EnrollmentDetails -> EnrollmentDetailsDB
fun EnrollmentDetails.mapToEntity() = EnrollmentDetailsDB(
    created = created?.toString(),
    mode = mode,
    isActive = isActive,
    upgradeDeadline = upgradeDeadline?.toString()
)

// Media -> MediaDb
fun Media.mapToEntity() = MediaDb(
    bannerImage = bannerImage?.mapToEntity(),
    courseImage = courseImage?.mapToEntity(),
    courseVideo = courseVideo?.mapToEntity(),
    image = image?.mapToEntity()
)

// Image -> ImageDb
fun Image.mapToEntity() = ImageDb(large, raw, small)

// CourseVideo -> CourseVideoDb
fun CourseVideo.mapToEntity() = CourseVideoDb(uri)

// CourseImage -> CourseImageDb
fun CourseImage.mapToEntity() = CourseImageDb(uri, name)

// BannerImage -> BannerImageDb
fun BannerImage.mapToEntity() = BannerImageDb(uri, uriAbsolute)
