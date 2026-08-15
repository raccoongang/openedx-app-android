package org.openedx.core.lmsdirectory

import android.util.Log

/**
 * Supplies the platform list. All calls return [Result] so callers can fall back
 * gracefully when the source is unreachable.
 *
 * [source] decides where the list comes from — a live service or a single JSON
 * document, hosted or shipped in the app. [api] is only needed for the one thing
 * a document cannot do, which is send a report back.
 */
class LmsDirectoryRepository(
    private val source: LmsDirectorySource,
    private val appVersion: String,
    private val api: LmsDirectoryApi? = null,
) {
    companion object {
        private const val TAG = "LmsDirectory"
    }

    suspend fun fetchConfig(): DirectoryConfig {
        return try {
            source.config()
        } catch (e: Exception) {
            // Only a live service can fail this way, and falling back to search is
            // why a white-label build needs the local DIRECTORY_MODE guard. A
            // document source never reaches here: it has no server to ask.
            Log.w(TAG, "Config fetch failed, defaulting to search mode: ${e.message}")
            DirectoryConfig.SEARCH_DEFAULT
        }
    }

    /**
     * The registry's own answer, or null when it could not be reached.
     *
     * Unlike [fetchConfig] this does not fall back to search: a network error is
     * not evidence that a catalog is open to anyone, and whoever records the mode
     * has to be able to tell those two apart.
     */
    suspend fun fetchConfigOrNull(): DirectoryConfig? =
        runCatching { source.config() }
            .onFailure { Log.w(TAG, "Config fetch failed: ${it.message}") }
            .getOrNull()

    suspend fun search(query: String): Result<List<LmsSummary>> = runCatching {
        source.search(query)
    }.onFailure { Log.w(TAG, "Search failed: ${it.message}") }

    suspend fun fetchFeatured(): Result<List<LmsSummary>> = runCatching {
        source.featured()
    }.onFailure { Log.w(TAG, "Featured fetch failed: ${it.message}") }

    /** Full record for one platform (includes the OAuth client id needed to sign in). */
    suspend fun fetchDetail(id: String): Result<LmsDetail> = runCatching {
        source.detail(id)
    }.onFailure { Log.w(TAG, "Detail fetch failed: ${it.message}") }

    /** Images the list will need, for warming before the screens that show them. */
    suspend fun imageReferences(): List<String> =
        runCatching { source.imageReferences() }
            .onFailure { Log.w(TAG, "Could not list directory images: ${it.message}") }
            .getOrDefault(emptyList())

    /**
     * Send a complaint back to the service. Fails when the directory came from a
     * document, which is a one-way list with nothing to post to — the screens that
     * offer this are hidden in that case.
     */
    suspend fun submitReport(draft: ReportDraft): Result<Unit> = runCatching {
        val api = requireNotNull(api) { "This directory has no service to report to" }
        api.submitReport(
            ReportRequestBody(
                lmsId = draft.lmsId?.toIntOrNull(),
                baseUrl = draft.baseUrl,
                category = draft.category.apiValue,
                message = draft.message,
                reporterEmail = draft.reporterEmail?.trim()?.ifBlank { null },
                appVersion = appVersion,
                screenshotBase64 = draft.screenshotBase64,
            )
        )
        Unit
    }.onFailure { Log.w(TAG, "Report submit failed: ${it.message}") }
}
