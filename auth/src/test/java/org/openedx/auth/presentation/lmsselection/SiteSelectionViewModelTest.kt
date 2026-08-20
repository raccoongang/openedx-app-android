package org.openedx.auth.presentation.lmsselection

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.openedx.core.data.storage.CorePreferences
import org.openedx.core.lmsdirectory.LmsDetail
import org.openedx.core.lmsdirectory.LmsDirectoryRepository
import org.openedx.core.lmsdirectory.LmsSummary
import org.openedx.core.lmsdirectory.LmsThemeController
import org.openedx.foundation.system.ResourceManager

/**
 * The picker lists what the document holds, and choosing one platform makes the
 * app talk to it: its host, its OAuth client, its brand. Everything downstream
 * reads those, so what this writes is the whole point of the screen.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SiteSelectionViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val corePreferences = mockk<CorePreferences>(relaxed = true)
    private val resourceManager = mockk<ResourceManager>(relaxed = true)
    private val repository = mockk<LmsDirectoryRepository>(relaxed = true)

    private val summary = LmsSummary(
        id = "4",
        title = "Sandbox Env",
        shortDescription = "",
        baseUrl = "https://sandbox.openedx.org",
        logoUrl = null,
        accentColor = "#6a2e7b",
    )

    private fun detail(preLoginDiscovery: Boolean = false) = LmsDetail(
        id = "4",
        title = "Sandbox Env",
        shortDescription = "",
        baseUrl = "https://sandbox.openedx.org",
        logoUrl = null,
        accentColor = "#6a2e7b",
        oauthClientId = "client-id",
        feedbackEmail = null,
        loginBackgroundUrl = null,
        preLoginDiscovery = preLoginDiscovery,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        coEvery { repository.platforms() } returns Result.success(listOf(summary))
        coEvery { repository.providerName() } returns "Northwind"
        coEvery { repository.imageReferences() } returns emptyList()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        LmsThemeController.clear()
    }

    @Test
    fun `the directory is listed as the document orders it`() = runTest(dispatcher) {
        val viewModel = SiteSelectionViewModel(corePreferences, resourceManager, repository)
        advanceUntilIdle()

        assertEquals(CatalogState.Loaded, viewModel.uiState.value.catalog)
        assertEquals(listOf("4"), viewModel.uiState.value.platforms.map { it.id })
        assertEquals("Northwind", viewModel.uiState.value.providerName)
    }

    @Test
    fun `a document with no platforms says so rather than looking broken`() = runTest(dispatcher) {
        coEvery { repository.platforms() } returns Result.success(emptyList())
        val viewModel = SiteSelectionViewModel(corePreferences, resourceManager, repository)
        advanceUntilIdle()

        assertEquals(CatalogState.Empty, viewModel.uiState.value.catalog)
    }

    @Test
    fun `a document that cannot be read surfaces as an error`() = runTest(dispatcher) {
        coEvery { repository.platforms() } returns Result.failure(IllegalStateException("nope"))
        val viewModel = SiteSelectionViewModel(corePreferences, resourceManager, repository)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.catalog is CatalogState.Error)
    }

    @Test
    fun `choosing a platform makes the app talk to it`() = runTest(dispatcher) {
        val (viewModel, actions) = select(preLoginDiscovery = false)

        // The summary carries no OAuth client id, so the full record has to be read.
        coVerify { repository.detail("4") }
        verify { corePreferences.selectedBaseUrl = "https://sandbox.openedx.org/" }
        verify { corePreferences.selectedOAuthClientId = "client-id" }
        verify { corePreferences.selectedLmsTitle = "Sandbox Env" }
        assertEquals(1, actions.size)
        assertTrue(!(actions.first() as SiteSelectionViewModel.SiteSelectionAction.Success).preLoginDiscovery)
        assertEquals(CatalogState.Loaded, viewModel.uiState.value.catalog)
    }

    @Test
    fun `a platform that opens on discovery routes there instead of sign-in`() = runTest(dispatcher) {
        val (_, actions) = select(preLoginDiscovery = true)

        val success = actions.first() as SiteSelectionViewModel.SiteSelectionAction.Success
        assertTrue("Discovery platform must route to pre-login Discovery", success.preLoginDiscovery)
    }

    private fun kotlinx.coroutines.test.TestScope.select(
        preLoginDiscovery: Boolean,
    ): Pair<SiteSelectionViewModel, List<SiteSelectionViewModel.SiteSelectionAction>> {
        coEvery { repository.detail("4") } returns Result.success(detail(preLoginDiscovery))
        val viewModel = SiteSelectionViewModel(corePreferences, resourceManager, repository)
        val actions = mutableListOf<SiteSelectionViewModel.SiteSelectionAction>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.actions.toList(actions)
        }
        advanceUntilIdle()

        viewModel.onPlatformSelected(summary)
        advanceUntilIdle()
        return viewModel to actions
    }
}
