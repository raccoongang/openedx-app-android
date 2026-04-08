package org.openedx.profile.presentation.settings

import android.content.Context
import androidx.compose.ui.text.intl.Locale
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.openedx.core.AppUpdateState
import org.openedx.core.config.Config
import org.openedx.core.module.DownloadWorkerController
import org.openedx.core.presentation.global.AppData
import org.openedx.core.system.AppCookieManager
import org.openedx.core.system.notifier.app.AppNotifier
import org.openedx.core.system.notifier.app.LogoutEvent
import org.openedx.core.utils.EmailUtil
import org.openedx.foundation.presentation.BaseViewModel
import org.openedx.foundation.system.ResourceManager
import org.openedx.profile.domain.interactor.ProfileInteractor
import org.openedx.profile.domain.model.Configuration
import org.openedx.profile.presentation.ProfileAnalytics
import org.openedx.profile.presentation.ProfileAnalyticsEvent
import org.openedx.profile.presentation.ProfileAnalyticsKey
import org.openedx.profile.system.notifier.account.AccountDeactivated
import org.openedx.profile.system.notifier.profile.ProfileNotifier

class SettingsViewModel(
    private val appData: AppData,
    private val config: Config,
    private val resourceManager: ResourceManager,
    private val interactor: ProfileInteractor,
    private val cookieManager: AppCookieManager,
    private val workerController: DownloadWorkerController,
    private val analytics: ProfileAnalytics,
    private val appNotifier: AppNotifier,
    private val profileNotifier: ProfileNotifier,
) : BaseViewModel(resourceManager) {

    private val _uiState: MutableStateFlow<SettingsUIState> = MutableStateFlow(SettingsUIState.Data(configuration))
    val uiState: StateFlow<SettingsUIState> = _uiState.asStateFlow()

    private val _successLogout = MutableSharedFlow<Boolean>()
    val successLogout: SharedFlow<Boolean>
        get() = _successLogout.asSharedFlow()

    val isLogistrationEnabled get() = config.isPreLoginExperienceEnabled()

    private val configuration
        get() = Configuration(
            agreementUrls = config.getAgreement(Locale.current.language),
            faqUrl = config.getFaqUrl(),
            supportEmail = config.getFeedbackEmailAddress(),
            versionName = appData.versionName,
        )

    init {
        collectProfileEvent()
    }

    fun logout() {
        logProfileEvent(ProfileAnalyticsEvent.LOGOUT_CLICKED)
        viewModelScope.launch {
            try {
                workerController.removeModels()
                withContext(Dispatchers.IO) {
                    interactor.logout()
                }
                logProfileEvent(
                    event = ProfileAnalyticsEvent.LOGGED_OUT,
                    params = buildMap {
                        put(ProfileAnalyticsKey.FORCE.key, ProfileAnalyticsKey.FALSE.key)
                    }
                )
            } catch (e: Exception) {
                handleErrorUiMessage(
                    throwable = e,
                )
            } finally {
                cookieManager.clearWebViewCookie()
                appNotifier.send(LogoutEvent(false))
                _successLogout.emit(true)
            }
        }
    }

    private fun collectProfileEvent() {
        viewModelScope.launch {
            profileNotifier.notifier.collect {
                if (it is AccountDeactivated) {
                    logout()
                }
            }
        }
    }

    fun videoSettingsClicked() {
        logProfileEvent(ProfileAnalyticsEvent.VIDEO_SETTING_CLICKED)
    }

    fun privacyPolicyClicked() {
        logProfileEvent(ProfileAnalyticsEvent.PRIVACY_POLICY_CLICKED)
    }

    fun cookiePolicyClicked() {
        logProfileEvent(ProfileAnalyticsEvent.COOKIE_POLICY_CLICKED)
    }

    fun dataSellClicked() {
        logProfileEvent(ProfileAnalyticsEvent.DATA_SELL_CLICKED)
    }

    fun faqClicked() {
        logProfileEvent(ProfileAnalyticsEvent.FAQ_CLICKED)
    }

    fun termsOfUseClicked() {
        logProfileEvent(ProfileAnalyticsEvent.TERMS_OF_USE_CLICKED)
    }

    fun emailSupportClicked(context: Context) {
        EmailUtil.showFeedbackScreen(
            context = context,
            feedbackEmailAddress = config.getFeedbackEmailAddress(),
            appVersion = appData.versionName
        )
        logProfileEvent(ProfileAnalyticsEvent.CONTACT_SUPPORT_CLICKED)
    }

    fun appVersionClickedEvent(context: Context) {
        AppUpdateState.openPlayMarket(context)
    }

    private val _restartAppEvent = MutableSharedFlow<Boolean>()
    val restartAppEvent: SharedFlow<Boolean>
        get() = _restartAppEvent.asSharedFlow()

    fun restartApp() {
        viewModelScope.launch {
            _restartAppEvent.emit(isLogistrationEnabled)
        }
    }

    private fun logProfileEvent(
        event: ProfileAnalyticsEvent,
        params: Map<String, Any?> = emptyMap(),
    ) {
        analytics.logEvent(
            event = event.eventName,
            params = buildMap {
                put(ProfileAnalyticsKey.NAME.key, event.biValue)
                put(ProfileAnalyticsKey.CATEGORY.key, ProfileAnalyticsKey.PROFILE.key)
                putAll(params)
            }
        )
    }
}
