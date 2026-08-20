package org.openedx.core.lmsdirectory

import android.util.Log

/**
 * Supplies the platform list. Calls return [Result] so a screen can say what went
 * wrong instead of showing an empty list and hoping.
 *
 * [source] decides where the list comes from — a document, hosted or shipped in
 * the app. Whoever builds the app decides that; nothing here is told about it.
 */
class LmsDirectoryRepository(private val source: LmsDirectorySource) {

    companion object {
        private const val TAG = "LmsDirectory"
    }

    /** The publisher's own name, shown above the list. Blank when they gave none. */
    suspend fun providerName(): String =
        runCatching { source.providerName() }
            .onFailure { Log.w(TAG, "Provider name unavailable: ${it.message}") }
            .getOrDefault("")

    suspend fun platforms(): Result<List<LmsSummary>> = runCatching {
        source.platforms()
    }.onFailure { Log.w(TAG, "Could not read the directory: ${it.message}") }

    /** Full record for one platform, including the OAuth client id sign-in needs. */
    suspend fun detail(id: String): Result<LmsDetail> = runCatching {
        source.detail(id)
    }.onFailure { Log.w(TAG, "Could not read platform $id: ${it.message}") }

    /** Images the list will need, for warming before the screens that show them. */
    suspend fun imageReferences(): List<String> =
        runCatching { source.imageReferences() }
            .onFailure { Log.w(TAG, "Could not list directory images: ${it.message}") }
            .getOrDefault(emptyList())
}
