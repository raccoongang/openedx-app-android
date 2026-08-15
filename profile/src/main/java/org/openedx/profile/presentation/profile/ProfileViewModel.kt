package org.openedx.profile.presentation.profile

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.openedx.core.config.Config
import org.openedx.core.data.storage.CorePreferences
import org.openedx.core.lmsdirectory.LmsDirectoryState
import org.openedx.foundation.presentation.BaseViewModel
import org.openedx.foundation.system.ResourceManager
import org.openedx.profile.domain.interactor.ProfileInteractor
import org.openedx.profile.presentation.ProfileAnalytics
import org.openedx.profile.presentation.ProfileAnalyticsEvent
import org.openedx.profile.presentation.ProfileAnalyticsKey
import org.openedx.profile.presentation.ProfileRouter
import org.openedx.profile.system.notifier.account.AccountUpdated
import org.openedx.profile.system.notifier.profile.ProfileNotifier

class ProfileViewModel(
    private val interactor: ProfileInteractor,
    private val resourceManager: ResourceManager,
    private val notifier: ProfileNotifier,
    private val analytics: ProfileAnalytics,
    private val config: Config,
    private val corePreferences: CorePreferences,
    val profileRouter: ProfileRouter
) : BaseViewModel(resourceManager) {

    /** LMS Directory: show the "Report this LMS" entry only when the feature is on. */
    val isLmsDirectoryEnabled: Boolean get() = config.getLMSDirectoryConfig().isReachable

    /**
     * Whether to offer "Report this LMS".
     *
     * Reporting belongs to the universal app: a build reading its list from a
     * document has no service to post to, a curated catalog vouches for its own
     * platforms, and a live catalog that has not answered yet is not known to be
     * either — so all three keep the entry point hidden.
     *
     * A flow rather than a value, because the answer can arrive after this screen
     * is already on top: the launch-time refresh is what tells a build whose
     * platform picker never runs again that its registry has changed.
     */
    val canReportLms: StateFlow<Boolean> = LmsDirectoryState.revision
        .map { LmsDirectoryState.canReport(config.getLMSDirectoryConfig(), corePreferences) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            LmsDirectoryState.canReport(config.getLMSDirectoryConfig(), corePreferences)
        )

    private companion object {
        /** Keeps the flow alive across a configuration change. */
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }

    private val _uiState: MutableStateFlow<ProfileUIState> = MutableStateFlow(ProfileUIState.Loading)
    internal val uiState: StateFlow<ProfileUIState> = _uiState.asStateFlow()

    private val _isUpdating = MutableLiveData<Boolean>()
    val isUpdating: LiveData<Boolean>
        get() = _isUpdating

    init {
        getAccount()
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        viewModelScope.launch {
            notifier.notifier.collect {
                if (it is AccountUpdated) {
                    getAccount()
                }
            }
        }
    }

    private fun getAccount() {
        _uiState.value = ProfileUIState.Loading
        viewModelScope.launch {
            try {
                val cachedAccount = interactor.getCachedAccount()
                if (cachedAccount == null) {
                    _uiState.value = ProfileUIState.Loading
                } else {
                    _uiState.value = ProfileUIState.Data(
                        account = cachedAccount
                    )
                }
                val account = interactor.getAccount()
                _uiState.value = ProfileUIState.Data(
                    account = account
                )
            } catch (e: Exception) {
                handleErrorUiMessage(
                    throwable = e,
                )
            } finally {
                _isUpdating.value = false
            }
        }
    }

    fun updateAccount() {
        _isUpdating.value = true
        getAccount()
    }

    fun profileEditClicked(fragmentManager: FragmentManager) {
        (uiState.value as? ProfileUIState.Data)?.let { data ->
            profileRouter.navigateToEditProfile(
                fragmentManager,
                data.account
            )
        }
        logProfileEvent(ProfileAnalyticsEvent.EDIT_CLICKED)
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
