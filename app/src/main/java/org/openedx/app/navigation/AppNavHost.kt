package org.openedx.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.openedx.app.MainScreen
import org.openedx.core.ui.theme.OpenEdXTheme

/**
 * Root navigation host for the app.
 *
 * Current state: AppActivity uses FragmentContainerView for backward compat.
 * This NavHost is prepared for full Compose Navigation migration.
 *
 * To switch: Replace FragmentContainerView in AppActivity.AppContent()
 * with AppNavHost() call.
 */
@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: Any = AppNavRoutes.Main(),
    modifier: Modifier = Modifier,
) {
    OpenEdXTheme {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier,
        ) {
            composable<AppNavRoutes.Main> {
                MainScreen()
            }

            // Auth flow — to be wired when Fragments are removed
            // composable<AppNavRoutes.SignIn> { SignInDestination(navController) }
            // composable<AppNavRoutes.SignUp> { SignUpDestination(navController) }
            // composable<AppNavRoutes.RestorePassword> { RestorePasswordDestination(navController) }
            // composable<AppNavRoutes.Logistration> { LogistrationDestination(navController) }
            // composable<AppNavRoutes.WhatsNew> { WhatsNewDestination(navController) }
        }
    }
}
