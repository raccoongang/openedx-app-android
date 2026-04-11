package org.openedx.course.presentation.outline

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.openedx.core.CoreMocks
import org.openedx.core.config.Config
import org.openedx.core.data.storage.CorePreferences
import org.openedx.core.domain.model.CourseComponentStatus
import org.openedx.core.module.DownloadWorkerController
import org.openedx.core.module.download.DownloadModelsSource
import org.openedx.core.module.download.DownloadHelper
import org.openedx.core.presentation.CoreAnalytics
import org.openedx.core.presentation.CoreAnalyticsEvent
import org.openedx.core.presentation.dialog.downloaddialog.DownloadDialogManager
import org.openedx.core.system.connection.NetworkConnection
import org.openedx.core.system.notifier.CourseNotifier
import org.openedx.core.system.notifier.CourseStructureUpdated
import org.openedx.course.Res as courseRes
import org.openedx.course.course_can_download_only_with_wifi
import org.openedx.course.domain.interactor.CourseInteractor
import org.openedx.course.presentation.CourseAnalytics
import org.openedx.foundation.presentation.UIMessage
import org.openedx.foundation.system.ResourceManager
import org.openedx.foundation.utils.FileUtil
import java.net.UnknownHostException
import org.openedx.foundation.Res as foundationRes
import org.openedx.foundation.foundation_error_no_connection
import org.openedx.foundation.foundation_error_unknown_error

@OptIn(ExperimentalCoroutinesApi::class)
class CourseOutlineViewModelTest {

    @get:Rule
    val testInstantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    private val dispatcher = UnconfinedTestDispatcher()

    private val config = mockk<Config>()
    private val resourceManager = mockk<ResourceManager>()
    private val interactor = mockk<CourseInteractor>()
    private val preferencesManager = mockk<CorePreferences>()
    private val networkConnection = mockk<NetworkConnection>()
    private val notifier = spyk<CourseNotifier>()
    private val downloadModelsSource = mockk<DownloadModelsSource>()
    private val workerController = mockk<DownloadWorkerController>()
    private val analytics = mockk<CourseAnalytics>()
    private val coreAnalytics = mockk<CoreAnalytics>()
    private val fileUtil = mockk<FileUtil>()
    private val downloadDialogManager = mockk<DownloadDialogManager>()
    private val downloadHelper = mockk<DownloadHelper>()

    private val noInternet = "Slow or no internet connection"
    private val somethingWrong = "Something went wrong"
    private val cantDownload = "You can download content only from Wi-fi"

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every {
            resourceManager.getString(foundationRes.string.foundation_error_no_connection)
        } returns noInternet
        every {
            resourceManager.getString(foundationRes.string.foundation_error_unknown_error)
        } returns somethingWrong
        every {
            resourceManager.getString(courseRes.string.course_can_download_only_with_wifi)
        } returns cantDownload
        every { config.getApiHostURL() } returns "http://localhost:8000"
        every { downloadDialogManager.showDownloadFailedPopup(any(), any()) } returns Unit
        every { preferencesManager.isRelativeDatesEnabled } returns true

        coEvery { interactor.getCourseDates(any(), any()) } returns CoreMocks.mockCourseDatesResult
        coEvery {
            interactor.getCourseDatesFlow(
                any(),
                any()
            )
        } returns flowOf(CoreMocks.mockCourseDatesResult)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getCourseDataInternal no internet connection exception`() =
        runTest(UnconfinedTestDispatcher()) {
            coEvery { interactor.getCourseStructureFlow(any(), any()) } returns flowOf(
                CoreMocks.mockCourseStructure
            )
            every { networkConnection.isOnline() } returns true
            every { downloadModelsSource.getDownloadModelsFlow() } returns flow { emit(emptyList()) }
            every {
                downloadDialogManager.showPopup(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                )
            } returns Unit
            coEvery {
                interactor.getCourseStatusFlow(
                    any(),
                    any()
                )
            } returns flow { throw UnknownHostException() }

            val viewModel = CourseContentAllViewModel(
                "",
                "",
                config,
                interactor,
                resourceManager,
                notifier,
                networkConnection,
                preferencesManager,
                analytics,
                downloadDialogManager,
                fileUtil,
                coreAnalytics,
                downloadModelsSource,
                workerController,
                downloadHelper,
            )

            val message = async {
                viewModel.uiMessage.first() as? UIMessage.SnackBarMessage
            }
            viewModel.getCourseData()
            advanceUntilIdle()

            coVerify(exactly = 2) { interactor.getCourseStructureFlow(any(), any()) }
            coVerify(exactly = 2) { interactor.getCourseStatusFlow(any(), any()) }

            assertEquals(noInternet, message.await()?.message)
            assert(viewModel.uiState.value is CourseContentAllUIState.Error)
        }

    @Suppress("TooGenericExceptionThrown")
    @Test
    fun `getCourseDataInternal unknown exception`() = runTest(UnconfinedTestDispatcher()) {
        coEvery { interactor.getCourseStructureFlow(any(), any()) } returns flowOf(
            CoreMocks.mockCourseStructure
        )
        every { networkConnection.isOnline() } returns true
        every { downloadModelsSource.getDownloadModelsFlow() } returns flow { emit(emptyList()) }
        coEvery { interactor.getCourseStatusFlow(any(), any()) } returns flow { throw Exception() }
        val viewModel = CourseContentAllViewModel(
            "",
            "",
            config,
            interactor,
            resourceManager,
            notifier,
            networkConnection,
            preferencesManager,
            analytics,
            downloadDialogManager,
            fileUtil,
            coreAnalytics,
            downloadModelsSource,
            workerController,
            downloadHelper,
        )

        val message = async {
            viewModel.uiMessage.first() as? UIMessage.SnackBarMessage
        }
        viewModel.getCourseData()
        advanceUntilIdle()

        coVerify(exactly = 2) { interactor.getCourseStructureFlow(any(), any()) }
        coVerify(exactly = 2) { interactor.getCourseStatusFlow(any(), any()) }

        assertEquals(somethingWrong, message.await()?.message)
        assert(viewModel.uiState.value is CourseContentAllUIState.Error)
    }

    @Test
    fun `getCourseDataInternal success with internet connection`() =
        runTest(UnconfinedTestDispatcher()) {
            coEvery { interactor.getCourseStructureFlow(any(), any()) } returns flowOf(
                CoreMocks.mockCourseStructure
            )
            every { networkConnection.isOnline() } returns true
            coEvery { downloadModelsSource.getDownloadModelsFlow() } returns flow {
                emit(
                    listOf(
                        (
                            CoreMocks.mockDownloadModel
                        )
                    )
                )
            }
            coEvery { interactor.getCourseStatusFlow(any(), any()) } returns flowOf(
                CourseComponentStatus("id")
            )
            every { config.getCourseUIConfig().isCourseDropdownNavigationEnabled } returns false

            val viewModel = CourseContentAllViewModel(
                "",
                "",
                config,
                interactor,
                resourceManager,
                notifier,
                networkConnection,
                preferencesManager,
                analytics,
                downloadDialogManager,
                fileUtil,
                coreAnalytics,
                downloadModelsSource,
                workerController,
                downloadHelper,
            )

            val message = async {
                withTimeoutOrNull(5000) {
                    viewModel.uiMessage.first() as? UIMessage.SnackBarMessage
                }
            }

            viewModel.getCourseData()
            advanceUntilIdle()

            coVerify(exactly = 2) { interactor.getCourseStructureFlow(any(), any()) }
            coVerify(exactly = 2) { interactor.getCourseStatusFlow(any(), any()) }

            assert(message.await() == null)
            assert(viewModel.uiState.value is CourseContentAllUIState.CourseData)
        }

    @Test
    fun `getCourseDataInternal success without internet connection`() =
        runTest(UnconfinedTestDispatcher()) {
            coEvery { interactor.getCourseStructureFlow(any(), any()) } returns flowOf(
                CoreMocks.mockCourseStructure
            )
            every { networkConnection.isOnline() } returns false
            coEvery { downloadModelsSource.getDownloadModelsFlow() } returns flow {
                emit(
                    listOf(
                        (
                            CoreMocks.mockDownloadModel
                        )
                    )
                )
            }
            coEvery { interactor.getCourseStatusFlow(any(), any()) } returns flowOf(
                CourseComponentStatus("id")
            )
            every { config.getCourseUIConfig().isCourseDropdownNavigationEnabled } returns false

            val viewModel = CourseContentAllViewModel(
                "",
                "",
                config,
                interactor,
                resourceManager,
                notifier,
                networkConnection,
                preferencesManager,
                analytics,
                downloadDialogManager,
                fileUtil,
                coreAnalytics,
                downloadModelsSource,
                workerController,
                downloadHelper,
            )

            val message = async {
                withTimeoutOrNull(5000) {
                    viewModel.uiMessage.first() as? UIMessage.SnackBarMessage
                }
            }
            viewModel.getCourseData()
            advanceUntilIdle()

            coVerify(exactly = 2) { interactor.getCourseStructureFlow(any(), any()) }
            coVerify(exactly = 2) { interactor.getCourseStatusFlow(any(), any()) }

            assert(message.await() == null)
            assert(viewModel.uiState.value is CourseContentAllUIState.CourseData)
        }

    @Test
    fun `updateCourseData success with internet connection`() =
        runTest(UnconfinedTestDispatcher()) {
            coEvery { interactor.getCourseStructureFlow(any(), any()) } returns flowOf(
                CoreMocks.mockCourseStructure
            )
            every { networkConnection.isOnline() } returns true
            coEvery { downloadModelsSource.getDownloadModelsFlow() } returns flow {
                emit(
                    listOf(
                        (
                            CoreMocks.mockDownloadModel
                        )
                    )
                )
            }
            coEvery { interactor.getCourseStatusFlow(any(), any()) } returns flowOf(
                CourseComponentStatus("id")
            )
            every { config.getCourseUIConfig().isCourseDropdownNavigationEnabled } returns false

            val viewModel = CourseContentAllViewModel(
                "",
                "",
                config,
                interactor,
                resourceManager,
                notifier,
                networkConnection,
                preferencesManager,
                analytics,
                downloadDialogManager,
                fileUtil,
                coreAnalytics,
                downloadModelsSource,
                workerController,
                downloadHelper,
            )

            val message = async {
                withTimeoutOrNull(5000) {
                    viewModel.uiMessage.first() as? UIMessage.SnackBarMessage
                }
            }
            viewModel.getCourseData()
            advanceUntilIdle()

            coVerify(exactly = 2) { interactor.getCourseStructureFlow(any(), any()) }
            coVerify(exactly = 2) { interactor.getCourseStatusFlow(any(), any()) }

            assert(message.await() == null)
            assert(viewModel.uiState.value is CourseContentAllUIState.CourseData)
        }

    @Test
    fun `CourseStructureUpdated notifier test`() = runTest(UnconfinedTestDispatcher()) {
        coEvery { downloadModelsSource.getDownloadModelsFlow() } returns flow { emit(emptyList()) }
        coEvery { interactor.getCourseStructureFlow(any(), any()) } returns flowOf(
            CoreMocks.mockCourseStructure
        )
        coEvery { notifier.notifier } returns flow { emit(CourseStructureUpdated("")) }
        every { networkConnection.isOnline() } returns true
        coEvery { interactor.getCourseStatusFlow(any(), any()) } returns flowOf(
            CourseComponentStatus("id")
        )

        val viewModel = CourseContentAllViewModel(
            "",
            "",
            config,
            interactor,
            resourceManager,
            notifier,
            networkConnection,
            preferencesManager,
            analytics,
            downloadDialogManager,
            fileUtil,
            coreAnalytics,
            downloadModelsSource,
            workerController,
            downloadHelper,
        )

        val mockLifeCycleOwner: LifecycleOwner = mockk()
        val lifecycleRegistry = LifecycleRegistry(mockLifeCycleOwner)
        lifecycleRegistry.addObserver(viewModel)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)

        viewModel.getCourseData()
        advanceUntilIdle()

        coVerify(exactly = 3) { interactor.getCourseStructureFlow(any(), any()) }
        coVerify(exactly = 3) { interactor.getCourseStatusFlow(any(), any()) }
    }

    @Test
    fun `saveDownloadModels test`() = runTest(UnconfinedTestDispatcher()) {
        every { preferencesManager.videoSettings.wifiDownloadOnly } returns false
        coEvery { interactor.getCourseStructure(any()) } returns CoreMocks.mockCourseStructure
        coEvery { interactor.getCourseStructureFlow(any(), any()) } returns flowOf(
            CoreMocks.mockCourseStructure
        )
        every { networkConnection.isWifiConnected() } returns true
        every { networkConnection.isOnline() } returns true
        every {
            coreAnalytics.logEvent(
                CoreAnalyticsEvent.VIDEO_DOWNLOAD_SUBSECTION.eventName,
                any()
            )
        } returns Unit
        coEvery { workerController.saveModels(any()) } returns Unit
        coEvery { interactor.getCourseStatus(any()) } returns CourseComponentStatus("id")
        coEvery { interactor.getCourseStatusFlow(any(), any()) } returns flowOf(
            CourseComponentStatus("id")
        )
        coEvery { downloadModelsSource.getDownloadModelsFlow() } returns flow { emit(emptyList()) }
        every { config.getCourseUIConfig().isCourseDropdownNavigationEnabled } returns false

        val viewModel = CourseContentAllViewModel(
            "",
            "",
            config,
            interactor,
            resourceManager,
            notifier,
            networkConnection,
            preferencesManager,
            analytics,
            downloadDialogManager,
            fileUtil,
            coreAnalytics,
            downloadModelsSource,
            workerController,
            downloadHelper,
        )
        val message = async {
            withTimeoutOrNull(5000) {
                viewModel.uiMessage.first() as? UIMessage.SnackBarMessage
            }
        }
        viewModel.saveDownloadModels("", "", "")
        advanceUntilIdle()
        verify(exactly = 1) {
            coreAnalytics.logEvent(
                CoreAnalyticsEvent.VIDEO_DOWNLOAD_SUBSECTION.eventName,
                any()
            )
        }

        assert(message.await()?.message.isNullOrEmpty())
    }

    @Test
    fun `saveDownloadModels only wifi download, with connection`() =
        runTest(UnconfinedTestDispatcher()) {
            coEvery { interactor.getCourseStructure(any()) } returns CoreMocks.mockCourseStructure
            coEvery { interactor.getCourseStructureFlow(any(), any()) } returns flowOf(
                CoreMocks.mockCourseStructure
            )
            coEvery { interactor.getCourseStatus(any()) } returns CourseComponentStatus("id")
            coEvery { interactor.getCourseStatusFlow(any(), any()) } returns flowOf(
                CourseComponentStatus("id")
            )
            every { preferencesManager.videoSettings.wifiDownloadOnly } returns true
            every { networkConnection.isWifiConnected() } returns true
            every { networkConnection.isOnline() } returns true
            coEvery { workerController.saveModels(any()) } returns Unit
            coEvery { downloadModelsSource.getDownloadModelsFlow() } returns flow { emit(emptyList()) }
            every { config.getCourseUIConfig().isCourseDropdownNavigationEnabled } returns false
            every { coreAnalytics.logEvent(any(), any()) } returns Unit

            val viewModel = CourseContentAllViewModel(
                "",
                "",
                config,
                interactor,
                resourceManager,
                notifier,
                networkConnection,
                preferencesManager,
                analytics,
                downloadDialogManager,
                fileUtil,
                coreAnalytics,
                downloadModelsSource,
                workerController,
                downloadHelper,
            )
            val message = async {
                withTimeoutOrNull(5000) {
                    viewModel.uiMessage.first() as? UIMessage.SnackBarMessage
                }
            }
            viewModel.saveDownloadModels("", "", "")
            advanceUntilIdle()

            assert(message.await()?.message.isNullOrEmpty())
        }
}
