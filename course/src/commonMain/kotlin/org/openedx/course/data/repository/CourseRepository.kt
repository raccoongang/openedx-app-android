package org.openedx.course.data.repository

import kotlinx.coroutines.flow.Flow
import org.openedx.core.domain.model.AnnouncementModel
import org.openedx.core.domain.model.CourseComponentStatus
import org.openedx.core.domain.model.CourseDatesBannerInfo
import org.openedx.core.domain.model.CourseDatesResult
import org.openedx.core.domain.model.ResetCourseDates
import org.openedx.core.domain.model.CourseEnrollmentDetails
import org.openedx.core.domain.model.CourseProgress
import org.openedx.core.domain.model.CourseStructure
import org.openedx.core.domain.model.HandoutsModel
import org.openedx.core.domain.model.VideoProgress
import org.openedx.core.module.db.DownloadModel

interface CourseRepository {
    fun startCourseSession(courseId: String)
    fun endCourseSession()
    fun getCourseStructureFlow(courseId: String, forceRefresh: Boolean = false): Flow<CourseStructure>
    suspend fun getCourseStructureFromCache(courseId: String): CourseStructure
    fun getEnrollmentDetailsFlow(courseId: String, forceRefresh: Boolean = false): Flow<CourseEnrollmentDetails>
    suspend fun getEnrollmentDetails(courseId: String): CourseEnrollmentDetails
    fun getCourseStatusFlow(courseId: String, forceRefresh: Boolean = false): Flow<CourseComponentStatus>
    suspend fun getCourseStatus(courseId: String): CourseComponentStatus
    fun getCourseDatesFlow(courseId: String, forceRefresh: Boolean = false): Flow<CourseDatesResult>
    suspend fun getCourseDates(courseId: String, forceRefresh: Boolean = false): CourseDatesResult
    fun getCourseProgress(courseId: String, isRefresh: Boolean, getOnlyCacheIfExist: Boolean): Flow<CourseProgress>
    suspend fun markBlocksCompletion(courseId: String, blocksId: List<String>)
    suspend fun resetCourseDates(courseId: String): ResetCourseDates
    suspend fun getDatesBannerInfo(courseId: String): CourseDatesBannerInfo
    suspend fun getHandouts(courseId: String): HandoutsModel
    suspend fun getAnnouncements(courseId: String): List<AnnouncementModel>
    suspend fun removeDownloadModel(id: String)
    fun getDownloadModels(): Flow<List<DownloadModel>>
    suspend fun getAllDownloadModels(): List<DownloadModel>
    suspend fun saveOfflineXBlockProgress(blockId: String, courseId: String, jsonProgress: String)
    suspend fun getXBlockProgressJson(blockId: String): String?
    suspend fun submitAllOfflineXBlockProgress()
    suspend fun submitOfflineXBlockProgress(blockId: String, courseId: String)
    suspend fun saveVideoProgress(blockId: String, videoUrl: String, videoTime: Long, duration: Long)
    suspend fun getVideoProgress(blockId: String): VideoProgress
}
