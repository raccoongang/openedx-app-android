package org.openedx.auth.presentation.logistration

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.openedx.auth.presentation.AuthAnalytics
import org.openedx.auth.presentation.AuthAnalyticsEvent
import org.openedx.auth.presentation.AuthAnalyticsKey
import org.openedx.auth.presentation.sso.SocialAuthProvider
import org.openedx.core.config.Config
import org.openedx.core.utils.Logger
import org.openedx.foundation.presentation.BaseViewModel
import org.openedx.foundation.system.ResourceManager

class LogistrationViewModel(
    private val courseId: String,
    private val config: Config,
    private val analytics: AuthAnalytics,
    private val socialAuthProvider: SocialAuthProvider,
    private val resourceManager: ResourceManager,
) : BaseViewModel() {

    private val logger = Logger("LogistrationViewModel")

    val isDiscoveryTypeWebView get() = config.getDiscoveryConfig().isViewTypeWebView()
    val isRegistrationEnabled get() = config.isRegistrationEnabled()
    val isBrowserRegistrationEnabled get() = config.isBrowserRegistrationEnabled()
    val isBrowserLoginEnabled get() = config.isBrowserLoginEnabled()
    val apiHostUrl get() = config.getApiHostURL()

    init {
        logLogistrationScreenEvent()
    }

    fun signInBrowser(activityContext: Any) {
        viewModelScope.launch {
            runCatching {
                socialAuthProvider.signInWithBrowser(activityContext)
            }.onFailure {
                logger.e { "Browser auth error: $it" }
            }
        }
    }

    fun logSignInClicked() {
        logEvent(AuthAnalyticsEvent.SIGN_IN_CLICKED)
    }

    fun logRegisterClicked() {
        logEvent(AuthAnalyticsEvent.REGISTER_CLICKED)
    }

    fun logDiscoverySearch(query: String) {
        if (query.isNotEmpty()) {
            logEvent(
                event = AuthAnalyticsEvent.DISCOVERY_COURSES_SEARCH,
                params = buildMap {
                    put(AuthAnalyticsKey.SEARCH_QUERY.key, query)
                },
            )
        } else {
            logEvent(AuthAnalyticsEvent.EXPLORE_ALL_COURSES)
        }
    }

    private fun logEvent(
        event: AuthAnalyticsEvent,
        params: Map<String, Any?> = emptyMap(),
    ) {
        analytics.logEvent(
            event = event.eventName,
            params = buildMap {
                put(AuthAnalyticsKey.NAME.key, event.biValue)
                putAll(params)
            },
        )
    }

    private fun logLogistrationScreenEvent() {
        val event = AuthAnalyticsEvent.Logistration
        analytics.logScreenEvent(
            screenName = event.eventName,
            params = buildMap {
                put(AuthAnalyticsKey.NAME.key, event.biValue)
            }
        )
    }
}
