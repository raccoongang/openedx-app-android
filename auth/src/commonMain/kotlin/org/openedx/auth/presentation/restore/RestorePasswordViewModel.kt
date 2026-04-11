package org.openedx.auth.presentation.restore

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.openedx.auth.Res
import org.openedx.auth.auth_invalid_email
import org.openedx.auth.domain.interactor.AuthInteractor
import org.openedx.auth.presentation.AuthAnalytics
import org.openedx.auth.presentation.AuthAnalyticsEvent
import org.openedx.auth.presentation.AuthAnalyticsKey
import org.openedx.core.system.EdxError
import org.openedx.core.system.notifier.app.AppNotifier
import org.openedx.core.system.notifier.app.AppUpgradeEvent
import org.openedx.foundation.extension.isEmailValid
import org.openedx.foundation.presentation.BaseViewModel
import org.openedx.foundation.presentation.UIMessage
import org.openedx.foundation.system.ResourceManager
import org.openedx.foundation.Res as foundationRes
import org.openedx.foundation.foundation_error_no_connection
import org.openedx.foundation.foundation_error_unknown_error

class RestorePasswordViewModel(
    private val interactor: AuthInteractor,
    private val resourceManager: ResourceManager,
    private val analytics: AuthAnalytics,
    private val appNotifier: AppNotifier
) : BaseViewModel(
    noConnectionMessage = resourceManager.getString(foundationRes.string.foundation_error_no_connection),
    defaultErrorMessage = resourceManager.getString(foundationRes.string.foundation_error_unknown_error),
) {

    private val _uiState = MutableStateFlow<RestorePasswordUIState?>(null)
    val uiState: StateFlow<RestorePasswordUIState?> = _uiState.asStateFlow()

    private val _appUpgradeEvent = MutableStateFlow<AppUpgradeEvent?>(null)
    val appUpgradeEventUIState: StateFlow<AppUpgradeEvent?> = _appUpgradeEvent.asStateFlow()

    init {
        collectAppUpgradeEvent()
    }

    fun passwordReset(email: String) {
        logEvent(AuthAnalyticsEvent.RESET_PASSWORD_CLICKED)
        _uiState.value = RestorePasswordUIState.Loading
        viewModelScope.launch {
            try {
                if (email.isNotEmpty() && email.isEmailValid()) {
                    if (interactor.passwordReset(email)) {
                        _uiState.value = RestorePasswordUIState.Success(email)
                        logResetPasswordEvent(true)
                    } else {
                        _uiState.value = RestorePasswordUIState.Initial
                        handleErrorUiMessage(
                            throwable = null,
                        )
                        logResetPasswordEvent(false)
                    }
                } else {
                    _uiState.value = RestorePasswordUIState.Initial
                    handleErrorUiMessage(
                        throwable = null,
                        defaultErrorMessage = resourceManager.getString(Res.string.auth_invalid_email),
                    )
                    logResetPasswordEvent(false)
                }
            } catch (e: Exception) {
                _uiState.value = RestorePasswordUIState.Initial
                logResetPasswordEvent(false)
                when (e) {
                    is EdxError.ValidationException -> sendMessage(
                        UIMessage.SnackBarMessage(e.error)
                    )

                    else -> handleErrorUiMessage(
                        throwable = e,
                    )
                }
            }
        }
    }

    private fun collectAppUpgradeEvent() {
        viewModelScope.launch {
            appNotifier.notifier.collect { event ->
                if (event is AppUpgradeEvent) {
                    _appUpgradeEvent.value = event
                }
            }
        }
    }

    private fun logResetPasswordEvent(success: Boolean) {
        logEvent(
            event = AuthAnalyticsEvent.RESET_PASSWORD_SUCCESS,
            params = buildMap {
                put(AuthAnalyticsKey.SUCCESS.key, success)
            }
        )
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
            }
        )
    }
}
