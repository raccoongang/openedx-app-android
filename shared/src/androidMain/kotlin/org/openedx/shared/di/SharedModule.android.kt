package org.openedx.shared.di

import org.koin.core.module.Module
import org.koin.dsl.module
import org.openedx.shared.network.NetworkConnection

actual fun platformModule(): Module = module {
    single { NetworkConnection(get()) }
}
