package org.openedx.downloads.data.repository

import kotlinx.coroutines.flow.Flow
import org.openedx.core.domain.model.CourseStructure
import org.openedx.core.domain.model.DownloadCoursePreview
import org.openedx.core.module.db.DownloadModel

interface DownloadRepository {
    fun getDownloadCoursesPreview(refresh: Boolean): Flow<List<DownloadCoursePreview>>
    suspend fun getCourseStructureFromCache(courseId: String): CourseStructure
    suspend fun getCourseStructure(courseId: String): CourseStructure
    suspend fun getDownloadModelsByCourseIds(courseId: String): List<DownloadModel>
}
