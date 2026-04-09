package org.openedx.auth.presentation.logistration

import android.app.Activity
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.openedx.auth.presentation.AuthAnalytics
import org.openedx.auth.presentation.AuthAnalyticsEvent
import org.openedx.auth.presentation.AuthAnalyticsKey
import org.openedx.auth.presentation.sso.BrowserAuthHelper
import org.openedx.core.config.Config
import org.openedx.core.utils.Logger
import org.openedx.foundation.presentation.BaseViewModel
import org.openedx.foundation.system.ResourceManager

class LogistrationViewModel(
    private val courseId: String,
    private val config: Config,
    private val analytics: AuthAnalytics,
    private val browserAuthHelper: BrowserAuthHelper,
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

    fun signInBrowser(activityContext: Activity) {
        viewModelScope.launch {
            runCatching {
                browserAuthHelper.signIn(activityContext)
            }.onFailure {
                logger.e { "Browser auth error: $it" }
            }
        }
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
