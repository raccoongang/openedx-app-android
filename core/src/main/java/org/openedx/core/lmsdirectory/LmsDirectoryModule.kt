package org.openedx.core.lmsdirectory

import android.content.Context
import okhttp3.OkHttpClient
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.openedx.core.config.Config
import org.openedx.core.config.LMSDirectoryConfig
import java.util.concurrent.TimeUnit

/**
 * Koin module for the LMS directory.
 *
 * The list comes from whichever source the config names: a document to fetch, or
 * one shipped in the app's assets. Nothing resolves this unless the feature is
 * configured — see [LMSDirectoryConfig.isReachable].
 */
val lmsDirectoryModule = module {

    single(qualifier = named("LmsDirectory")) {
        OkHttpClient.Builder()
            .connectTimeout(DIRECTORY_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(DIRECTORY_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    single<LmsDirectorySource> {
        when (val source = get<Config>().getLMSDirectoryConfig().source) {
            is LMSDirectoryConfig.Source.BundledDocument ->
                DocumentLmsDirectorySource.fromAsset(get<Context>(), source.fileName)

            is LMSDirectoryConfig.Source.Document ->
                DocumentLmsDirectorySource.fromUrl(
                    get(qualifier = named("LmsDirectory")),
                    source.url
                )

            null -> error("The LMS directory has no configured source")
        }
    }

    single { LmsDirectoryRepository(source = get()) }
}

private const val DIRECTORY_TIMEOUT_SECONDS = 20L
