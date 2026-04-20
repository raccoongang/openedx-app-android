package org.openedx.shared.di

import org.koin.core.context.startKoin
import org.openedx.app.di.commonScreenModule

/**
 * Initializes Koin DI for iOS.
 * Called from Swift code at app startup.
 */
fun initKoin() {
    startKoin {
        modules(sharedModule, platformModule(), commonScreenModule)
    }
}
