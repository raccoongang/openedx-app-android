package org.openedx.core.lmsdirectory

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.openedx.core.config.LMSDirectoryConfig
import org.openedx.core.data.model.User
import org.openedx.core.data.storage.CorePreferences
import org.openedx.core.domain.model.AppConfig
import org.openedx.core.domain.model.VideoSettings

/**
 * The persisted "curated" answer used to be believed forever. These cover the
 * transitions where that was wrong: the build gets pointed somewhere else, the
 * picker is skipped because a platform is already selected, and the stale value
 * decides whether reporting appears.
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

    @Test
    fun `an open catalog reports until the server says it is curated`() {
        val prefs = FakePreferences()

        assertTrue(LmsDirectoryState.canReport(openCatalog, prefs))

        LmsDirectoryState.rememberCurated(true, openCatalog, prefs)
        assertTrue(LmsDirectoryState.isCurated(openCatalog, prefs))
        assertFalse(LmsDirectoryState.canReport(openCatalog, prefs))
    }

    @Test
    fun `a curated answer does not follow the build to another service`() {
        // The bug this exists for: the picker is skipped once a platform is
        // selected, so nothing would ever overwrite the old answer.
        val prefs = FakePreferences()
        LmsDirectoryState.rememberCurated(true, openCatalog, prefs)

        assertFalse(LmsDirectoryState.isCurated(otherCatalog, prefs))
        assertTrue(LmsDirectoryState.canReport(otherCatalog, prefs))
    }

    @Test
    fun `moving from a document to an open catalog restores reporting`() {
        val prefs = FakePreferences()
        LmsDirectoryState.rememberCurated(true, document, prefs)

        assertTrue(LmsDirectoryState.canReport(openCatalog, prefs))
    }

    @Test
    fun `moving from an open catalog to a document hides reporting`() {
        val prefs = FakePreferences()
        LmsDirectoryState.rememberCurated(false, openCatalog, prefs)

        assertTrue(LmsDirectoryState.isCurated(document, prefs))
        assertFalse(LmsDirectoryState.canReport(document, prefs))
        assertTrue(LmsDirectoryState.isCurated(bundled, prefs))
        assertFalse(LmsDirectoryState.canReport(bundled, prefs))
    }

    @Test
    fun `a remembered open answer cannot unlock reporting on a curated build`() {
        val prefs = FakePreferences()
        val forcedCurated = LMSDirectoryConfig(
            enabled = true,
            directoryUrl = "https://registry.example.com",
            directoryMode = "curated"
        )
        LmsDirectoryState.rememberCurated(false, forcedCurated, prefs)

        assertTrue(LmsDirectoryState.isCurated(forcedCurated, prefs))
        assertFalse(LmsDirectoryState.canReport(forcedCurated, prefs))
    }

    @Test
    fun `a value stored by an older build without a source key is not trusted`() {
        // Upgrades carry the flag but no key, and the flag alone is what went stale.
        val prefs = FakePreferences().apply { lmsDirectoryCurated = true }

        assertFalse(LmsDirectoryState.isCurated(openCatalog, prefs))
        assertTrue(LmsDirectoryState.canReport(openCatalog, prefs))
    }

    @Test
    fun `reconcile drops a value belonging to a different source`() {
        val prefs = FakePreferences()
        LmsDirectoryState.rememberCurated(true, openCatalog, prefs)

        LmsDirectoryState.reconcile(otherCatalog, prefs)

        assertFalse(prefs.lmsDirectoryCurated)
        assertTrue(prefs.lmsDirectorySourceKey.isEmpty())
    }

    @Test
    fun `reconcile keeps a value belonging to the same source`() {
        val prefs = FakePreferences()
        LmsDirectoryState.rememberCurated(true, openCatalog, prefs)

        LmsDirectoryState.reconcile(openCatalog, prefs)

        assertTrue(prefs.lmsDirectoryCurated)
        assertTrue(LmsDirectoryState.isCurated(openCatalog, prefs))
    }

    @Test
    fun `clear leaves nothing to be believed`() {
        val prefs = FakePreferences()
        LmsDirectoryState.rememberCurated(true, openCatalog, prefs)

        LmsDirectoryState.clear(prefs)

        assertFalse(LmsDirectoryState.isCurated(openCatalog, prefs))
        assertTrue(LmsDirectoryState.canReport(openCatalog, prefs))
    }

    @Test
    fun `a disabled feature reports nothing at all`() {
        val prefs = FakePreferences()
        LmsDirectoryState.rememberCurated(false, openCatalog, prefs)

        val off = LMSDirectoryConfig(enabled = false, directoryUrl = "https://registry.example.com")
        assertFalse(LmsDirectoryState.canReport(off, prefs))
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
        override var lmsDirectoryCurated: Boolean = false
        override var lmsDirectorySourceKey: String = ""
        override var lmsHistory: List<LmsHistoryEntry> = emptyList()

        override suspend fun clearCorePreferences() = Unit
    }
}
