package org.openedx.profile.presentation.profile

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.compose.ui.text.intl.Locale
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.openedx.core.config.Config
import org.openedx.core.config.LMSDirectoryConfig
import org.openedx.core.data.storage.CorePreferences
import org.openedx.core.domain.model.AgreementUrls
import org.openedx.foundation.presentation.UIMessage
import org.openedx.foundation.presentation.captureUiMessage
import org.openedx.foundation.system.ResourceManager
import org.openedx.profile.ProfileMocks
import org.openedx.profile.domain.interactor.ProfileInteractor
import org.openedx.profile.presentation.ProfileAnalytics
import org.openedx.profile.presentation.ProfileRouter
import org.openedx.profile.system.notifier.account.AccountUpdated
import org.openedx.profile.system.notifier.profile.ProfileNotifier
import java.net.UnknownHostException
import org.openedx.foundation.R as foundationR

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @get:Rule
    val testInstantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    private val dispatcher = StandardTestDispatcher()

    private val config = mockk<Config>()
    private val corePreferences = mockk<CorePreferences>(relaxed = true)
    private val resourceManager = mockk<ResourceManager>()
    private val interactor = mockk<ProfileInteractor>()
    private val notifier = mockk<ProfileNotifier>()
    private val analytics = mockk<ProfileAnalytics>()
    private val router = mockk<ProfileRouter>()

    private val noInternet = "Slow or no internet connection"
    private val somethingWrong = "Something went wrong"

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every {
            resourceManager.getString(foundationR.string.foundation_error_no_connection)
        } returns noInternet
        every {
            resourceManager.getString(foundationR.string.foundation_error_unknown_error)
        } returns somethingWrong
        every { config.isPreLoginExperienceEnabled() } returns false
        every { config.getFeedbackEmailAddress() } returns ""
        every { config.getAgreement(Locale.current.language) } returns AgreementUrls()
        every { config.getFaqUrl() } returns ""
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getAccount no internetConnection and cache is null`() = runTest {
        val viewModel = ProfileViewModel(
            interactor,
            resourceManager,
            notifier,
            analytics,
            config,
            corePreferences,
            router
        )
        coEvery { interactor.getCachedAccount() } returns null
        coEvery { interactor.getAccount() } throws UnknownHostException()
        advanceUntilIdle()

        coVerify(exactly = 1) { interactor.getAccount() }

        val message = captureUiMessage(viewModel)
        assert(viewModel.uiState.value is ProfileUIState.Loading)
        assertEquals(noInternet, (message.await() as? UIMessage.SnackBarMessage)?.message)
    }

    @Test
    fun `getAccount no internetConnection and cache is not null`() = runTest {
        val viewModel = ProfileViewModel(
            interactor,
            resourceManager,
            notifier,
            analytics,
            config,
            corePreferences,
            router
        )
        coEvery { interactor.getCachedAccount() } returns ProfileMocks.account.copy(
            accountPrivacy = org.openedx.profile.domain.model.Account.Privacy.PRIVATE
        )
        coEvery { interactor.getAccount() } throws UnknownHostException()
        advanceUntilIdle()

        coVerify(exactly = 1) { interactor.getAccount() }

        val message = captureUiMessage(viewModel)
        assert(viewModel.uiState.value is ProfileUIState.Data)
        assertEquals(noInternet, (message.await() as? UIMessage.SnackBarMessage)?.message)
    }

    @Test
    fun `getAccount unknown exception`() = runTest {
        val viewModel = ProfileViewModel(
            interactor,
            resourceManager,
            notifier,
            analytics,
            config,
            corePreferences,
            router
        )
        coEvery { interactor.getCachedAccount() } returns null
        coEvery { interactor.getAccount() } throws Exception()
        advanceUntilIdle()

        coVerify(exactly = 1) { interactor.getAccount() }

        val message = captureUiMessage(viewModel)
        assert(viewModel.uiState.value is ProfileUIState.Loading)
        assertEquals(somethingWrong, (message.await() as? UIMessage.SnackBarMessage)?.message)
    }

    @Test
    fun `getAccount success`() = runTest {
        val viewModel = ProfileViewModel(
            interactor,
            resourceManager,
            notifier,
            analytics,
            config,
            corePreferences,
            router
        )
        coEvery { interactor.getCachedAccount() } returns null
        coEvery { interactor.getAccount() } returns ProfileMocks.account.copy(
            accountPrivacy = org.openedx.profile.domain.model.Account.Privacy.PRIVATE
        )
        advanceUntilIdle()

        coVerify(exactly = 1) { interactor.getAccount() }

        assert(viewModel.uiState.value is ProfileUIState.Data)
        val message = captureUiMessage(viewModel)
        assert(message.await() == null)
    }

    @Test
    fun `AccountUpdated notifier test`() = runTest {
        val viewModel = ProfileViewModel(
            interactor,
            resourceManager,
            notifier,
            analytics,
            config,
            corePreferences,
            router
        )
        coEvery { interactor.getCachedAccount() } returns null
        every { notifier.notifier } returns flow { emit(AccountUpdated()) }
        val mockLifeCycleOwner: LifecycleOwner = mockk()
        val lifecycleRegistry = LifecycleRegistry(mockLifeCycleOwner)
        lifecycleRegistry.addObserver(viewModel)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)

        advanceUntilIdle()

        coVerify(exactly = 2) { interactor.getAccount() }
    }

    /**
     * What the Profile tab actually asks before drawing "Report this LMS". The
     * flag it used to read was written by the platform picker and never cleared,
     * so these pin the answer to the configured source instead.
     */
    private fun reportingOffered(
        directory: LMSDirectoryConfig,
        remembered: Boolean = false,
        rememberedFor: String = ""
    ): Boolean {
        every { config.getLMSDirectoryConfig() } returns directory
        every { corePreferences.lmsDirectoryCurated } returns remembered
        every { corePreferences.lmsDirectorySourceKey } returns rememberedFor
        coEvery { interactor.getCachedAccount() } returns null
        return ProfileViewModel(
            interactor,
            resourceManager,
            notifier,
            analytics,
            config,
            corePreferences,
            router
        ).canReportLms
    }

    @Test
    fun `an open catalog offers reporting`() {
        val service = LMSDirectoryConfig(enabled = true, directoryUrl = "https://registry.example.com")
        assertEquals(true, reportingOffered(service))
    }

    @Test
    fun `a document build never offers reporting`() {
        assertEquals(
            false,
            reportingOffered(
                LMSDirectoryConfig(enabled = true, directoryUrl = "https://cdn.example.com/directory.json")
            )
        )
        assertEquals(
            false,
            reportingOffered(LMSDirectoryConfig(enabled = true, directoryFile = "lms_directory.json"))
        )
    }

    @Test
    fun `a curated catalog does not offer reporting`() {
        val service = LMSDirectoryConfig(enabled = true, directoryUrl = "https://registry.example.com")
        assertEquals(
            false,
            reportingOffered(service, remembered = true, rememberedFor = service.sourceKey)
        )
    }

    @Test
    fun `a curated answer left by a different directory is ignored`() {
        // The regression: the picker is skipped once a platform is selected, so a
        // build repointed at an open catalog kept hiding the entry point.
        val service = LMSDirectoryConfig(enabled = true, directoryUrl = "https://registry.example.com")
        assertEquals(
            true,
            reportingOffered(service, remembered = true, rememberedFor = "service:https://old-registry.example.com")
        )
    }

    @Test
    fun `a disabled directory offers nothing`() {
        assertEquals(false, reportingOffered(LMSDirectoryConfig(enabled = false)))
    }
}
