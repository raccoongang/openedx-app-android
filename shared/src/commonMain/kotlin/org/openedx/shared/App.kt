package org.openedx.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import org.koin.compose.koinInject
import org.openedx.app.navigation.AppNavHost
import org.openedx.app.navigation.AppNavRoutes
import org.openedx.core.config.Config
import org.openedx.core.data.storage.CorePreferences
import org.openedx.core.presentation.dialog.appreview.AppReviewHost
import org.openedx.core.presentation.dialog.appreview.AppReviewManager
import org.openedx.core.presentation.dialog.downloaddialog.DownloadDialogHost
import org.openedx.core.presentation.dialog.downloaddialog.DownloadDialogManager
import org.openedx.core.presentation.global.WhatsNewGlobalManager
import org.openedx.core.system.StorageManager
import org.openedx.core.system.notifier.app.AppNotifier
import org.openedx.core.system.notifier.app.LogoutEvent

/**
 * Root Composable for the OpenEdX Compose Multiplatform app.
 *
 * Mirrors the startDestination logic Android has in `AppActivity.AppContent()`:
 *   no user  + logistration enabled → Logistration
 *   no user  + logistration off     → SignIn
 *   has user + WhatsNew pending     → WhatsNew
 *   has user + no WhatsNew          → Main
 *
 * Also observes AppNotifier for LogoutEvent to navigate back to auth screen
 * (mirroring Android's AppActivity.observeLogoutEvent()).
 */
@Composable
fun App() {
    val navController = rememberNavController()
    val config: Config = koinInject()
    val prefs: CorePreferences = koinInject()
    val whatsNew: WhatsNewGlobalManager = koinInject()
    val appNotifier: AppNotifier = koinInject()
    val downloadDialogManager: DownloadDialogManager = koinInject()
    val storageManager: StorageManager = koinInject()
    val appReviewManager: AppReviewManager = koinInject()

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

    // Observe logout events — navigate to auth screen when user logs out
    LaunchedEffect(Unit) {
        appNotifier.notifier.collect { event ->
            if (event is LogoutEvent) {
                val dest = if (config.isPreLoginExperienceEnabled()) {
                    AppNavRoutes.Logistration()
                } else {
                    AppNavRoutes.SignIn()
                }
                navController.navigate(dest) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AppNavHost(
            navController = navController,
            startDestination = startDestination,
        )
        DownloadDialogHost(
            manager = downloadDialogManager,
            storageManager = storageManager,
        )
        AppReviewHost(manager = appReviewManager)
    }
}


