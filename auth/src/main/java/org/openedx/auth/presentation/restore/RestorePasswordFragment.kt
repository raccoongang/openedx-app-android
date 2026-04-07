package org.openedx.auth.presentation.restore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.openedx.auth.presentation.restore.compose.RestorePasswordScreen
import org.openedx.core.AppUpdateState
import org.openedx.core.presentation.global.appupgrade.AppUpgradeRequiredScreen
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.foundation.presentation.rememberWindowSize

@Deprecated("Replaced by Compose Navigation destination in AppNavHost")
class RestorePasswordFragment : Fragment() {

    private val viewModel: RestorePasswordViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            OpenEdXTheme {
                val windowSize = rememberWindowSize()

                val uiState by viewModel.uiState.observeAsState(RestorePasswordUIState.Initial)
                val uiMessage by viewModel.uiMessage.collectAsState(initial = null)
                val appUpgradeEvent by viewModel.appUpgradeEventUIState.observeAsState(null)

                if (appUpgradeEvent == null) {
                    RestorePasswordScreen(
                        windowSize = windowSize,
                        uiState = uiState,
                        uiMessage = uiMessage,
                        onBackClick = {
                            requireActivity().supportFragmentManager.popBackStackImmediate()
                        },
                        onRestoreButtonClick = {
                            viewModel.passwordReset(it)
                        }
                    )
                } else {
                    AppUpgradeRequiredScreen(
                        onUpdateClick = {
                            AppUpdateState.openPlayMarket(requireContext())
                        }
                    )
                }
            }
        }
    }
}
