package org.openedx.core.lmsdirectory

import android.content.Context
import okhttp3.OkHttpClient
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.openedx.core.config.Config
import org.openedx.core.config.LMSDirectoryConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Koin module for the LMS directory.
 *
 * The list comes from whichever source the config names — a live catalog, a JSON
 * document to fetch, or one shipped in the app's assets. Only the live catalog
 * builds a Retrofit client, because only it has anything to send back.
 */
val lmsDirectoryModule = module {

    single(qualifier = named("LmsDirectory")) {
        OkHttpClient.Builder()
            .connectTimeout(DIRECTORY_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(DIRECTORY_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    single<LmsDirectoryApi> {
        val rawUrl = get<Config>().getLMSDirectoryConfig().directoryUrl
        Retrofit.Builder()
            .baseUrl(normalizeBaseUrl(rawUrl))
            .client(get(qualifier = named("LmsDirectory")))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LmsDirectoryApi::class.java)
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

            // Null means the feature is off or unconfigured. The directory is gated
            // on isReachable before anything resolves this, so the Retrofit client
            // built against the stub URL is never actually called.
            else -> ApiLmsDirectorySource(get())
        }
    }

    single {
        val context = get<Context>()
        val version = runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull().orEmpty()
        val isService = get<Config>().getLMSDirectoryConfig().source is LMSDirectoryConfig.Source.Service
        LmsDirectoryRepository(
            source = get(),
            appVersion = version,
            // Reporting needs somewhere to post; a document has nowhere.
            api = if (isService) get() else null,
        )
    }
}

private const val DIRECTORY_TIMEOUT_SECONDS = 20L

/** Retrofit requires an absolute URL ending in "/". Blank config yields a safe stub. */
private fun normalizeBaseUrl(url: String): String {
    val trimmed = url.trim()
    if (trimmed.isEmpty()) return "https://directory.invalid/"
    val withScheme = if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
        trimmed
    } else {
        "https://$trimmed"
    }
    return if (withScheme.endsWith("/")) withScheme else "$withScheme/"
}
