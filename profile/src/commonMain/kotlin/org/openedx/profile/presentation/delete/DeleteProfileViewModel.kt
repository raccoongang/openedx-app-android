package org.openedx.profile.presentation.delete

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.openedx.core.Res as coreRes
import org.openedx.core.core_user_not_active
import org.openedx.core.Validator
import org.openedx.profile.Res as profileRes
import org.openedx.profile.profile_invalid_password
import org.openedx.profile.profile_password_is_incorrect
import org.openedx.core.system.EdxError
import org.openedx.foundation.extension.isInternetError
import org.openedx.foundation.presentation.BaseViewModel
import org.openedx.foundation.presentation.UIMessage
import org.openedx.foundation.system.ResourceManager
import org.openedx.profile.domain.interactor.ProfileInteractor
import org.openedx.profile.presentation.ProfileAnalytics
import org.openedx.profile.presentation.ProfileAnalyticsEvent
import org.openedx.profile.presentation.ProfileAnalyticsKey
import org.openedx.profile.system.notifier.account.AccountDeactivated
import org.openedx.profile.system.notifier.profile.ProfileNotifier
import org.openedx.foundation.Res as foundationRes
import org.openedx.foundation.foundation_error_no_connection
import org.openedx.foundation.foundation_error_unknown_error

class DeleteProfileViewModel(
    private val resourceManager: ResourceManager,
    private val interactor: ProfileInteractor,
    private val notifier: ProfileNotifier,
    private val validator: Validator,
    private val analytics: ProfileAnalytics,
) : BaseViewModel(
    noConnectionMessage = resourceManager.getString(foundationRes.string.foundation_error_no_connection),
    defaultErrorMessage = resourceManager.getString(foundationRes.string.foundation_error_unknown_error),
) {

    private val _uiState = MutableStateFlow<DeleteProfileFragmentUIState?>(null)
    val uiState: StateFlow<DeleteProfileFragmentUIState?> = _uiState.asStateFlow()

    fun deleteProfile(password: String) {
        logDeleteProfileClickedEvent()
        if (!validator.isPasswordValid(password)) {
            _uiState.value =
                DeleteProfileFragmentUIState.Error(
                    resourceManager.getString(profileRes.string.profile_invalid_password)
                )
            return
        }
        viewModelScope.launch {
            _uiState.value = DeleteProfileFragmentUIState.Loading
            try {
                interactor.deactivateAccount(password)
                _uiState.value = DeleteProfileFragmentUIState.Success
                logDeleteProfileEvent(true)
                notifier.send(AccountDeactivated())
            } catch (e: Exception) {
                if (e.isInternetError()) {
                    handleErrorUiMessage(
                        throwable = e,
                    )
                    _uiState.value = DeleteProfileFragmentUIState.Initial
                } else if (e is EdxError.UserNotActiveException) {
                    sendMessage(
                        UIMessage.SnackBarMessage(
                            resourceManager.getString(coreRes.string.core_user_not_active)
                        )
                    )
                    _uiState.value = DeleteProfileFragmentUIState.Initial
                } else {
                    _uiState.value =
                        DeleteProfileFragmentUIState.Error(
                            resourceManager.getString(profileRes.string.profile_password_is_incorrect)
                        )
                }
                logDeleteProfileEvent(false)
            }
        }
    }

    private fun logDeleteProfileClickedEvent() {
        logEvent(ProfileAnalyticsEvent.USER_DELETE_ACCOUNT_CLICKED)
    }

    private fun logDeleteProfileEvent(isSuccess: Boolean) {
        logEvent(
            ProfileAnalyticsEvent.DELETE_ACCOUNT_SUCCESS,
            buildMap {
                put(ProfileAnalyticsKey.SUCCESS.key, isSuccess)
            }
        )
    }

    private fun logEvent(event: ProfileAnalyticsEvent, param: Map<String, Any?> = emptyMap()) {
        analytics.logEvent(
            event.eventName,
            buildMap {
                put(ProfileAnalyticsKey.NAME.key, event.biValue)
                put(ProfileAnalyticsKey.CATEGORY.key, ProfileAnalyticsKey.PROFILE.key)
                putAll(param)
            }
        )
    }
}
