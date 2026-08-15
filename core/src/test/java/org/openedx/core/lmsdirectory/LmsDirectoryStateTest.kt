package org.openedx.core.lmsdirectory

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.openedx.core.config.LMSDirectoryConfig
import org.openedx.core.data.model.User
import org.openedx.core.data.storage.CorePreferences
import org.openedx.core.domain.model.AppConfig
import org.openedx.core.domain.model.VideoSettings

/**
 * Whether a build offers to report a platform depends on something only a live
 * server can say, remembered between launches. These cover the ways a remembered
 * answer stops being true: the build is pointed elsewhere, the server changes its
 * mind at the same address, or the value was written by a version that recorded
 * no source at all.
 */
class LmsDirectoryStateTest {

    private val openCatalog = LMSDirectoryConfig(
        enabled = true,
        directoryUrl = "https://registry.example.com"
    )
    private val otherCatalog = LMSDirectoryConfig(
        enabled = true,
        directoryUrl = "https://other-registry.example.com"
    )
    private val document = LMSDirectoryConfig(
        enabled = true,
        directoryUrl = "https://cdn.example.com/directory.json"
    )
    private val bundled = LMSDirectoryConfig(
        enabled = true,
        directoryFile = "lms_directory.json"
    )

    private lateinit var prefs: FakePreferences

    @Before
    fun setUp() {
        prefs = FakePreferences()
        LmsDirectoryState.clear(prefs)
    }

    // What the configuration alone decides

    @Test
    fun `a document is curated without asking anyone`() {
        assertEquals(LmsDirectoryMode.CURATED, LmsDirectoryState.mode(document, prefs))
        assertEquals(LmsDirectoryMode.CURATED, LmsDirectoryState.mode(bundled, prefs))
        assertFalse(LmsDirectoryState.canReport(document, prefs))
        assertFalse(LmsDirectoryState.canReport(bundled, prefs))
    }

    @Test
    fun `a service is unknown until it answers`() {
        // The point of three states: an unanswered service is not "open", it is
        // unknown, and nothing that depends on the answer may appear yet.
        assertEquals(LmsDirectoryMode.UNKNOWN, LmsDirectoryState.mode(openCatalog, prefs))
        assertFalse(LmsDirectoryState.canReport(openCatalog, prefs))
    }

    @Test
    fun `a forced mode is honoured without the server`() {
        val curated = openCatalog.copy(directoryMode = "curated")
        val search = openCatalog.copy(directoryMode = "search")

        assertEquals(LmsDirectoryMode.CURATED, LmsDirectoryState.mode(curated, prefs))
        assertFalse(LmsDirectoryState.canReport(curated, prefs))
        assertEquals(LmsDirectoryMode.SEARCH, LmsDirectoryState.mode(search, prefs))
        assertTrue(LmsDirectoryState.canReport(search, prefs))
    }

    @Test
    fun `a disabled directory offers nothing`() {
        LmsDirectoryState.remember(LmsDirectoryMode.SEARCH, openCatalog, prefs)

        assertFalse(LmsDirectoryState.canReport(LMSDirectoryConfig(enabled = false), prefs))
    }

    // What a remembered answer is good for

    @Test
    fun `an answered service offers reporting`() {
        LmsDirectoryState.remember(LmsDirectoryMode.SEARCH, openCatalog, prefs)

        assertEquals(LmsDirectoryMode.SEARCH, LmsDirectoryState.mode(openCatalog, prefs))
        assertTrue(LmsDirectoryState.canReport(openCatalog, prefs))
    }

    @Test
    fun `an answer does not follow the build to another service`() {
        // The picker is skipped once a platform is selected, so nothing would ever
        // overwrite an answer left by a different registry.
        LmsDirectoryState.remember(LmsDirectoryMode.CURATED, openCatalog, prefs)

        assertEquals(LmsDirectoryMode.UNKNOWN, LmsDirectoryState.mode(otherCatalog, prefs))
        assertFalse(LmsDirectoryState.canReport(otherCatalog, prefs))
    }

    @Test
    fun `moving from a document to a service leaves the mode unknown`() {
        LmsDirectoryState.remember(LmsDirectoryMode.CURATED, document, prefs)

        assertEquals(LmsDirectoryMode.UNKNOWN, LmsDirectoryState.mode(openCatalog, prefs))
        assertFalse(LmsDirectoryState.canReport(openCatalog, prefs))
    }

    @Test
    fun `moving from a service to a document hides reporting`() {
        LmsDirectoryState.remember(LmsDirectoryMode.SEARCH, openCatalog, prefs)

        assertEquals(LmsDirectoryMode.CURATED, LmsDirectoryState.mode(document, prefs))
        assertFalse(LmsDirectoryState.canReport(document, prefs))
    }

    // The same address changing its mind

    @Test
    fun `the same service can change its mode`() {
        LmsDirectoryState.remember(LmsDirectoryMode.CURATED, openCatalog, prefs)
        assertFalse(LmsDirectoryState.canReport(openCatalog, prefs))

        LmsDirectoryState.remember(LmsDirectoryMode.SEARCH, openCatalog, prefs)

        assertEquals(LmsDirectoryMode.SEARCH, LmsDirectoryState.mode(openCatalog, prefs))
        assertTrue(LmsDirectoryState.canReport(openCatalog, prefs))
    }

    @Test
    fun `a refresh records what the server now says`() = runTest {
        LmsDirectoryState.remember(LmsDirectoryMode.CURATED, openCatalog, prefs)

        LmsDirectoryState.refresh(openCatalog, prefs) { LmsDirectoryMode.SEARCH }

        assertEquals(LmsDirectoryMode.SEARCH, LmsDirectoryState.mode(openCatalog, prefs))
        assertTrue(LmsDirectoryState.canReport(openCatalog, prefs))
    }

    @Test
    fun `an unreachable registry changes nothing`() = runTest {
        LmsDirectoryState.remember(LmsDirectoryMode.SEARCH, openCatalog, prefs)

        LmsDirectoryState.refresh(openCatalog, prefs) { LmsDirectoryMode.UNKNOWN }
        LmsDirectoryState.refresh(openCatalog, prefs) { error("offline") }

        assertEquals(LmsDirectoryMode.SEARCH, LmsDirectoryState.mode(openCatalog, prefs))
    }

    @Test
    fun `a refresh does not override a configured mode`() = runTest {
        // DIRECTORY_MODE is the operator's decision; the server does not get a vote.
        val curated = openCatalog.copy(directoryMode = "curated")

        LmsDirectoryState.refresh(curated, prefs) { LmsDirectoryMode.SEARCH }

        assertEquals(LmsDirectoryMode.CURATED, LmsDirectoryState.mode(curated, prefs))
    }

    @Test
    fun `a document is never refreshed`() = runTest {
        var asked = false

        LmsDirectoryState.refresh(document, prefs) {
            asked = true
            LmsDirectoryMode.SEARCH
        }

        assertFalse("a document has no server to ask", asked)
    }

    // Upgrades from a build that stored only a boolean

    @Test
    fun `a value from an older build is not trusted`() {
        // Upgrades carry a flag that names no source. Both ways round it must mean
        // "not known yet", which hides reporting rather than revealing it.
        prefs.lmsDirectoryMode = "true"
        prefs.lmsDirectorySourceKey = ""

        assertEquals(LmsDirectoryMode.UNKNOWN, LmsDirectoryState.mode(openCatalog, prefs))
        assertFalse(LmsDirectoryState.canReport(openCatalog, prefs))
    }

    @Test
    fun `an upgraded install starts reporting only once the registry answers`() {
        prefs.lmsDirectoryMode = "false"
        prefs.lmsDirectorySourceKey = ""
        assertFalse(LmsDirectoryState.canReport(openCatalog, prefs))

        LmsDirectoryState.remember(LmsDirectoryMode.SEARCH, openCatalog, prefs)

        assertTrue(LmsDirectoryState.canReport(openCatalog, prefs))
    }

    // Housekeeping

    @Test
    fun `reconcile drops a value belonging to a different source`() {
        LmsDirectoryState.remember(LmsDirectoryMode.SEARCH, openCatalog, prefs)

        LmsDirectoryState.reconcile(otherCatalog, prefs)

        assertEquals(LmsDirectoryMode.UNKNOWN, LmsDirectoryState.mode(otherCatalog, prefs))
    }

    @Test
    fun `reconcile leaves a matching value alone`() {
        LmsDirectoryState.remember(LmsDirectoryMode.SEARCH, openCatalog, prefs)

        LmsDirectoryState.reconcile(openCatalog, prefs)

        assertEquals(LmsDirectoryMode.SEARCH, LmsDirectoryState.mode(openCatalog, prefs))
    }

    @Test
    fun `clear leaves nothing to be believed`() {
        LmsDirectoryState.remember(LmsDirectoryMode.SEARCH, openCatalog, prefs)

        LmsDirectoryState.clear(prefs)

        assertEquals(LmsDirectoryMode.UNKNOWN, LmsDirectoryState.mode(openCatalog, prefs))
        assertFalse(LmsDirectoryState.canReport(openCatalog, prefs))
    }

    @Test
    fun `every source has its own key`() {
        assertTrue(openCatalog.sourceKey != document.sourceKey)
        assertTrue(document.sourceKey != bundled.sourceKey)
        assertTrue(openCatalog.sourceKey != otherCatalog.sourceKey)
    }

    @Test
    fun `a changed answer is observable`() {
        // The Profile screen redraws off this; without it the entry point would
        // only appear on the next visit.
        val before = LmsDirectoryState.revision.value

        LmsDirectoryState.remember(LmsDirectoryMode.SEARCH, openCatalog, prefs)
        LmsDirectoryState.remember(LmsDirectoryMode.CURATED, openCatalog, prefs)

        assertEquals(before + 2, LmsDirectoryState.revision.value)
    }

    /** In-memory preferences: only the two directory fields matter here. */
    private class FakePreferences : CorePreferences {
        override var accessToken: String = ""
        override var refreshToken: String = ""
        override var pushToken: String = ""
        override var accessTokenExpiresAt: Long = 0
        override var user: User? = null
        override var videoSettings: VideoSettings = VideoSettings.default
        override var appConfig: AppConfig = AppConfig()
        override var canResetAppDirectory: Boolean = false
        override var isRelativeDatesEnabled: Boolean = true
        override var selectedBaseUrl: String? = null
        override var selectedLmsAccentColor: String? = null
        override var selectedOAuthClientId: String? = null
        override var selectedFeedbackEmail: String? = null
        override var selectedLmsLogoUrl: String? = null
        override var selectedLmsLoginBackgroundUrl: String? = null
        override var selectedLmsTitle: String? = null
        override var lmsDirectoryMode: String = ""
        override var lmsDirectorySourceKey: String = ""
        override var lmsHistory: List<LmsHistoryEntry> = emptyList()

        override suspend fun clearCorePreferences() = Unit
    }
}
