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
import org.koin.dsl.module
import org.openedx.auth.presentation.sso.SocialAuthProvider
import org.openedx.core.config.Config
import org.openedx.core.data.storage.CorePreferences
import org.openedx.core.module.DownloadWorkerController
import org.openedx.core.system.CalendarManager
import org.openedx.core.worker.CalendarSyncScheduler
import org.openedx.course.worker.OfflineProgressSyncScheduler
import org.openedx.shared.calendar.IosCalendarManager
import org.openedx.shared.network.NetworkConnection
import org.openedx.shared.network.commonNetworkingModule
import org.openedx.shared.sso.IosSocialAuthProvider
import org.openedx.shared.storage.SecureStorage
import org.openedx.shared.worker.IosCalendarSyncScheduler
import org.openedx.shared.worker.IosDownloadWorkerController
import org.openedx.shared.worker.IosOfflineProgressSyncScheduler

actual fun platformModule(): Module = module {
    includes(commonNetworkingModule)

    single { NetworkConnection() }
    single { SecureStorage() }
    single<DownloadWorkerController> { IosDownloadWorkerController() }
    single<CalendarSyncScheduler> { IosCalendarSyncScheduler() }
    single<OfflineProgressSyncScheduler> { IosOfflineProgressSyncScheduler() }
    factory<SocialAuthProvider> { IosSocialAuthProvider() }
    single<CalendarManager> { IosCalendarManager() }

    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
            coerceInputValues = true
        }
    }

    single {
        val config = get<Config>()
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
                val prefs = get<CorePreferences>()
                val token = prefs.accessToken
                if (token.isNotEmpty()) {
                    headers.append("Authorization", "${config.getAccessTokenType()} $token")
                }
            }

            install(Logging) {
                level = LogLevel.HEADERS
            }
        }
    }
}
