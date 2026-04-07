package org.openedx.app.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import org.openedx.app.data.api.NotificationsApi
import org.openedx.app.data.networking.AppUpgradeInterceptor
import org.openedx.app.data.networking.HandleErrorInterceptor
import org.openedx.app.data.networking.HeadersInterceptor
import org.openedx.app.data.networking.OauthRefreshTokenAuthenticator
import org.openedx.auth.data.api.AuthApi
import org.openedx.core.BuildConfig
import org.openedx.core.config.Config
import org.openedx.core.data.api.CookiesApi
import org.openedx.core.data.api.CourseApi
import org.openedx.discovery.data.api.DiscoveryApi
import org.openedx.discussion.data.api.DiscussionApi
import org.openedx.profile.data.api.ProfileApi

val networkingModule = module {

    single { OauthRefreshTokenAuthenticator(get(), get(), get()) }

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
        HttpClient(OkHttp) {
            engine {
                config {
                    retryOnConnectionFailure(true)
                }
                addInterceptor(HeadersInterceptor(get(), get(), get()))
                if (BuildConfig.DEBUG) {
                    addInterceptor(okhttp3.logging.HttpLoggingInterceptor().setLevel(
                        okhttp3.logging.HttpLoggingInterceptor.Level.BODY
                    ))
                }
                addInterceptor(HandleErrorInterceptor(get()))
                addInterceptor(AppUpgradeInterceptor(get()))
                addInterceptor(get<OauthRefreshTokenAuthenticator>())
            }

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
            }

            if (BuildConfig.DEBUG) {
                install(Logging) {
                    logger = object : Logger {
                        override fun log(message: String) {
                            android.util.Log.d("KtorClient", message)
                        }
                    }
                    level = LogLevel.HEADERS
                }
            }
        }
    }

    single { AuthApi(get()) }
    single { CookiesApi(get()) }
    single { CourseApi(get()) }
    single { ProfileApi(get()) }
    single { DiscussionApi(get()) }
    single { DiscoveryApi(get()) }
    single { NotificationsApi(get()) }
}
