package org.openedx.shared.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.openedx.auth.data.repository.AuthRepository
import org.openedx.auth.domain.interactor.AuthInteractor
import org.openedx.auth.presentation.AgreementProvider
import org.openedx.auth.presentation.AuthAnalytics
import org.openedx.auth.presentation.IosAuthAnalytics
import org.openedx.auth.presentation.logistration.LogistrationViewModel
import org.openedx.auth.presentation.restore.RestorePasswordViewModel
import org.openedx.auth.presentation.signin.SignInViewModel
import org.openedx.auth.presentation.signup.SignUpViewModel
import org.openedx.auth.presentation.sso.SocialAuthProvider
import org.openedx.core.DatabaseManager
import org.openedx.core.Validator
import org.openedx.core.config.Config
import org.openedx.core.data.storage.CalendarPreferences
import org.openedx.core.data.storage.CorePreferences
import org.openedx.core.data.storage.CourseDao
import org.openedx.core.data.storage.IosCalendarPreferences
import org.openedx.core.data.storage.IosCorePreferences
import org.openedx.core.domain.interactor.CalendarInteractor
import org.openedx.core.module.DownloadWorkerController
import org.openedx.core.module.db.CalendarDao
import org.openedx.core.module.db.DownloadDao
import org.openedx.core.module.download.DownloadHelper
import org.openedx.core.module.download.DownloadModelsSource
import org.openedx.core.presentation.CoreAnalytics
import org.openedx.core.presentation.DownloadsAnalytics
import org.openedx.core.presentation.dialog.downloaddialog.DownloadDialogManager
import org.openedx.core.presentation.global.IosWhatsNewGlobalManager
import org.openedx.core.presentation.global.WhatsNewGlobalManager
import org.openedx.core.system.CalendarManager
import org.openedx.core.system.connection.IosNetworkConnection
import org.openedx.core.system.notifier.CourseNotifier
import org.openedx.core.system.notifier.DiscoveryNotifier
import org.openedx.core.system.notifier.app.AppNotifier
import org.openedx.core.worker.CalendarSyncScheduler
import org.openedx.course.worker.OfflineProgressSyncScheduler
import org.openedx.courses.presentation.AllEnrolledCoursesViewModel
import org.openedx.courses.presentation.DashboardGalleryViewModel
import org.openedx.foundation.presentation.WindowSize
import org.openedx.dashboard.data.DashboardDao
import org.openedx.dashboard.data.repository.DashboardRepository
import org.openedx.dashboard.data.repository.DashboardRepositoryImpl
import org.openedx.dashboard.domain.interactor.DashboardInteractor
import org.openedx.dashboard.presentation.DashboardAnalytics
import org.openedx.dates.data.repository.DatesRepository
import org.openedx.dates.data.repository.DatesRepositoryImpl
import org.openedx.dates.data.storage.DatesDao
import org.openedx.dates.domain.interactor.DatesInteractor
import org.openedx.dates.presentation.DatesAnalytics
import org.openedx.dates.presentation.dates.DatesViewModel
import org.openedx.discovery.data.repository.DiscoveryRepository
import org.openedx.discovery.data.repository.DiscoveryRepositoryImpl
import org.openedx.discovery.data.storage.DiscoveryDao
import org.openedx.discovery.domain.interactor.DiscoveryInteractor
import org.openedx.discovery.presentation.DiscoveryAnalytics
import org.openedx.discovery.presentation.NativeDiscoveryViewModel
import org.openedx.downloads.data.repository.DownloadRepository
import org.openedx.downloads.data.repository.DownloadRepositoryImpl
import org.openedx.downloads.domain.interactor.DownloadInteractor
import org.openedx.downloads.presentation.download.DownloadsViewModel
import org.openedx.foundation.system.IosResourceManager
import org.openedx.foundation.system.ResourceManager
import org.openedx.profile.data.repository.ProfileRepository
import org.openedx.profile.data.repository.ProfileRepositoryImpl
import org.openedx.profile.data.storage.IosProfilePreferences
import org.openedx.profile.data.storage.ProfilePreferences
import org.openedx.profile.domain.interactor.ProfileInteractor
import org.openedx.profile.presentation.ProfileAnalytics
import org.openedx.profile.presentation.profile.ProfileViewModel
import org.openedx.profile.system.notifier.profile.ProfileNotifier
import org.openedx.shared.analytics.IosCoreAnalytics
import org.openedx.shared.analytics.IosDashboardAnalytics
import org.openedx.shared.analytics.IosDatesAnalytics
import org.openedx.shared.analytics.IosDiscoveryAnalytics
import org.openedx.shared.analytics.IosDownloadsAnalytics
import org.openedx.shared.analytics.IosProfileAnalytics
import org.openedx.app.room.AppDatabase
import org.openedx.app.room.DATABASE_NAME
import org.openedx.app.room.DatabaseManager as AppDatabaseManager
import org.openedx.shared.calendar.IosCalendarManager
import org.openedx.shared.download.IosFileDownloader
import org.openedx.shared.download.StubDownloadDialogManager
import org.openedx.shared.network.NetworkConnection
import org.openedx.shared.network.commonNetworkingModule
import org.openedx.shared.network.installTokenRefresh
import org.openedx.shared.sso.IosSocialAuthProvider
import org.openedx.shared.worker.IosCalendarSyncScheduler
import org.openedx.shared.worker.IosDownloadWorkerController
import org.openedx.shared.worker.IosOfflineProgressSyncScheduler

actual fun platformModule(): Module = module {
    includes(commonNetworkingModule)

    // ---- Platform infrastructure ----
    single { NetworkConnection() }
    single<org.openedx.core.system.connection.NetworkConnection> { IosNetworkConnection() }
    single { IosFileDownloader() }
    single<DownloadWorkerController> { IosDownloadWorkerController(get(), get(), get(), get()) }
    single<CalendarSyncScheduler> { IosCalendarSyncScheduler() }
    single<OfflineProgressSyncScheduler> { IosOfflineProgressSyncScheduler() }
    factory<SocialAuthProvider> { IosSocialAuthProvider() }
    single<CalendarManager> { IosCalendarManager() }

    // ---- Preferences ----
    single<CorePreferences> { IosCorePreferences() }
    single<CalendarPreferences> { IosCalendarPreferences() }
    single<ProfilePreferences> { IosProfilePreferences() }
    single<WhatsNewGlobalManager> { IosWhatsNewGlobalManager() }

    // ---- Resource manager ----
    single<ResourceManager> { IosResourceManager() }
    single { org.openedx.foundation.utils.FileUtil() }
    single { org.openedx.core.domain.helper.VideoPreviewHelper() }
    single<org.openedx.course.utils.ImageProcessor> { org.openedx.course.utils.IosImageProcessor() }

    // ---- Additional preferences ----
    single<org.openedx.whatsnew.data.storage.WhatsNewPreferences> { org.openedx.shared.stubs.IosWhatsNewPreferences() }
    single<org.openedx.course.data.storage.CoursePreferences> { org.openedx.shared.stubs.IosCoursePreferences() }
    single<org.openedx.core.data.storage.InAppReviewPreferences> { org.openedx.shared.stubs.IosInAppReviewPreferences() }
    single<org.openedx.core.presentation.dialog.appreview.AppReviewManager> {
        org.openedx.core.presentation.dialog.appreview.AppReviewManagerImpl(get(), get(), get(), get(), get())
    }
    single<org.openedx.core.presentation.dialog.appreview.AppReviewAnalytics> {
        org.openedx.shared.analytics.IosAppReviewAnalytics()
    }
    single<org.openedx.core.module.TranscriptProvider> { org.openedx.shared.stubs.IosTranscriptProvider() }

    // ---- Additional stubs ----
    single<org.openedx.core.system.AppCookieManager> {
        org.openedx.shared.stubs.IosAppCookieManager(
            config = get(),
            client = get(),
        )
    }
    single<org.openedx.core.system.PlatformActions> { org.openedx.shared.stubs.IosPlatformActions() }
    single<org.openedx.whatsnew.WhatsNewManager> { org.openedx.shared.stubs.IosWhatsNewManager() }
    single { org.openedx.core.presentation.global.AppData("OpenEdX", "org.openedx.app.ios", "1.0.0") }

    // ---- Analytics (all no-ops on iOS) ----
    factory<AuthAnalytics> { IosAuthAnalytics() }
    single<CoreAnalytics> { IosCoreAnalytics() }
    single<DashboardAnalytics> { IosDashboardAnalytics() }
    single<DiscoveryAnalytics> { IosDiscoveryAnalytics() }
    single<DownloadsAnalytics> { IosDownloadsAnalytics() }
    single<DatesAnalytics> { IosDatesAnalytics() }
    single<ProfileAnalytics> { IosProfileAnalytics() }
    single<org.openedx.app.AppAnalytics> { org.openedx.shared.stubs.IosAppAnalytics() }
    single<org.openedx.course.presentation.CourseAnalytics> { org.openedx.shared.analytics.IosCourseAnalytics() }
    single<org.openedx.discussion.presentation.DiscussionAnalytics> { org.openedx.shared.analytics.IosDiscussionAnalytics() }
    single<org.openedx.whatsnew.presentation.WhatsNewAnalytics> { org.openedx.shared.analytics.IosWhatsNewAnalytics() }

    // ---- Notifiers (concrete classes — same impl as Android) ----
    single { AppNotifier() }
    single { CourseNotifier() }
    single { DiscoveryNotifier() }
    single { ProfileNotifier() }
    single { org.openedx.core.system.notifier.VideoNotifier() }
    single { org.openedx.core.system.notifier.calendar.CalendarNotifier() }
    single { org.openedx.core.system.notifier.DownloadNotifier() }

    // ---- Room (KMP) ----
    single<AppDatabase> {
        val dbDir = platform.Foundation.NSSearchPathForDirectoriesInDomains(
            platform.Foundation.NSDocumentDirectory,
            platform.Foundation.NSUserDomainMask,
            true
        ).first() as String
        val dbPath = "$dbDir/$DATABASE_NAME"
        androidx.room.Room.databaseBuilder<AppDatabase>(name = dbPath)
            .setDriver(androidx.sqlite.driver.bundled.BundledSQLiteDriver())
            .setQueryCoroutineContext(kotlinx.coroutines.Dispatchers.Default)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }
    single<CourseDao> { get<AppDatabase>().courseDao() }
    single<DashboardDao> { get<AppDatabase>().dashboardDao() }
    single<DiscoveryDao> { get<AppDatabase>().discoveryDao() }
    single<DatesDao> { get<AppDatabase>().datesDao() }
    single<DownloadDao> { get<AppDatabase>().downloadDao() }
    single<CalendarDao> { get<AppDatabase>().calendarDao() }
    single { AppDatabaseManager(get(), get(), get(), get()) }
    single<DatabaseManager> { get<AppDatabaseManager>() }
    single<CalendarInteractor> { org.openedx.core.domain.interactor.CalendarInteractorImpl(get()) }

    // ---- Download helpers ----
    single<DownloadDialogManager> { StubDownloadDialogManager() }
    single<DownloadModelsSource> { org.openedx.core.module.download.DownloadModelsSourceImpl(get()) }
    single<DownloadHelper> { org.openedx.core.module.download.DownloadHelperImpl(get(), get()) }
    single<org.openedx.core.system.StorageManager> { org.openedx.core.system.StorageManagerImpl() }

    // ---- Repositories ----
    factory { Validator() }
    factory { AgreementProvider(get(), get()) }
    factory { AuthRepository(get(), get(), get()) }
    factory<DashboardRepository> { DashboardRepositoryImpl(get(), get(), get(), get()) }
    factory<DiscoveryRepository> { DiscoveryRepositoryImpl(get(), get(), get()) }
    factory<DatesRepository> { DatesRepositoryImpl(get(), get(), get()) }
    factory<DownloadRepository> { DownloadRepositoryImpl(get(), get(), get(), get()) }
    factory<ProfileRepository> { ProfileRepositoryImpl(get(), get(), get(), get(), get()) }

    // ---- Repositories (continued) ----
    factory<org.openedx.discussion.data.repository.DiscussionRepository> {
        org.openedx.discussion.data.repository.DiscussionRepositoryImpl(get(), get(), get())
    }
    factory { org.openedx.core.repository.CalendarRepository(get(), get(), get()) }

    // ---- Interactors ----
    factory { AuthInteractor(get()) }
    factory { DashboardInteractor(get()) }
    factory { DiscoveryInteractor(get()) }
    factory { DatesInteractor(get()) }
    factory { DownloadInteractor(get()) }
    factory { ProfileInteractor(get()) }
    factory { org.openedx.discussion.domain.interactor.DiscussionInteractor(get()) }
    single { org.openedx.course.domain.interactor.CourseInteractor(get()) }
    single<org.openedx.core.domain.interactor.CourseInteractor> { get<org.openedx.course.domain.interactor.CourseInteractor>() }
    single<org.openedx.course.data.repository.CourseRepository> { org.openedx.course.data.repository.CourseRepositoryImpl(get(), get(), get(), get(), get()) }

    // ---- ViewModels ----
    viewModel { (courseId: String) ->
        LogistrationViewModel(courseId, get(), get(), get(), get())
    }
    viewModel { (courseId: String?, infoType: String?, authCode: String) ->
        SignInViewModel(
            get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(),
            courseId, infoType, authCode,
        )
    }
    viewModel { (courseId: String?, infoType: String?) ->
        SignUpViewModel(get(), get(), get(), get(), get(), get(), get(), get(), courseId, infoType)
    }
    viewModel { RestorePasswordViewModel(get(), get(), get(), get()) }

    // Main-screen tabs
    viewModel {
        AllEnrolledCoursesViewModel(
            config = get(), networkConnection = get(), interactor = get(),
            resourceManager = get(), discoveryNotifier = get(), analytics = get(),
        )
    }
    viewModel { (windowSize: WindowSize) ->
        DashboardGalleryViewModel(
            config = get(), interactor = get(), resourceManager = get(),
            discoveryNotifier = get(), networkConnection = get(), fileUtil = get(),
            corePreferences = get(), windowSize = windowSize,
        )
    }
    viewModel { NativeDiscoveryViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel {
        DownloadsViewModel(
            networkConnection = get(), interactor = get(), downloadDialogManager = get(),
            resourceManager = get(), fileUtil = get(), config = get(), analytics = get(),
            discoveryNotifier = get(), courseNotifier = get(), preferencesManager = get(),
            coreAnalytics = get(), downloadModelsSource = get(), workerController = get(),
            downloadHelper = get(),
        )
    }
    viewModel {
        DatesViewModel(
            networkConnection = get(), resourceManager = get(), datesInteractor = get(),
            analytics = get(), calendarSyncScheduler = get(), corePreferences = get(),
        )
    }
    viewModel {
        ProfileViewModel(interactor = get(), resourceManager = get(), notifier = get(), analytics = get())
    }

    // ---- Profile/Settings screens ----
    viewModel { org.openedx.profile.presentation.settings.SettingsViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { org.openedx.profile.presentation.manageaccount.ManageAccountViewModel(get(), get(), get(), get()) }
    viewModel { org.openedx.profile.presentation.delete.DeleteProfileViewModel(get(), get(), get(), get(), get()) }
    viewModel { (account: org.openedx.profile.domain.model.Account) ->
        org.openedx.profile.presentation.edit.EditProfileViewModel(get(), get(), get(), get(), get(), account)
    }
    viewModel { (username: String) ->
        org.openedx.profile.presentation.anothersaccount.AnothersProfileViewModel(get(), get(), username)
    }
    viewModel { org.openedx.profile.presentation.video.VideoSettingsViewModel(get(), get(), get(), get()) }
    viewModel { (qualityType: String) ->
        org.openedx.core.presentation.settings.video.VideoQualityViewModel(qualityType, get(), get(), get(), get())
    }
    viewModel { org.openedx.profile.presentation.calendar.CalendarViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { org.openedx.profile.presentation.calendar.CoursesToSyncViewModel(get(), get(), get(), get()) }

    // ---- Discovery screens ----
    viewModel { (courseId: String) ->
        org.openedx.discovery.presentation.detail.CourseDetailsViewModel(courseId, get(), get(), get(), get(), get(), get(), get(), get())
    }
    viewModel { org.openedx.discovery.presentation.search.CourseSearchViewModel(get(), get(), get(), get(), get()) }
    viewModel { (pathId: String, infoType: String) ->
        org.openedx.discovery.presentation.info.CourseInfoViewModel(pathId, infoType, get(), get(), get(), get(), get(), get(), get(), get())
    }
    viewModel { org.openedx.discovery.presentation.program.ProgramViewModel(get(), get(), get(), get(), get(), get(), get()) }

    // ---- Course screens ----
    viewModel { (courseId: String, courseTitle: String, resumeBlockId: String) ->
        org.openedx.course.presentation.container.CourseContainerViewModel(
            courseId = courseId,
            courseName = courseTitle,
            resumeBlockId = resumeBlockId,
            config = get(), interactor = get(), resourceManager = get(),
            courseNotifier = get(), networkConnection = get(), corePreferences = get(),
            courseAnalytics = get(), imageProcessor = get(), calendarSyncScheduler = get(),
        )
    }
    viewModel { (courseId: String, courseTitle: String) ->
        org.openedx.course.presentation.home.CourseHomeViewModel(courseId, courseTitle, get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get())
    }
    viewModel { (courseId: String) ->
        org.openedx.course.presentation.section.CourseSectionViewModel(courseId, get(), get(), get(), get())
    }
    viewModel { (courseId: String, unitId: String, mode: org.openedx.course.presentation.unit.container.CourseViewMode) ->
        org.openedx.course.presentation.unit.container.CourseUnitContainerViewModel(courseId, unitId, mode, get(), get(), get(), get(), get(), get(), get())
    }
    viewModel { (courseId: String, handoutsType: String) ->
        org.openedx.course.presentation.handouts.HandoutsViewModel(courseId, handoutsType, get(), get(), get(), get())
    }
    viewModel { (descendants: List<String>) ->
        org.openedx.course.settings.download.DownloadQueueViewModel(descendants, get(), get(), get(), get(), get(), get(), get())
    }
    viewModel { (courseId: String, courseTitle: String) ->
        org.openedx.course.presentation.outline.CourseContentAllViewModel(
            courseId, courseTitle,
            get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(),
        )
    }
    viewModel { (courseId: String, courseTitle: String) ->
        org.openedx.course.presentation.contenttab.ContentTabViewModel(
            courseId = courseId, courseTitle = courseTitle,
            analytics = get(), resourceManager = get(),
        )
    }
    viewModel { (courseId: String) ->
        org.openedx.course.presentation.videos.CourseVideoViewModel(
            courseId = courseId, config = get(), interactor = get(), resourceManager = get(),
            networkConnection = get(), preferencesManager = get(), courseNotifier = get(),
            downloadDialogManager = get(), fileUtil = get(), analytics = get(),
            videoPreviewHelper = get(), coreAnalytics = get(), downloadModelsSource = get(),
            workerController = get(), downloadHelper = get(),
        )
    }
    viewModel { (courseId: String) ->
        org.openedx.course.presentation.assignments.CourseAssignmentViewModel(
            courseId = courseId, interactor = get(), courseNotifier = get(), analytics = get(),
        )
    }
    viewModel { (courseId: String) ->
        org.openedx.course.presentation.progress.CourseProgressViewModel(courseId, get(), get(), get())
    }
    viewModel { (courseId: String, courseTitle: String) ->
        org.openedx.course.presentation.offline.CourseOfflineViewModel(
            courseId, courseTitle,
            get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(),
        )
    }
    viewModel { (courseId: String, enrollmentMode: String) ->
        org.openedx.course.presentation.dates.CourseDatesViewModel(
            courseId = courseId, enrollmentMode = enrollmentMode,
            courseNotifier = get(), interactor = get(), courseAnalytics = get(),
            config = get(), calendarInteractor = get(), calendarNotifier = get(),
            corePreferences = get(), resourceManager = get(),
        )
    }
    viewModel { (courseId: String, courseTitle: String) ->
        org.openedx.discussion.presentation.topics.DiscussionTopicsViewModel(
            courseId = courseId, courseTitle = courseTitle,
            interactor = get(), resourceManager = get(), analytics = get(), courseNotifier = get(),
        )
    }

    // ---- Discussion screens ----
    viewModel { (courseId: String, topicId: String, threadType: String) ->
        org.openedx.discussion.presentation.threads.DiscussionThreadsViewModel(get(), get(), get(), courseId, topicId, threadType)
    }
    viewModel { (thread: org.openedx.discussion.domain.model.Thread) ->
        org.openedx.discussion.presentation.comments.DiscussionCommentsViewModel(get(), get(), get(), thread)
    }
    viewModel { (comment: org.openedx.discussion.domain.model.DiscussionComment) ->
        org.openedx.discussion.presentation.responses.DiscussionResponsesViewModel(get(), get(), get(), comment)
    }
    viewModel { (courseId: String) ->
        org.openedx.discussion.presentation.threads.DiscussionAddThreadViewModel(get(), get(), get(), courseId)
    }
    viewModel { (courseId: String) ->
        org.openedx.discussion.presentation.search.DiscussionSearchThreadViewModel(get(), get(), get(), courseId)
    }

    // ---- WhatsNew ----
    viewModel { (courseId: String?, infoType: String?) ->
        org.openedx.whatsnew.presentation.whatsnew.WhatsNewViewModel(courseId, infoType, get(), get(), get(), get(), get())
    }

    // ---- Networking ----
    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
            coerceInputValues = true
            explicitNulls = false
        }
    }

    single {
        val config = get<Config>()
        val prefs = get<CorePreferences>()
        val notifier = get<AppNotifier>()
        HttpClient(Darwin) {
            install(ContentNegotiation) {
                json(get())
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 60_000
                connectTimeoutMillis = 60_000
                socketTimeoutMillis = 60_000
            }

            defaultRequest {
                url(config.getApiHostURL())
                headers.append("Accept", "application/json")
                val token = prefs.accessToken
                if (token.isNotEmpty()) {
                    headers.append("Authorization", "${config.getAccessTokenType()} $token")
                }
            }

            install(Logging) {
                level = LogLevel.HEADERS
            }
        }.also { it.installTokenRefresh(config, prefs, notifier) }
    }
}
