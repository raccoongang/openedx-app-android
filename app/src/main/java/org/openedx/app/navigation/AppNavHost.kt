package org.openedx.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.openedx.core.ui.theme.OpenEdXTheme

/**
 * Root navigation host for the app.
 * This is a placeholder that will be expanded as Fragments are converted to composable destinations.
 *
 * Current approach: Activity still uses Fragment-based navigation.
 * Migration will proceed incrementally by converting one Fragment at a time.
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
            // Placeholder - destinations will be added as Fragments are converted
            composable<AppNavRoutes.Main> {
                // Will host the main screen with bottom navigation
            }
        }
    }
}
