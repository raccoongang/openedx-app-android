package org.openedx.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import org.openedx.app.MainScreen
import org.openedx.auth.presentation.restore.RestorePasswordUIState
import org.openedx.auth.presentation.restore.RestorePasswordViewModel
import org.openedx.auth.presentation.restore.compose.RestorePasswordScreen
import org.openedx.auth.presentation.signin.AuthEvent
import org.openedx.auth.presentation.signin.SignInViewModel
import org.openedx.auth.presentation.signin.compose.LoginScreen
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.foundation.presentation.rememberWindowSize

/**
 * Root navigation host for the app.
 * Hosts composable destinations that replace Fragments.
 */
@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: Any = AppNavRoutes.Main(),
    fragmentManager: FragmentManager? = null,
    modifier: Modifier = Modifier,
) {
    OpenEdXTheme {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier,
        ) {
            // Main screen with bottom navigation
            composable<AppNavRoutes.Main> {
                MainScreen()
            }

            // Auth: Sign In
            composable<AppNavRoutes.SignIn> { entry ->
                val route = entry.toRoute<AppNavRoutes.SignIn>()
                val viewModel: SignInViewModel = koinViewModel {
                    parametersOf(route.courseId ?: "", route.infoType ?: "", route.authCode)
                }
                val windowSize = rememberWindowSize()
                val state by viewModel.uiState.collectAsState()
                val uiMessage by viewModel.uiMessage.collectAsState(initial = null)

                LoginScreen(
                    windowSize = windowSize,
                    state = state,
                    uiMessage = uiMessage,
                    onEvent = { event ->
                        when (event) {
                            is AuthEvent.SignIn -> viewModel.login(event.login, event.password)
                            AuthEvent.ForgotPasswordClick -> {
                                navController.navigate(AppNavRoutes.RestorePassword)
                            }
                            AuthEvent.RegisterClick -> {
                                navController.navigate(AppNavRoutes.SignUp(route.courseId, route.infoType))
                            }
                            AuthEvent.BackClick -> navController.popBackStack()
                            else -> {} // Social auth, browser login - handled by ViewModel
                        }
                    },
                )
            }

            // Auth: Restore Password
            composable<AppNavRoutes.RestorePassword> {
                val viewModel: RestorePasswordViewModel = koinViewModel()
                val windowSize = rememberWindowSize()
                val uiState by viewModel.uiState.observeAsState(RestorePasswordUIState.Initial)
                val uiMessage by viewModel.uiMessage.collectAsState(initial = null)

                RestorePasswordScreen(
                    windowSize = windowSize,
                    uiState = uiState,
                    uiMessage = uiMessage,
                    onBackClick = { navController.popBackStack() },
                    onRestoreButtonClick = { viewModel.passwordReset(it) },
                )
            }

            // Auth: Sign Up
            composable<AppNavRoutes.SignUp> { entry ->
                val route = entry.toRoute<AppNavRoutes.SignUp>()
                // SignUp screen - delegates to Fragment for now due to social auth complexity
                // TODO: Wire SignUpView composable directly
            }

            // WhatsNew
            composable<AppNavRoutes.WhatsNew> {
                // TODO: Wire WhatsNewScreen composable
            }

            // Other destinations to be added incrementally
        }
    }
}
