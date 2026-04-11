package org.openedx.core.data.model.room

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.openedx.core.data.model.room.discovery.CertificateDb
import org.openedx.core.data.model.room.discovery.CourseAccessDetailsDb
import org.openedx.core.data.model.room.discovery.CourseSharingUtmParametersDb
import org.openedx.core.data.model.room.discovery.EnrollmentDetailsDB
import kotlinx.datetime.Instant
import org.openedx.core.domain.model.CourseEnrollmentDetails
import org.openedx.core.domain.model.CourseInfoOverview

@Entity(tableName = "course_enrollment_details_table")
data class CourseEnrollmentDetailsEntity(
    @PrimaryKey
    @ColumnInfo("id")
    val id: String,
    @ColumnInfo("courseUpdates")
    val courseUpdates: String,
    @ColumnInfo("courseHandouts")
    val courseHandouts: String,
    @ColumnInfo("discussionUrl")
    val discussionUrl: String,
    @Embedded
    val courseAccessDetails: CourseAccessDetailsDb,
    @Embedded
    val certificate: CertificateDb?,
    @Embedded
    val enrollmentDetails: EnrollmentDetailsDB,
    @Embedded
    val courseInfoOverview: CourseInfoOverviewDb
) {
    fun mapToDomain() = CourseEnrollmentDetails(
        id = id,
        courseUpdates = courseUpdates,
        courseHandouts = courseHandouts,
        discussionUrl = discussionUrl,
        courseAccessDetails = courseAccessDetails.mapToDomain(),
        certificate = certificate?.mapToDomain(),
        enrollmentDetails = enrollmentDetails.mapToDomain(),
        courseInfoOverview = courseInfoOverview.mapToDomain()
    )
}

data class CourseInfoOverviewDb(
    @ColumnInfo("name")
    val name: String,
    @ColumnInfo("number")
    val number: String,
    @ColumnInfo("org")
    val org: String,
    @ColumnInfo("start")
    val start: Long?,
    @ColumnInfo("startDisplay")
    val startDisplay: String,
    @ColumnInfo("startType")
    val startType: String,
    @ColumnInfo("end")
    val end: Long?,
    @ColumnInfo("isSelfPaced")
    val isSelfPaced: Boolean,
    @Embedded
    var media: MediaDb?,
    @Embedded
    val courseSharingUtmParameters: CourseSharingUtmParametersDb,
    @ColumnInfo("courseAbout")
    val courseAbout: String,
) {
    fun mapToDomain() = CourseInfoOverview(
        name = name,
        number = number,
        org = org,
        start = start?.let { Instant.fromEpochMilliseconds(it) },
        startDisplay = startDisplay,
        startType = startType,
        end = end?.let { Instant.fromEpochMilliseconds(it) },
        isSelfPaced = isSelfPaced,
        media = media?.mapToDomain(),
        courseSharingUtmParameters = courseSharingUtmParameters.mapToDomain(),
        courseAbout = courseAbout
    )
}
