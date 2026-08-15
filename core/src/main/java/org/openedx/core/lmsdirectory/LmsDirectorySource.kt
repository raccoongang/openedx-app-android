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
 * Two shapes exist. A live service is queried for each thing the app needs; a
 * document is read once and answers everything from memory. The repository above
 * cannot tell them apart, which is the point — whoever ships a build decides
 * where the list lives, and the app is not told anything about that choice.
 */
interface LmsDirectorySource {
    suspend fun config(): DirectoryConfig
    suspend fun search(query: String): List<LmsSummary>
    suspend fun featured(): List<LmsSummary>
    suspend fun detail(id: String): LmsDetail

    /**
     * Every image the list will ask for, so a caller can warm them before the
     * screens that show them are built. Empty when the source cannot know them
     * up front.
     */
    suspend fun imageReferences(): List<String> = emptyList()
}

/** The live catalog: one request per thing the app needs. */
class ApiLmsDirectorySource(private val api: LmsDirectoryApi) : LmsDirectorySource {

    override suspend fun config(): DirectoryConfig = api.getConfig().toDomain()

    override suspend fun search(query: String): List<LmsSummary> =
        api.search(query = query.ifBlank { null }).items.map { it.toDomain() }

    override suspend fun featured(): List<LmsSummary> =
        api.search(featured = true).items.map { it.toDomain() }

    override suspend fun detail(id: String): LmsDetail = api.detail(id).toDomain()
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

    override suspend fun config(): DirectoryConfig {
        val provider = document().provider
        // A fixed list has nothing to search across, so it is always curated. There
        // is no server to ask, which is exactly why a document build cannot fall
        // back to open search when it is offline.
        return DirectoryConfig(
            directoryMode = "curated",
            providerName = provider?.name.orEmpty(),
            providerTagline = provider?.tagline.orEmpty(),
        )
    }

    override suspend fun search(query: String): List<LmsSummary> {
        val platforms = document().platforms
        val needle = query.trim().lowercase()
        val matches = if (needle.isEmpty()) {
            platforms
        } else {
            platforms.filter {
                it.title.lowercase().contains(needle) || it.baseUrl.lowercase().contains(needle)
            }
        }
        return matches.map { it.toSummary() }
    }

    override suspend fun featured(): List<LmsSummary> = document().platforms.map { it.toSummary() }

    override suspend fun detail(id: String): LmsDetail =
        document().platforms.firstOrNull { it.id == id }?.toDomain()
            ?: throw NoSuchElementException("No platform with id $id in the directory document")

    override suspend fun imageReferences(): List<String> =
        document().platforms.flatMap {
            listOfNotNull(it.logoUrl, it.theme?.loginBackgroundUrl)
        }.filter { it.isNotBlank() }

    private suspend fun document(): DirectoryDocumentDto {
        cached?.let { return it }
        val raw = loader.load()
        val parsed = gson.fromJson(raw, DirectoryDocumentDto::class.java)
            ?: throw IllegalStateException("Directory document is empty")
        if (parsed.platforms.isEmpty()) {
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
                            if (!response.isSuccessful) {
                                throw IllegalStateException("Directory document returned ${response.code}")
                            }
                            response.body?.string()
                                ?: throw IllegalStateException("Directory document had no body")
                        }
                    }
                }
            )
    }
}

/**
 * Wire format of the directory document.
 *
 * `platforms` entries are the same objects `/api/v1/directory/{id}` returns, so
 * [LmsDetailDto] is reused rather than duplicated — a document and a live service
 * describe a platform identically, and keeping one parser is what guarantees it.
 */
data class DirectoryDocumentDto(
    @SerializedName("version") val version: Int = 1,
    @SerializedName("provider") val provider: ProviderDto? = null,
    @SerializedName("platforms") val platforms: List<LmsDetailDto> = emptyList(),
) {
    data class ProviderDto(
        @SerializedName("name") val name: String? = null,
        @SerializedName("tagline") val tagline: String? = null,
        @SerializedName("logo_url") val logoUrl: String? = null,
    )
}

private fun LmsDetailDto.toSummary(): LmsSummary = LmsSummary(
    id = id,
    title = title,
    shortDescription = shortDescription.orEmpty(),
    baseUrl = api?.hostUrl?.ifBlank { null } ?: baseUrl,
    logoUrl = logoUrl,
    accentColor = accentColor,
)
