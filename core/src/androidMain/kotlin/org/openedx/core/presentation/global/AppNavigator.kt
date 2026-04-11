package org.openedx.core.presentation.global

import androidx.navigation.NavHostController

/**
 * Global navigation wrapper that provides NavHostController access
 * throughout the app. This is the bridge between Fragment-based navigation
 * (using FragmentManager) and Compose Navigation (using NavHostController).
 *
 * During migration:
 * - Old code uses FragmentManager via Router interfaces
 * - New code uses AppNavigator.navController
 * - Both coexist until all Fragments are migrated
 */
class AppNavigator {
    var navController: NavHostController? = null
        private set

    fun setNavController(controller: NavHostController) {
        navController = controller
    }

    fun navigateBack() {
        navController?.popBackStack()
    }

    fun <T : Any> navigateTo(route: T) {
        navController?.navigate(route)
    }

    fun <T : Any> navigateAndClearBackStack(route: T) {
        navController?.navigate(route) {
            popUpTo(0) { inclusive = true }
        }
    }
}
