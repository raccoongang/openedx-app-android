package org.openedx.core.config

import com.google.gson.annotations.SerializedName

/**
 * Feature flag for the multi-tenant LMS Directory: a build that lets a learner
 * choose which Open edX platform to sign in to. Off by default — the app then
 * behaves as a stock single-tenant build.
 */
data class LMSDirectoryConfig(
    @SerializedName("ENABLED")
    val enabled: Boolean = false,

    /** Address of a JSON document listing the platforms this build offers. */
    @SerializedName("DIRECTORY_URL")
    val directoryUrl: String = "",

    /**
     * The same document, in the app's assets, e.g. "lms_directory.json". Set it
     * and the app reads its platform list from there and never asks the network.
     */
    @SerializedName("DIRECTORY_FILE")
    val directoryFile: String = "",
) {

    /**
     * Where the document comes from.
     *
     * A bundled file wins over a URL: a build that ships its own copy has opted out
     * of the network, and quietly preferring a remote list would undo that.
     */
    sealed interface Source {
        /** Read from the app's assets. Never touches the network. */
        data class BundledDocument(val fileName: String) : Source

        /** Fetched once, from anywhere the publisher chose to put it. */
        data class Document(val url: String) : Source
    }

    val source: Source?
        get() {
            if (!enabled) return null
            val file = directoryFile.trim()
            if (file.isNotEmpty()) return Source.BundledDocument(file)
            val url = directoryUrl.trim()
            return if (url.isEmpty()) null else Source.Document(url)
        }

    /**
     * The single gate for activating any LMS Directory behaviour. An ENABLED:true
     * build with nothing configured stays fully single-tenant rather than starting
     * a feature that has no list to show.
     */
    val isReachable: Boolean
        get() = enabled && (directoryFile.isNotBlank() || directoryUrl.isNotBlank())
}
