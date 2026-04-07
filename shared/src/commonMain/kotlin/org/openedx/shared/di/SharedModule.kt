package org.openedx.shared.di

import org.koin.core.module.Module
import org.koin.dsl.module
import org.openedx.shared.deeplink.DefaultDeepLinkHandler
import org.openedx.shared.deeplink.DeepLinkHandler

/**
 * Common Koin module for shared dependencies.
 * Platform-specific modules extend this with actual implementations.
 */
val sharedModule = module {
    single<DeepLinkHandler> { DefaultDeepLinkHandler() }
}

/**
 * Platform-specific module that provides actual implementations.
 * Must be provided by each platform.
 */
expect fun platformModule(): Module
