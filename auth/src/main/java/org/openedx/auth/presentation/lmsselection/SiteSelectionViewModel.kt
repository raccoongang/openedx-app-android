package org.openedx.auth.presentation.lmsselection

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.openedx.auth.R
import org.openedx.core.data.storage.CorePreferences
import org.openedx.core.lmsdirectory.LmsDirectoryRepository
import org.openedx.core.lmsdirectory.LmsSummary
import org.openedx.core.lmsdirectory.LmsThemeController
import org.openedx.foundation.presentation.BaseViewModel
import org.openedx.foundation.system.ResourceManager

/**
 * Drives the platform picker: show what the directory lists, and make the chosen
 * platform the one the app talks to — its host, its OAuth client, its branding —
 * then signal the fragment to continue to sign-in.
 */
class SiteSelectionViewModel(
    private val corePreferences: CorePreferences,
    private val resourceManager: ResourceManager,
    private val directoryRepository: LmsDirectoryRepository,
) : BaseViewModel(resourceManager) {

    private val _uiState = MutableStateFlow(SiteSelectionUIState())
    val uiState: StateFlow<SiteSelectionUIState> = _uiState

    private val _actions = MutableSharedFlow<SiteSelectionAction>()
    val actions: SharedFlow<SiteSelectionAction> = _actions.asSharedFlow()

    init {
        loadPlatforms()
    }

    fun retry() = loadPlatforms()

    private fun loadPlatforms() {
        _uiState.update { it.copy(catalog = CatalogState.Loading) }
        viewModelScope.launch {
            directoryRepository.platforms()
                .onSuccess { items ->
                    _uiState.update {
                        it.copy(
                            platforms = items,
                            providerName = directoryRepository.providerName(),
                            catalog = if (items.isEmpty()) CatalogState.Empty else CatalogState.Loaded,
                            // The whole list is in hand, so every logo and sign-in
                            // background is known before anything is tapped.
                            imageReferences = directoryRepository.imageReferences(),
                        )
                    }
                }
                .onFailure {
                    _uiState.update { state ->
                        state.copy(
                            catalog = CatalogState.Error(
                                resourceManager.getString(R.string.auth_lms_error_catalog)
                            )
                        )
                    }
                }
        }
    }

    fun onPlatformSelected(item: LmsSummary) {
        viewModelScope.launch {
            // The summary carries no OAuth client id, and sign-in needs the
            // platform's own registered mobile client to work.
            val detail = directoryRepository.detail(item.id).getOrNull()
            val normalized = normalizeUrl(detail?.baseUrl ?: item.baseUrl)
            if (normalized == null) {
                _uiState.update {
                    it.copy(
                        catalog = CatalogState.Error(
                            resourceManager.getString(R.string.auth_lms_error_invalid_url)
                        )
                    )
                }
                return@launch
            }
            selectLms(
                baseUrl = normalized.newBuilder().encodedPath("/").build().toString(),
                accentColor = detail?.accentColor ?: item.accentColor,
                oauthClientId = detail?.oauthClientId,
                feedbackEmail = detail?.feedbackEmail,
                logoUrl = detail?.logoUrl ?: item.logoUrl,
                title = detail?.title ?: item.title,
                loginBackgroundUrl = detail?.loginBackgroundUrl,
            )
            _actions.emit(SiteSelectionAction.Success(detail?.preLoginDiscovery ?: false))
        }
    }

    /**
     * Make this platform the one the app talks to.
     *
     * Everything the rest of the app needs about the chosen platform is written
     * here, because from this point on nothing else knows a directory existed.
     */
    private fun selectLms(
        baseUrl: String,
        accentColor: String?,
        oauthClientId: String?,
        feedbackEmail: String?,
        logoUrl: String?,
        title: String?,
        loginBackgroundUrl: String?,
    ) {
        corePreferences.selectedBaseUrl = baseUrl
        corePreferences.selectedLmsAccentColor = accentColor
        corePreferences.selectedOAuthClientId = oauthClientId
        corePreferences.selectedFeedbackEmail = feedbackEmail
        corePreferences.selectedLmsLogoUrl = logoUrl
        corePreferences.selectedLmsTitle = title
        corePreferences.selectedLmsLoginBackgroundUrl = loginBackgroundUrl
        LmsThemeController.apply(accentColor)
        LmsThemeController.applyBackground(loginBackgroundUrl)
    }

    sealed interface SiteSelectionAction {
        /** [preLoginDiscovery] true -> open the pre-login catalog instead of sign-in. */
        data class Success(val preLoginDiscovery: Boolean) : SiteSelectionAction
    }

    private fun normalizeUrl(text: String): HttpUrl? {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return null
        val withScheme = if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            trimmed
        } else {
            "https://$trimmed"
        }
        return withScheme.toHttpUrlOrNull()
    }
}
