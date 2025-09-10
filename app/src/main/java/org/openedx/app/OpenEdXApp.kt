package org.openedx.app

import android.app.Application
import com.braze.Braze
import com.braze.configuration.BrazeConfig
import com.braze.ui.BrazeDeeplinkHandler
import com.google.firebase.FirebaseApp
import io.branch.referral.Branch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.openedx.app.deeplink.BranchBrazeDeeplinkHandler
import org.openedx.app.di.appModule
import org.openedx.app.di.networkingModule
import org.openedx.app.di.screenModule
import org.openedx.core.config.Config
import org.openedx.core.data.storage.CorePreferences
import org.openedx.core.oex.foundation.PurchaseProviderRevenueCat
import org.openedx.core.oex.foundation.RevenueCatConfiguration
import org.openedx.firebase.OEXFirebaseAnalytics

class OpenEdXApp : Application() {

    private val config by inject<Config>()
    private val pluginManager by inject<PluginManager>()
    private val corePreferences by inject<CorePreferences>()

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@OpenEdXApp)
            modules(
                appModule,
                networkingModule,
                screenModule
            )
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

    private fun initPlugins() {
        // In-app purchases plugin (RevenueCat)
        if (config.isInAppPurchasesEnabled()) {
            val revenueCat = PurchaseProviderRevenueCat()
            val rcUserId = corePreferences.user?.username
            val configuration = RevenueCatConfiguration(
                application = this@OpenEdXApp,
                apiKey = "goog_ppnXKLYmeQGTVKRzLGYPJxGAetl",
                userID = rcUserId
            )
            revenueCat.configure(configuration)
            pluginManager.addPlugin(revenueCat)
        }
        if (config.getFirebaseConfig().enabled) {
            pluginManager.addPlugin(OEXFirebaseAnalytics(context = this))
        }
    }
}
