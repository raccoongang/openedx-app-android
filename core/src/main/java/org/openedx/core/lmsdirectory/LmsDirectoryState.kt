package org.openedx.core.lmsdirectory

import org.openedx.core.config.LMSDirectoryConfig
import org.openedx.core.data.storage.CorePreferences

/**
 * What the app remembers about the directory between launches, and the rule that
 * decides when to stop believing it.
 *
 * Only the server can say whether a live catalog is curated, and it says so on a
 * screen the app skips once a platform has been picked. So the answer is
 * remembered — and a remembered answer is worth exactly nothing after the build
 * has been pointed at a different directory. Everything stored here is therefore
 * stamped with the source it came from, and read back only when that still matches.
 */
object LmsDirectoryState {

    /** Record what the server said, against the source that said it. */
    fun rememberCurated(
        curated: Boolean,
        config: LMSDirectoryConfig,
        preferences: CorePreferences
    ) {
        preferences.lmsDirectoryCurated = curated
        preferences.lmsDirectorySourceKey = config.sourceKey
    }

    /** Forget everything source-specific. Used when the feature is switched off. */
    fun clear(preferences: CorePreferences) {
        preferences.lmsDirectoryCurated = false
        preferences.lmsDirectorySourceKey = ""
    }

    /**
     * Whether this build shows a fixed list of platforms.
     *
     * The config decides on its own whenever it can. Only when it cannot — a live
     * service with no forced mode — does the remembered answer matter, and then
     * only if it was recorded against this same source. A value left over from a
     * different directory is discarded rather than obeyed, which is what stops a
     * build that used to be curated from staying curated after it is pointed at an
     * open catalog.
     */
    fun isCurated(config: LMSDirectoryConfig, preferences: CorePreferences): Boolean {
        val remembersThisSource = preferences.lmsDirectorySourceKey == config.sourceKey
        return config.isCuratedByConfiguration ||
            (remembersThisSource && preferences.lmsDirectoryCurated)
    }

    /**
     * Whether to offer reporting a platform.
     *
     * Reporting exists because an open catalog lets a stranger list anything, so it
     * needs a live service to post to and a list nobody vouched for. A document has
     * no service; a curated catalog vouches for its own platforms.
     */
    fun canReport(config: LMSDirectoryConfig, preferences: CorePreferences): Boolean =
        config.supportsReporting && !isCurated(config, preferences)

    /**
     * Drop a remembered answer that belongs to a directory this build no longer
     * reads. Safe to call on every launch; does nothing when the source is unchanged.
     */
    fun reconcile(config: LMSDirectoryConfig, preferences: CorePreferences) {
        val stored = preferences.lmsDirectorySourceKey
        if (stored.isEmpty() || stored == config.sourceKey) return
        clear(preferences)
    }
}
