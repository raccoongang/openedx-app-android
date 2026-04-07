package org.openedx.shared.di

import org.koin.core.module.Module
import org.koin.dsl.module
import org.openedx.shared.network.NetworkConnection
import org.openedx.shared.storage.SecureStorage

actual fun platformModule(): Module = module {
    single { NetworkConnection() }
    single { SecureStorage() }
}
