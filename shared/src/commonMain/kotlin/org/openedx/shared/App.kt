package org.openedx.shared

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import org.koin.compose.koinInject
import org.openedx.app.navigation.AppNavHost
import org.openedx.app.navigation.AppNavRoutes
import org.openedx.core.config.Config
import org.openedx.core.data.storage.CorePreferences
import org.openedx.core.presentation.global.WhatsNewGlobalManager

/**
 * Root Composable for the OpenEdX Compose Multiplatform app.
 *
 * Mirrors the startDestination logic Android has in `AppActivity.AppContent()`:
 *   no user  + logistration enabled → Logistration
 *   no user  + logistration off     → SignIn
 *   has user + WhatsNew pending     → WhatsNew
 *   has user + no WhatsNew          → Main
 *
 * Android wires AppNavHost directly in `AppActivity` (splash / insets / Branch /
 * push handling), so this composable is iOS-only entry point today.
 */
@Composable
fun App() {
    val navController = rememberNavController()
    val config: Config = koinInject()
    val prefs: CorePreferences = koinInject()
    val whatsNew: WhatsNewGlobalManager = koinInject()

    val startDestination: Any = when {
        prefs.user == null -> {
            if (config.isPreLoginExperienceEnabled()) {
                AppNavRoutes.Logistration()
            } else {
                AppNavRoutes.SignIn()
            }
        }
        whatsNew.shouldShowWhatsNew() -> AppNavRoutes.WhatsNew()
        else -> AppNavRoutes.Main()
    }

    AppNavHost(
        navController = navController,
        startDestination = startDestination,
    )
}
