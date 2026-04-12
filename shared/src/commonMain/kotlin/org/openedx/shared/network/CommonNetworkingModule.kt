package org.openedx.shared.network

import io.ktor.client.HttpClient
import org.koin.dsl.module
import org.openedx.auth.data.api.AuthApi
import org.openedx.core.data.api.CookiesApi
import org.openedx.core.data.api.CourseApi
import org.openedx.discovery.data.api.DiscoveryApi
import org.openedx.discussion.data.api.DiscussionApi
import org.openedx.profile.data.api.ProfileApi

/**
 * Common API service registrations.
 * HttpClient must be provided by platform-specific module.
 */
val commonNetworkingModule = module {
    single { AuthApi(get<HttpClient>()) }
    single { CookiesApi(get<HttpClient>()) }
    single { CourseApi(get<HttpClient>()) }
    single { ProfileApi(get<HttpClient>()) }
    single { DiscussionApi(get<HttpClient>()) }
    single { DiscoveryApi(get<HttpClient>()) }
}
