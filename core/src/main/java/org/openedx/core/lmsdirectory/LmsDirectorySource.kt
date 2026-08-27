package org.openedx.core.lmsdirectory

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Where the platform list comes from.
 *
 * One implementation today — a JSON document, hosted or shipped with the app —
 * behind an interface so the screens do not care which of the two they got.
 */
interface LmsDirectorySource {
    /** The publisher's own name, shown above the list. Blank when they gave none. */
    suspend fun providerName(): String

    /** Every platform in the directory, in the order the document lists them. */
    suspend fun platforms(): List<LmsSummary>

    /** Everything needed to re-theme the app and sign in to one of them. */
    suspend fun detail(id: String): LmsDetail

    /**
     * Every image the list will ask for, so a caller can warm them before the
     * screens that show them are built.
     */
    suspend fun imageReferences(): List<String> = emptyList()
}

/**
 * A single JSON document, fetched from a URL or read out of the app's assets.
 *
 * Read once and kept: the picker, the theming and the image prefetch all work
 * from the same copy rather than parsing it three times. Because everything
 * arrives together, a platform's sign-in background is known before the learner
 * has picked anything, which is what lets the artwork be warmed in advance.
 */
class DocumentLmsDirectorySource(
    private val loader: DocumentLoader,
    private val gson: Gson = Gson(),
) : LmsDirectorySource {

    /** How the bytes are obtained. Kept separate so tests need no network or app. */
    fun interface DocumentLoader {
        suspend fun load(): String
    }

    private var cached: DirectoryDocumentDto? = null

    override suspend fun providerName(): String = document().provider?.name.orEmpty()

    override suspend fun platforms(): List<LmsSummary> =
        document().include.mapIndexed { index, entry -> entry.toSummary(index.toString()) }

    override suspend fun detail(id: String): LmsDetail =
        document().include.getOrNull(id.toIntOrNull() ?: -1)?.toDomain(id)
            ?: throw NoSuchElementException("No platform at position $id in the directory document")

    override suspend fun imageReferences(): List<String> =
        document().include.flatMap {
            listOfNotNull(it.logo, it.theme?.loginBackground)
        }.filter { it.isNotBlank() }

    private suspend fun document(): DirectoryDocumentDto {
        cached?.let { return it }
        val raw = loader.load()
        val parsed = gson.fromJson(raw, DirectoryDocumentDto::class.java)
        checkNotNull(parsed) { "Directory document is empty" }
        if (parsed.include.isEmpty()) {
            Log.w(TAG, "Directory document parsed but lists no platforms")
        }
        cached = parsed
        return parsed
    }

    companion object {
        private const val TAG = "LmsDirectory"

        /** Reads a document shipped in the app's assets. Never touches the network. */
        fun fromAsset(context: Context, fileName: String): DocumentLmsDirectorySource =
            DocumentLmsDirectorySource(
                loader = {
                    withContext(Dispatchers.IO) {
                        context.assets.open(fileName).bufferedReader().use { it.readText() }
                    }
                }
            )

        /** Fetches a document over HTTP, once. */
        fun fromUrl(client: OkHttpClient, url: String): DocumentLmsDirectorySource =
            DocumentLmsDirectorySource(
                loader = {
                    withContext(Dispatchers.IO) {
                        client.newCall(Request.Builder().url(url).build()).execute().use { response ->
                            check(response.isSuccessful) {
                                "Directory document returned ${response.code}"
                            }
                            checkNotNull(response.body?.string()) {
                                "Directory document had no body"
                            }
                        }
                    }
                }
            )
    }
}

/**
 * Wire format of the directory document.
 *
 * The key names are the ones the Open edX mobile working group settled on, so a
 * file written by hand and a file exported from a registry are the same shape.
 */
data class DirectoryDocumentDto(
    @SerializedName("format") val format: String? = null,
    @SerializedName("provider") val provider: ProviderDto? = null,
    @SerializedName("include") val include: List<LmsDetailDto> = emptyList(),
) {
    data class ProviderDto(
        @SerializedName("name") val name: String? = null,
        @SerializedName("tagline") val tagline: String? = null,
        @SerializedName("logo") val logo: String? = null,
    )
}

private fun LmsDetailDto.toSummary(id: String): LmsSummary = LmsSummary(
    id = id,
    title = name,
    shortDescription = description.orEmpty(),
    baseUrl = api?.hostUrl?.ifBlank { null } ?: url,
    logoUrl = logo,
    accentColor = accentColor,
)
