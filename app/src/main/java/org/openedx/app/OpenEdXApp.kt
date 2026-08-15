package org.openedx.app

import android.app.Application
import com.braze.Braze
import com.braze.configuration.BrazeConfig
import com.braze.ui.BrazeDeeplinkHandler
import com.google.firebase.FirebaseApp
import io.branch.referral.Branch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.openedx.app.deeplink.BranchBrazeDeeplinkHandler
import org.openedx.app.di.appModule
import org.openedx.app.di.networkingModule
import org.openedx.app.di.screenModule
import org.openedx.core.config.Config
import org.openedx.core.data.storage.CorePreferences
import org.openedx.core.lmsdirectory.LmsDirectoryMode
import org.openedx.core.lmsdirectory.LmsDirectoryRepository
import org.openedx.core.lmsdirectory.LmsDirectoryState
import org.openedx.core.lmsdirectory.LmsThemeController
import org.openedx.core.lmsdirectory.lmsDirectoryModule
import org.openedx.firebase.OEXFirebaseAnalytics

class OpenEdXApp : Application() {

    private val config by inject<Config>()
    private val corePreferences by inject<CorePreferences>()
    private val pluginManager by inject<PluginManager>()
    private val directoryRepository by inject<LmsDirectoryRepository>()

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@OpenEdXApp)
            modules(
                appModule,
                networkingModule,
                screenModule,
                lmsDirectoryModule
            )
        }
        // LMS Directory: re-apply the selected platform's brand color on cold start so
        // the whole app is themed before the first screen composes. No-op when off.
        if (config.getLMSDirectoryConfig().isReachable) {
            // Anything remembered about a directory this build no longer reads is
            // dropped here, before a screen can act on it.
            LmsDirectoryState.reconcile(config.getLMSDirectoryConfig(), corePreferences)
            LmsThemeController.apply(corePreferences.selectedLmsAccentColor)
            LmsThemeController.applyBackground(corePreferences.selectedLmsLoginBackgroundUrl)
            refreshDirectoryMode()
        } else {
            LmsDirectoryState.clear(corePreferences)
        }
        if (config.getFirebaseConfig().enabled) {
            FirebaseApp.initializeApp(this)
        }

        if (config.getBranchConfig().enabled) {
            if (BuildConfig.DEBUG) {
                Branch.enableTestMode()
                Branch.enableLogging()
            }
            Branch.expectDelayedSessionInitialization(true)
            Branch.getAutoInstance(this)
        }

        if (config.getBrazeConfig().isEnabled && config.getFirebaseConfig().enabled) {
            val isCloudMessagingEnabled = config.getFirebaseConfig().isCloudMessagingEnabled &&
                    config.getBrazeConfig().isPushNotificationsEnabled

            val brazeConfig = BrazeConfig.Builder()
                .setIsFirebaseCloudMessagingRegistrationEnabled(isCloudMessagingEnabled)
                .setFirebaseCloudMessagingSenderIdKey(config.getFirebaseConfig().projectNumber)
                .setHandlePushDeepLinksAutomatically(true)
                .setIsFirebaseMessagingServiceOnNewTokenRegistrationEnabled(true)
                .build()
            Braze.configure(this, brazeConfig)

            if (config.getBranchConfig().enabled) {
                BrazeDeeplinkHandler.setBrazeDeeplinkHandler(BranchBrazeDeeplinkHandler())
            }
        }

        initPlugins()
    }

    /**
     * Ask the registry what kind of catalog it is, on every launch.
     *
     * The platform picker asks this too, but a learner who has already chosen a
     * platform never sees the picker again — so without this, a registry that
     * switched between an open catalog and a curated one would go unnoticed for
     * the life of the install.
     */
    private fun refreshDirectoryMode() {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            LmsDirectoryState.refresh(config.getLMSDirectoryConfig(), corePreferences) {
                val answer = directoryRepository.fetchConfigOrNull()
                when {
                    answer == null -> LmsDirectoryMode.UNKNOWN
                    answer.isCurated -> LmsDirectoryMode.CURATED
                    else -> LmsDirectoryMode.SEARCH
                }
            }
        }
    }

    private fun initPlugins() {
        if (config.getFirebaseConfig().enabled) {
            pluginManager.addPlugin(OEXFirebaseAnalytics(context = this))
        }
    }
}
