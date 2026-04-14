package org.openedx.shared.db

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.openedx.core.DatabaseManager
import org.openedx.core.data.model.room.CourseCalendarEventEntity
import org.openedx.core.data.model.room.CourseCalendarStateEntity
import org.openedx.core.data.model.room.CourseEnrollmentDetailsEntity
import org.openedx.core.data.model.room.CourseProgressEntity
import org.openedx.core.data.model.room.CourseStructureEntity
import org.openedx.core.data.model.room.DownloadCoursePreview
import org.openedx.core.data.model.room.OfflineXBlockProgress
import org.openedx.core.data.model.room.VideoProgressEntity
import org.openedx.core.data.model.room.discovery.EnrolledCourseEntity
import org.openedx.core.data.storage.CourseDao
import org.openedx.core.module.db.CalendarDao
import org.openedx.core.module.db.DownloadDao
import org.openedx.core.module.db.DownloadModelEntity
import org.openedx.core.data.model.room.CourseDateEntity
import org.openedx.dashboard.data.DashboardDao
import org.openedx.dates.data.storage.DatesDao
import org.openedx.discovery.data.model.room.CourseEntity
import org.openedx.discovery.data.storage.DiscoveryDao

/**
 * iOS DAO stubs — no-op in-memory shims that satisfy the interface surface so
 * Koin can wire repos/interactors/ViewModels on iOS.
 *
 * TODO iOS: replace with Room KMP setup (sqlite-bundled driver + KSP). See
 * gradle/libs.versions.toml — `room = 2.7.2` and `sqliteBundled = 2.5.0` are
 * already in libs but only `kspAndroid` is enabled, so generated impls land
 * for Android only. Until that's wired, iOS reads always miss cache and writes
 * are dropped — feature works online, breaks offline.
 */

class StubCourseDao : CourseDao {
    override suspend fun getCourseStructureById(id: String): CourseStructureEntity? = null
    override suspend fun insertCourseStructureEntity(vararg courseStructureEntity: CourseStructureEntity) = Unit
    override suspend fun clearCourseStructure() = Unit
    override suspend fun clearVideoProgress() = Unit
    override suspend fun clearEnrollmentCachedData() = Unit
    override suspend fun clearCourseProgressData() = Unit
    override suspend fun insertCourseEnrollmentDetailsEntity(vararg courseEnrollmentDetailsEntity: CourseEnrollmentDetailsEntity) = Unit
    override suspend fun getCourseEnrollmentDetailsById(id: String): CourseEnrollmentDetailsEntity? = null
    override suspend fun insertVideoProgressEntity(vararg videoProgressEntity: VideoProgressEntity) = Unit
    override suspend fun getVideoProgressByBlockId(blockId: String): VideoProgressEntity? = null
    override suspend fun insertCourseProgressEntity(vararg courseProgressEntity: CourseProgressEntity) = Unit
    override suspend fun getCourseProgressById(id: String): CourseProgressEntity? = null
}

class StubDashboardDao : DashboardDao {
    override suspend fun insertEnrolledCourseEntity(vararg courseEntity: EnrolledCourseEntity) = Unit
    override suspend fun clearCachedData() = Unit
    override suspend fun readAllData(): List<EnrolledCourseEntity> = emptyList()
}

class StubDiscoveryDao : DiscoveryDao {
    override suspend fun getCourseById(id: String): CourseEntity? = null
    override suspend fun insertCourseEntity(vararg courseEntity: CourseEntity) = Unit
    override suspend fun updateCourseEntity(courseEntity: CourseEntity) = Unit
    override suspend fun clearCachedData() = Unit
    override suspend fun readAllData(): List<CourseEntity> = emptyList()
}

class StubDatesDao : DatesDao {
    override suspend fun getCourseDates(): List<CourseDateEntity> = emptyList()
    override suspend fun getCourseDates(limit: Int): List<CourseDateEntity> = emptyList()
    override suspend fun insertCourseDates(courseDates: List<CourseDateEntity>) = Unit
    override suspend fun clearCachedData() = Unit
}

class StubDownloadDao : DownloadDao {
    override suspend fun removeDownloadModel(id: String) = Unit
    override suspend fun insertDownloadModel(downloadModelEntities: List<DownloadModelEntity>) = Unit
    override suspend fun updateDownloadModel(downloadModelEntity: DownloadModelEntity) = Unit
    override fun getAllDataFlow(): Flow<List<DownloadModelEntity>> = flowOf(emptyList())
    override suspend fun readAllData(): List<DownloadModelEntity> = emptyList()
    override fun readAllDataByIds(ids: List<String>): Flow<List<DownloadModelEntity>> = flowOf(emptyList())
    override suspend fun removeAllDownloadModels(ids: List<String>) = Unit
    override suspend fun getDownloadModelsByCourseIds(courseId: String): List<DownloadModelEntity> = emptyList()
    override suspend fun insertOfflineXBlockProgress(offlineXBlockProgress: OfflineXBlockProgress) = Unit
    override suspend fun getOfflineXBlockProgress(id: String): OfflineXBlockProgress? = null
    override suspend fun getAllOfflineXBlockProgress(): List<OfflineXBlockProgress> = emptyList()
    override suspend fun removeOfflineXBlockProgress(ids: List<String>) = Unit
    override suspend fun clearOfflineProgress() = Unit
    override suspend fun insertDownloadCoursePreview(downloadCoursePreview: List<DownloadCoursePreview>) = Unit
    override fun getDownloadCoursesPreview(): List<DownloadCoursePreview> = emptyList()
}

class StubCalendarDao : CalendarDao {
    override suspend fun insertCourseCalendarEntity(vararg courseCalendarEntity: CourseCalendarEventEntity) = Unit
    override suspend fun deleteCourseCalendarEntitiesById(courseId: String) = Unit
    override suspend fun readCourseCalendarEventsById(courseId: String): List<CourseCalendarEventEntity> = emptyList()
    override suspend fun readAllCourseCalendarEvents(): List<CourseCalendarEventEntity> = emptyList()
    override suspend fun clearCourseCalendarEventsCachedData() = Unit
    override suspend fun insertCourseCalendarStateEntity(vararg courseCalendarStateEntity: CourseCalendarStateEntity) = Unit
    override suspend fun readCourseCalendarStateById(courseId: String): CourseCalendarStateEntity? = null
    override suspend fun readAllCourseCalendarState(): List<CourseCalendarStateEntity> = emptyList()
    override suspend fun clearCourseCalendarStateCachedData() = Unit
    override suspend fun deleteCourseCalendarStateById(courseId: String) = Unit
    override suspend fun resetChecksums() = Unit
    override suspend fun updateCourseCalendarStateById(courseId: String, checksum: Int?, isCourseSyncEnabled: Boolean?) = Unit
}

class StubDatabaseManager : DatabaseManager {
    override fun clearTables() = Unit
}
