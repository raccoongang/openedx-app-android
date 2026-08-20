package org.openedx.auth.presentation.lmsselection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.openedx.auth.presentation.AuthRouter
import org.openedx.core.config.Config
import org.openedx.core.ui.theme.OpenEdXTheme

/**
 * The platform picker, shown before sign-in when the directory is configured and
 * no platform has been chosen yet. Picking one re-themes the app to it and
 * continues into the normal sign-in flow.
 */
class SiteSelectionFragment : Fragment() {

    private val viewModel: SiteSelectionViewModel by viewModel()
    private val router: AuthRouter by inject()
    private val config: Config by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            OpenEdXTheme {
                val state by viewModel.uiState.collectAsState()

                LaunchedEffect(Unit) {
                    viewModel.actions.collect { action ->
                        when (action) {
                            is SiteSelectionViewModel.SiteSelectionAction.Success ->
                                continueAfterSelection(action.preLoginDiscovery)
                        }
                    }
                }

                SiteSelectionScreen(
                    state = state,
                    callbacks = SiteSelectionCallbacks(
                        onPlatformSelected = viewModel::onPlatformSelected,
                        onRetry = viewModel::retry,
                    )
                )
            }
        }
    }

    private fun continueAfterSelection(preLoginDiscovery: Boolean) {
        val fm = requireActivity().supportFragmentManager
        when {
            // The chosen platform starts on course Discovery — open that instead of
            // sign-in (native or webview per config), matching iOS.
            preLoginDiscovery -> if (config.getDiscoveryConfig().isViewTypeWebView()) {
                router.navigateToWebDiscoverCourses(fm, querySearch = "")
            } else {
                router.navigateToNativeDiscoverCourses(fm, querySearch = "")
            }

            config.isPreLoginExperienceEnabled() -> router.navigateToLogistration(fm, courseId = null)
            else -> router.navigateToSignIn(fm, courseId = null, infoType = null)
        }
    }
}
