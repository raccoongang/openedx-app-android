package org.openedx.profile.presentation.edit

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.openedx.core.config.Config
import org.openedx.foundation.presentation.BaseViewModel
import org.openedx.foundation.system.ResourceManager
import org.openedx.profile.domain.interactor.ProfileInteractor
import org.openedx.profile.domain.model.Account
import org.openedx.profile.presentation.ProfileAnalytics
import org.openedx.profile.presentation.ProfileAnalyticsEvent
import org.openedx.profile.presentation.ProfileAnalyticsKey
import org.openedx.profile.system.notifier.account.AccountUpdated
import org.openedx.profile.system.notifier.profile.ProfileNotifier
import org.openedx.foundation.Res as foundationRes
import org.openedx.foundation.foundation_error_no_connection
import org.openedx.foundation.foundation_error_unknown_error

class EditProfileViewModel(
    private val interactor: ProfileInteractor,
    private val resourceManager: ResourceManager,
    private val notifier: ProfileNotifier,
    private val analytics: ProfileAnalytics,
    val config: Config,
    account: Account,
) : BaseViewModel(
    noConnectionMessage = resourceManager.getString(foundationRes.string.foundation_error_no_connection),
    defaultErrorMessage = resourceManager.getString(foundationRes.string.foundation_error_unknown_error),
) {

    private val _uiState = MutableStateFlow<EditProfileUIState?>(null)
    val uiState: StateFlow<EditProfileUIState?> = _uiState.asStateFlow()

    var account = account
        private set

    private val _selectedImageUri = MutableStateFlow<String?>(null)
    val selectedImageUri: StateFlow<String?> = _selectedImageUri.asStateFlow()

    private val _deleteImage = MutableStateFlow(false)
    val deleteImage: StateFlow<Boolean> = _deleteImage.asStateFlow()

    var profileDataChanged = false
    var isLimitedProfile: Boolean = account.isLimited()
        set(value) {
            field = value
            _uiState.value = EditProfileUIState(account, isLimited = value)
            logProfileEvent(
                ProfileAnalyticsEvent.SWITCH_PROFILE,
                buildMap {
                    put(
                        ProfileAnalyticsKey.ACTION.key,
                        if (isLimitedProfile) {
                            ProfileAnalyticsKey.LIMITED_PROFILE.key
                        } else {
                            ProfileAnalyticsKey.FULL_PROFILE.key
                        }
                    )
                }
            )
        }

    private val _showLeaveDialog = MutableStateFlow(false)
    val showLeaveDialog: StateFlow<Boolean> = _showLeaveDialog.asStateFlow()

    init {
        logProfileScreenEvent(ProfileAnalyticsEvent.EDIT_PROFILE)
    }

    fun updateAccount(fields: Map<String, Any?>) {
        _uiState.value = EditProfileUIState(account, true, isLimitedProfile)
        viewModelScope.launch {
            try {
                if (deleteImage.value == true) {
                    interactor.deleteProfileImage()
                }
                val updatedAccount = interactor.updateAccount(fields)
                account = updatedAccount
                isLimitedProfile = updatedAccount.isLimited()
                _uiState.value =
                    EditProfileUIState(updatedAccount, isUpdating = false, isLimitedProfile)
                sendAccountUpdated()
                _deleteImage.value = false
                _selectedImageUri.value = null
            } catch (e: Exception) {
                _uiState.value = EditProfileUIState(account.copy(), isLimited = isLimitedProfile)
                handleErrorUiMessage(
                    throwable = e,
                )
            }
        }
    }

    fun updateAccountAndImage(fields: Map<String, Any?>, imageBody: org.openedx.profile.data.repository.ImageBody) {
        _uiState.value = EditProfileUIState(account, true, isLimitedProfile)
        viewModelScope.launch {
            try {
                interactor.setProfileImage(imageBody)
                val updatedAccount = interactor.updateAccount(fields)
                account = updatedAccount
                isLimitedProfile = updatedAccount.isLimited()
                _uiState.value =
                    EditProfileUIState(updatedAccount, isUpdating = false, isLimitedProfile)
                _selectedImageUri.value = null
                sendAccountUpdated()
            } catch (e: Exception) {
                _uiState.value = EditProfileUIState(account.copy(), isLimited = isLimitedProfile)
                handleErrorUiMessage(
                    throwable = e,
                )
            }
        }
    }

    fun deleteImage() {
        _deleteImage.value = true
        _selectedImageUri.value = null
    }

    fun setImageUri(uri: String) {
        _selectedImageUri.value = uri
        _deleteImage.value = false
    }

    fun setShowLeaveDialog(value: Boolean) {
        _showLeaveDialog.value = value
    }

    private suspend fun sendAccountUpdated() {
        notifier.send(AccountUpdated())
    }

    fun profileEditDoneClickedEvent() {
        logProfileEvent(ProfileAnalyticsEvent.EDIT_DONE_CLICKED)
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

    private fun logProfileScreenEvent(
        event: ProfileAnalyticsEvent,
        params: Map<String, Any?> = emptyMap(),
    ) {
        analytics.logScreenEvent(
            screenName = event.eventName,
            params = buildMap {
                put(ProfileAnalyticsKey.NAME.key, event.biValue)
                put(ProfileAnalyticsKey.CATEGORY.key, ProfileAnalyticsKey.PROFILE.key)
                putAll(params)
            }
        )
    }
}
