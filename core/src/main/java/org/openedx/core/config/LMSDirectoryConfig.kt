package org.openedx.core.config

import com.google.gson.annotations.SerializedName
import org.openedx.core.lmsdirectory.LmsDirectoryMode

/**
 * Feature flag for the multi-tenant LMS Directory.
 *
 * When [enabled], the app can browse the Open edX platforms published by a site
 * registry ([directoryUrl]), re-theme to the one the learner picks, and sign in
 * against it. Off by default — the app then behaves as a stock single-tenant build.
 */
data class LMSDirectoryConfig(
    @SerializedName("ENABLED")
    val enabled: Boolean = false,

    @SerializedName("DIRECTORY_URL")
    val directoryUrl: String = "",

    /**
     * A JSON document added to the app's assets, e.g. "lms_directory.json". Set it
     * and the app reads its platform list from there and never asks the network.
     */
    @SerializedName("DIRECTORY_FILE")
    val directoryFile: String = "",

    @SerializedName("DIRECTORY_MODE")
    val directoryMode: String = "",
) {

    /**
     * How to read the directory.
     *
     * A bundled file wins over a URL: a build that ships its own copy has opted out
     * of the network, and quietly preferring a remote list would undo that.
     */
    sealed interface Source {
        /** A JSON document in the app's assets. */
        data class BundledDocument(val fileName: String) : Source

        /** A JSON document to fetch once. */
        data class Document(val url: String) : Source

        /** A live catalog answering /api/v1/directory. */
        data class Service(val url: String) : Source
    }

    val source: Source?
        get() {
            if (!enabled) return null
            val file = directoryFile.trim()
            if (file.isNotEmpty()) return Source.BundledDocument(file)
            val url = directoryUrl.trim()
            if (url.isEmpty()) return null
            // A ".json" address is a document; anything else is a service to query.
            // The difference is visible in the config file, which is where whoever
            // set it will look when the app does not do what they expected.
            val path = url.substringBefore('?').substringBefore('#')
            return if (path.endsWith(".json", ignoreCase = true)) {
                Source.Document(url)
            } else {
                Source.Service(url)
            }
        }

    /**
     * The single gate for activating any LMS Directory behaviour: the feature only
     * works with a registry to talk to, so an ENABLED:true build with a blank
     * [directoryUrl] stays fully single-tenant instead of building clients against an
     * invalid stub.
     *
     * This deliberately diverges from the white-label source, which gates purely on the
     * directory URL being non-blank. It is equivalent in effect (a directory URL is only
     * ever present when the feature is on) and safer, because it also refuses to activate
     * on the ENABLED:true + empty-URL misconfiguration.
     */
    val isReachable: Boolean
        get() = enabled && (directoryFile.isNotBlank() || directoryUrl.isNotBlank())

    /**
     * Whether this build can report a platform to anyone.
     *
     * Reporting exists because the open catalog lets a stranger list anything; it
     * belongs to the universal app, not to a provider's own list. A directory read
     * from a document has no service behind it, so there is nothing to post to and
     * the entry point must not appear.
     *
     * A live service in curated mode also refuses reports, and is hidden separately
     * by the mode itself.
     */
    val supportsReporting: Boolean get() = source is Source.Service

    /**
     * A stable identifier for the configured source.
     *
     * Anything remembered *about* a source — the last mode the server reported,
     * for instance — is only meaningful while the source is the same one. Storing
     * this beside such a value is what lets a reader notice the build has been
     * pointed somewhere else and ignore what it remembers.
     */
    val sourceKey: String
        get() = when (val current = source) {
            is Source.Service -> "service:${current.url}"
            is Source.Document -> "document:${current.url}"
            is Source.BundledDocument -> "file:${current.fileName}"
            null -> ""
        }

    /**
     * What kind of list this is, as far as the config file alone can settle it.
     *
     * A document is a fixed list by construction. For a service, DIRECTORY_MODE
     * settles it either way when set. Null means the config does not know and the
     * server has to be asked.
     */
    val configuredMode: LmsDirectoryMode?
        get() = when (source) {
            is Source.Document, is Source.BundledDocument -> LmsDirectoryMode.CURATED
            null -> LmsDirectoryMode.CURATED
            else -> when (directoryMode.trim().lowercase()) {
                "curated" -> LmsDirectoryMode.CURATED
                "search" -> LmsDirectoryMode.SEARCH
                else -> null
            }
        }
}
