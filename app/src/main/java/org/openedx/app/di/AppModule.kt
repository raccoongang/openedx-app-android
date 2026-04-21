package org.openedx.app.di

import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import androidx.room.RoomDatabase
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidApplication
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.openedx.app.AnalyticsManager
import org.openedx.app.AppAnalytics
import org.openedx.app.BuildConfig
import org.openedx.app.PluginManager
import org.openedx.app.data.storage.PreferencesManager
import org.openedx.app.deeplink.DeepLinkRouter
import org.openedx.app.room.AppDatabase
import org.openedx.app.room.DATABASE_NAME
import org.openedx.app.room.DatabaseManager as AppDatabaseManager
import org.openedx.app.PlatformActionsImpl
import org.openedx.auth.presentation.AgreementProvider
import org.openedx.auth.presentation.AuthAnalytics
import org.openedx.auth.presentation.sso.BrowserAuthHelper
import org.openedx.auth.presentation.sso.FacebookAuthHelper
import org.openedx.auth.presentation.sso.GoogleAuthHelper
import org.openedx.auth.presentation.sso.MicrosoftAuthHelper
import org.openedx.auth.presentation.sso.OAuthHelper
import org.openedx.auth.presentation.sso.SocialAuthProvider
import org.openedx.auth.presentation.sso.SocialAuthProviderImpl
import org.openedx.core.R
import org.openedx.core.config.Config
import org.openedx.core.config.initCoreConfigLoader
import org.openedx.core.data.storage.CalendarPreferences
import org.openedx.core.data.storage.CorePreferences
import org.openedx.core.data.storage.InAppReviewPreferences
import org.openedx.core.domain.helper.VideoPreviewHelper
import org.openedx.core.module.DownloadWorkerController
import org.openedx.core.module.DownloadWorkerControllerImpl
import org.openedx.core.module.TranscriptManager
import org.openedx.core.module.TranscriptProvider
import org.openedx.core.module.download.DownloadHelper
import org.openedx.core.module.download.DownloadHelperImpl
import org.openedx.core.module.download.DownloadModelsSource
import org.openedx.core.module.download.DownloadModelsSourceImpl
import org.openedx.core.module.download.FileDownloader
import org.openedx.core.presentation.CoreAnalytics
import org.openedx.core.presentation.DownloadsAnalytics
import org.openedx.core.presentation.dialog.appreview.AppReviewAnalytics
import org.openedx.core.presentation.dialog.appreview.AppReviewManager
import org.openedx.core.presentation.dialog.appreview.AppReviewManagerImpl
import org.openedx.core.presentation.dialog.downloaddialog.DownloadDialogManager
import org.openedx.core.presentation.dialog.downloaddialog.DownloadDialogManagerImpl
import org.openedx.core.presentation.global.AppData
import org.openedx.core.presentation.global.WhatsNewGlobalManager
import org.openedx.core.system.AppCookieManager
import org.openedx.core.system.AppCookieManagerImpl
import org.openedx.core.system.CalendarManager
import org.openedx.core.system.CalendarManagerImpl
import org.openedx.core.system.StorageManager
import org.openedx.core.system.StorageManagerImpl
import org.openedx.core.system.PlatformActions
import org.openedx.core.system.connection.NetworkConnection
import org.openedx.core.system.connection.NetworkConnectionImpl
import org.openedx.core.system.notifier.CourseNotifier
import org.openedx.core.system.notifier.DiscoveryNotifier
import org.openedx.core.system.notifier.DownloadNotifier
import org.openedx.core.system.notifier.VideoNotifier
import org.openedx.core.system.notifier.app.AppNotifier
import org.openedx.core.system.notifier.calendar.CalendarNotifier
import org.openedx.core.worker.CalendarSyncScheduler
import org.openedx.core.worker.CalendarSyncSchedulerImpl
import org.openedx.course.data.storage.CoursePreferences
import org.openedx.course.presentation.CourseAnalytics
import org.openedx.course.presentation.unit.html.JsInjectionProvider
import org.openedx.course.presentation.unit.html.JsInjectionProviderImpl
import org.openedx.course.utils.ImageProcessor
import org.openedx.course.utils.ImageProcessorImpl
import org.openedx.course.worker.OfflineProgressSyncScheduler
import org.openedx.course.worker.OfflineProgressSyncSchedulerImpl
import org.openedx.dashboard.presentation.DashboardAnalytics
import org.openedx.dates.presentation.DatesAnalytics
import org.openedx.discovery.presentation.DiscoveryAnalytics
import org.openedx.discussion.presentation.DiscussionAnalytics
import org.openedx.discussion.system.notifier.DiscussionNotifier
import org.openedx.foundation.system.AndroidResourceManager
import org.openedx.foundation.system.ResourceManager
import org.openedx.foundation.utils.FileUtil
import org.openedx.profile.data.storage.ProfilePreferences
import org.openedx.profile.presentation.ProfileAnalytics
import org.openedx.profile.system.notifier.profile.ProfileNotifier
import org.openedx.whatsnew.WhatsNewManager
import org.openedx.whatsnew.WhatsNewManagerImpl
import org.openedx.whatsnew.data.storage.WhatsNewPreferences
import org.openedx.whatsnew.presentation.WhatsNewAnalytics
import org.openedx.core.DatabaseManager as IDatabaseManager

val appModule = module {

    single { initCoreConfigLoader(get()); Config() }
    single { PreferencesManager(get(), get()) }
    single<CorePreferences> { get<PreferencesManager>() }
    single<ProfilePreferences> { get<PreferencesManager>() }
    single<WhatsNewPreferences> { get<PreferencesManager>() }
    single<InAppReviewPreferences> { get<PreferencesManager>() }
    single<CoursePreferences> { get<PreferencesManager>() }
    single<CalendarPreferences> { get<PreferencesManager>() }

    single<ResourceManager> { AndroidResourceManager(get()) }
    single<AppCookieManager> { AppCookieManagerImpl(get(), get()) }
    single { ReviewManagerFactory.create(get()) }
    single<CalendarManager> { CalendarManagerImpl(get(), get()) }
    single<StorageManager> { StorageManagerImpl() }
    single<DownloadDialogManager> { DownloadDialogManagerImpl(get(), get(), get(), get(), get()) }
    single { AppDatabaseManager(get(), get(), get(), get()) }
    single<IDatabaseManager> { get<AppDatabaseManager>() }

    single<ImageProcessor> { ImageProcessorImpl(get()) }

    single { AppNotifier() }
    single { CourseNotifier() }
    single { DiscussionNotifier() }
    single { ProfileNotifier() }
    single { DownloadNotifier() }
    single { VideoNotifier() }
    single { DiscoveryNotifier() }
    single { CalendarNotifier() }

    single { org.openedx.core.presentation.global.AppNavigator() }
    single { DeepLinkRouter(get(), get(), get(), get(), get()) }

    single<NetworkConnection> { NetworkConnectionImpl(get()) }

    single(named("IODispatcher")) {
        Dispatchers.IO
    }

    single {
        Room.databaseBuilder(
            androidApplication(),
            AppDatabase::class.java,
            DATABASE_NAME
        ).fallbackToDestructiveMigration()
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()
    }

    single<RoomDatabase> {
        get<AppDatabase>()
    }

    single {
        val room = get<AppDatabase>()
        room.discoveryDao()
    }

    single {
        val room = get<AppDatabase>()
        room.courseDao()
    }

    single {
        val room = get<AppDatabase>()
        room.dashboardDao()
    }

    single {
        val room = get<AppDatabase>()
        room.downloadDao()
    }

    single<DownloadModelsSource> { DownloadModelsSourceImpl(get()) }

    single {
        val room = get<AppDatabase>()
        room.calendarDao()
    }

    single {
        val room = get<AppDatabase>()
        room.datesDao()
    }

    single {
        FileDownloader()
    }

    single<DownloadWorkerController> {
        DownloadWorkerControllerImpl(get(), get(), get())
    }

    single {
        val resourceManager = get<ResourceManager>()
        AppData(
            appName = resourceManager.getString(R.string.app_name),
            versionName = BuildConfig.VERSION_NAME,
            applicationId = BuildConfig.APPLICATION_ID,
        )
    }
    single<AppReviewManager> { AppReviewManagerImpl(get(), get(), get(), get(), get()) }

    single<TranscriptProvider> { TranscriptManager(get(), get()) }
    single { WhatsNewManagerImpl(get(), get(), get()) }
    single<WhatsNewManager> { get<WhatsNewManagerImpl>() }
    single<WhatsNewGlobalManager> { get<WhatsNewManagerImpl>() }

    single<AppAnalytics> { get<AnalyticsManager>() }
    single<AuthAnalytics> { get<AnalyticsManager>() }
    single<AppReviewAnalytics> { get<AnalyticsManager>() }
    single<CoreAnalytics> { get<AnalyticsManager>() }
    single<CourseAnalytics> { get<AnalyticsManager>() }
    single<DashboardAnalytics> { get<AnalyticsManager>() }
    single<DiscoveryAnalytics> { get<AnalyticsManager>() }
    single<DiscussionAnalytics> { get<AnalyticsManager>() }
    single<ProfileAnalytics> { get<AnalyticsManager>() }
    single<WhatsNewAnalytics> { get<AnalyticsManager>() }
    single<DatesAnalytics> { get<AnalyticsManager>() }
    single<DownloadsAnalytics> { get<AnalyticsManager>() }

    factory { AgreementProvider(get(), get()) }
    factory { FacebookAuthHelper() }
    factory { GoogleAuthHelper(get()) }
    factory { MicrosoftAuthHelper() }
    factory { BrowserAuthHelper(get()) }
    factory { OAuthHelper(get(), get(), get()) }
    factory<SocialAuthProvider> { SocialAuthProviderImpl(get(), get()) }
    factory { VideoPreviewHelper(get(), get()) }

    factory { FileUtil(get(), get<ResourceManager>().getString(R.string.app_name)) }
    single<DownloadHelper> { DownloadHelperImpl(get(), get()) }

    factory<OfflineProgressSyncScheduler> { OfflineProgressSyncSchedulerImpl(get()) }
    factory<JsInjectionProvider> { JsInjectionProviderImpl() }
    single<PlatformActions> { PlatformActionsImpl(get()) }

    single<CalendarSyncScheduler> { CalendarSyncSchedulerImpl(get()) }

    single { AnalyticsManager() }
    single {
        PluginManager(
            analyticsManager = get()
        )
    }
}
