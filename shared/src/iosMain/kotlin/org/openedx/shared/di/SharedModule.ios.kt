package org.openedx.shared.di

import org.koin.core.module.Module
import org.koin.dsl.module
import org.openedx.auth.presentation.sso.SocialAuthProvider
import org.openedx.core.module.DownloadWorkerController
import org.openedx.core.system.CalendarManager
import org.openedx.core.worker.CalendarSyncScheduler
import org.openedx.course.worker.OfflineProgressSyncScheduler
import org.openedx.shared.calendar.IosCalendarManager
import org.openedx.shared.network.NetworkConnection
import org.openedx.shared.sso.IosSocialAuthProvider
import org.openedx.shared.storage.SecureStorage
import org.openedx.shared.worker.IosCalendarSyncScheduler
import org.openedx.shared.worker.IosDownloadWorkerController
import org.openedx.shared.worker.IosOfflineProgressSyncScheduler

actual fun platformModule(): Module = module {
    single { NetworkConnection() }
    single { SecureStorage() }
    single<DownloadWorkerController> { IosDownloadWorkerController() }
    single<CalendarSyncScheduler> { IosCalendarSyncScheduler() }
    single<OfflineProgressSyncScheduler> { IosOfflineProgressSyncScheduler() }
    factory<SocialAuthProvider> { IosSocialAuthProvider() }
    single<CalendarManager> { IosCalendarManager() }
}
