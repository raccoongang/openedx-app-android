package org.openedx.core.lmsdirectory

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.openedx.core.config.LMSDirectoryConfig
import org.openedx.core.data.storage.CorePreferences

/** What kind of list a directory is. */
enum class LmsDirectoryMode {
    /** Nobody has said yet. A live service that has not answered this launch. */
    UNKNOWN,

    /** An open catalog: anyone may list a platform, so nothing is vouched for. */
    SEARCH,

    /** A fixed list published by one organisation. */
    CURATED
}

/**
 * What the app knows about the directory it reads, and how long that knowledge is
 * good for.
 *
 * Whether a live catalog is open to anyone or is one organisation's own list is
 * something only the server can say, and it says so on a screen the app stops
 * showing once a platform has been picked. So the answer is remembered — and a
 * remembered answer is worth nothing after the build is pointed somewhere else,
 * or after the server changes its mind. Hence three states rather than a flag:
 * until the current source has actually answered, the mode is [UNKNOWN], and
 * anything that depends on it stays hidden.
 *
 * A single object, like [LmsThemeController]: the answer is a property of the
 * install, and every screen has to see the same one.
 */
object LmsDirectoryState {

    private val _revision = MutableStateFlow(0L)

    /** Emits whenever the stored answer changes, so a screen can redraw. */
    val revision: StateFlow<Long> = _revision.asStateFlow()

    /**
     * What this build's directory is, as far as anyone currently knows.
     *
     * A document is a fixed list by construction. DIRECTORY_MODE settles it locally
     * either way. Only a live service with no override has to ask, and until it has
     * answered *for this exact source* the honest answer is [LmsDirectoryMode.UNKNOWN].
     */
    fun mode(config: LMSDirectoryConfig, preferences: CorePreferences): LmsDirectoryMode {
        val configured = config.configuredMode
        val remembered = if (preferences.lmsDirectorySourceKey == config.sourceKey) {
            runCatching { LmsDirectoryMode.valueOf(preferences.lmsDirectoryMode) }
                .getOrDefault(LmsDirectoryMode.UNKNOWN)
        } else {
            LmsDirectoryMode.UNKNOWN
        }
        return configured ?: remembered
    }

    /**
     * Whether to offer reporting a platform.
     *
     * Reporting exists because an open catalog lets a stranger list anything, so it
     * needs both a live service to post to and a list nobody vouched for. A document
     * has no service; a curated catalog vouches for its own platforms; and an
     * unanswered service is not yet known to be either, so it shows nothing rather
     * than guessing.
     */
    fun canReport(config: LMSDirectoryConfig, preferences: CorePreferences): Boolean =
        config.supportsReporting && mode(config, preferences) == LmsDirectoryMode.SEARCH

    /** Record what a source said, against the source that said it. */
    fun remember(
        mode: LmsDirectoryMode,
        config: LMSDirectoryConfig,
        preferences: CorePreferences
    ) {
        if (mode == LmsDirectoryMode.UNKNOWN) return
        val changed = preferences.lmsDirectoryMode != mode.name ||
            preferences.lmsDirectorySourceKey != config.sourceKey
        preferences.lmsDirectoryMode = mode.name
        preferences.lmsDirectorySourceKey = config.sourceKey
        if (changed) _revision.value++
    }

    /** Forget everything source-specific. Used when the feature is switched off. */
    fun clear(preferences: CorePreferences) {
        preferences.lmsDirectoryMode = ""
        preferences.lmsDirectorySourceKey = ""
        _revision.value++
    }

    /**
     * Drop knowledge that belongs to a directory this build no longer reads.
     * Safe on every launch; does nothing when the source is unchanged.
     */
    fun reconcile(config: LMSDirectoryConfig, preferences: CorePreferences) {
        val stored = preferences.lmsDirectorySourceKey
        if (stored.isEmpty() || stored == config.sourceKey) return
        clear(preferences)
    }

    /**
     * Ask the directory what it is, now, rather than trusting what it said last time.
     *
     * Called at launch so a service that has changed its mode is noticed even by a
     * build that never shows the platform picker again. A failure leaves what is
     * already known untouched, because a network error is not evidence of anything.
     */
    suspend fun refresh(
        config: LMSDirectoryConfig,
        preferences: CorePreferences,
        ask: suspend () -> LmsDirectoryMode
    ) {
        if (config.configuredMode != null || !config.supportsReporting) return
        val answered = runCatching { ask() }.getOrNull() ?: return
        if (answered != LmsDirectoryMode.UNKNOWN) remember(answered, config, preferences)
    }
}
