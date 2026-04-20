package org.openedx.course.presentation.videos

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.openedx.core.CoreMocks
import org.openedx.core.config.Config
import org.openedx.core.domain.model.VideoProgress
import org.openedx.core.data.storage.CorePreferences
import org.openedx.core.domain.helper.VideoPreviewHelper
import org.openedx.core.domain.model.VideoSettings
import org.openedx.core.module.DownloadWorkerController
import org.openedx.core.module.download.DownloadModelsSource
import org.openedx.core.module.download.DownloadHelper
import org.openedx.core.presentation.CoreAnalytics
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
import org.openedx.foundation.Res as foundationRes
import org.openedx.foundation.foundation_error_no_connection
import org.openedx.foundation.foundation_error_unknown_error

@OptIn(ExperimentalCoroutinesApi::class)
class CourseVideoViewModelTest {
    @get:Rule
    val testInstantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    private val dispatcher = UnconfinedTestDispatcher()

    private val config = mockk<Config>()
    private val resourceManager = mockk<ResourceManager>()
    private val interactor = mockk<CourseInteractor>()
    private val courseNotifier = spyk<CourseNotifier>()
    private val coreAnalytics = mockk<CoreAnalytics>()
    private val courseAnalytics = mockk<CourseAnalytics>()
    private val preferencesManager = mockk<CorePreferences>()
    private val networkConnection = mockk<NetworkConnection>()
    private val downloadModelsSource = mockk<DownloadModelsSource>()
    private val workerController = mockk<DownloadWorkerController>()
    private val downloadHelper = mockk<DownloadHelper>()
    private val downloadDialogManager = mockk<DownloadDialogManager>()
    private val fileUtil = mockk<FileUtil>()
    private val videoPreviewHelper = mockk<VideoPreviewHelper>()

    private val cantDownload = "You can download content only from Wi-fi"

    @Before
    fun setUp() {
        every { resourceManager.getString(foundationRes.string.foundation_error_no_connection) } returns "Slow or no internet connection"
        every { resourceManager.getString(foundationRes.string.foundation_error_unknown_error) } returns "Something went wrong"
        every { resourceManager.getString(courseRes.string.course_can_download_only_with_wifi) } returns cantDownload
        Dispatchers.setMain(dispatcher)
        every { config.getApiHostURL() } returns "http://localhost:8000"
        every { courseNotifier.notifier } returns flowOf()
        every { preferencesManager.isRelativeDatesEnabled } returns true
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
            )
        } returns Unit

        every { videoPreviewHelper.getVideoPreviewWithId(any(), any(), any()) } returns Pair(
            "test",
            null
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getVideos empty list`() = runTest(UnconfinedTestDispatcher()) {
        every { config.getCourseUIConfig().isCourseDropdownNavigationEnabled } returns false
        coEvery {
            interactor.getCourseStructureForVideos(any())
        } returns CoreMocks.mockCourseStructure.copy(blockData = emptyList())
        every { downloadModelsSource.getDownloadModelsFlow() } returns flow { emit(emptyList()) }
        every { preferencesManager.videoSettings } returns VideoSettings.default
        val viewModel = CourseVideoViewModel(
            "",
            config,
            interactor,
            resourceManager,
            networkConnection,
            preferencesManager,
            courseNotifier,
            downloadDialogManager,
            fileUtil,
            courseAnalytics,
            videoPreviewHelper,
            coreAnalytics,
            downloadModelsSource,
            workerController,
            downloadHelper,
        )

        viewModel.getVideos()
        advanceUntilIdle()

        coVerify(exactly = 2) { interactor.getCourseStructureForVideos(any()) }

        assert(viewModel.uiState.value is CourseVideoUIState.Empty)
    }

    @Test
    fun `getVideos success`() = runTest(UnconfinedTestDispatcher()) {
        every { config.getCourseUIConfig().isCourseDropdownNavigationEnabled } returns false
        coEvery { interactor.getCourseStructureForVideos(any()) } returns CoreMocks.mockCourseStructure
        every { downloadModelsSource.getDownloadModelsFlow() } returns flow {
            repeat(5) {
                delay(10000)
                emit(emptyList())
            }
        }
        every { preferencesManager.videoSettings } returns VideoSettings.default
        val viewModel = CourseVideoViewModel(
            "",
            config,
            interactor,
            resourceManager,
            networkConnection,
            preferencesManager,
            courseNotifier,
            downloadDialogManager,
            fileUtil,
            courseAnalytics,
            videoPreviewHelper,
            coreAnalytics,
            downloadModelsSource,
            workerController,
            downloadHelper,
        )

        val mockLifeCycleOwner: LifecycleOwner = mockk()
        val lifecycleRegistry = LifecycleRegistry(mockLifeCycleOwner)
        lifecycleRegistry.addObserver(viewModel)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)

        advanceUntilIdle()

        coVerify(exactly = 1) { interactor.getCourseStructureForVideos(any()) }

        assert(viewModel.uiState.value is CourseVideoUIState.CourseData)
    }

    @Test
    fun `updateVideos success`() = runTest(UnconfinedTestDispatcher()) {
        every { config.getCourseUIConfig().isCourseDropdownNavigationEnabled } returns false
        coEvery { interactor.getCourseStructureForVideos(any()) } returns CoreMocks.mockCourseStructure
        coEvery { courseNotifier.notifier } returns flow {
            emit(CourseStructureUpdated(""))
        }
        every { downloadModelsSource.getDownloadModelsFlow() } returns flow {
            emit(emptyList())
        }
        every { preferencesManager.videoSettings } returns VideoSettings.default
        every { networkConnection.isOnline() } returns true
        coEvery { interactor.getVideoProgress(any()) } returns VideoProgress("", "", 0L, 0L)
        val viewModel = CourseVideoViewModel(
            "",
            config,
            interactor,
            resourceManager,
            networkConnection,
            preferencesManager,
            courseNotifier,
            downloadDialogManager,
            fileUtil,
            courseAnalytics,
            videoPreviewHelper,
            coreAnalytics,
            downloadModelsSource,
            workerController,
            downloadHelper,
        )

        val mockLifeCycleOwner: LifecycleOwner = mockk()
        val lifecycleRegistry = LifecycleRegistry(mockLifeCycleOwner)
        lifecycleRegistry.addObserver(viewModel)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)

        advanceUntilIdle()

        coVerify(exactly = 2) { interactor.getCourseStructureForVideos(any()) }

        assert(viewModel.uiState.value is CourseVideoUIState.CourseData)
    }

    @Test
    fun `setIsUpdating success`() = runTest(UnconfinedTestDispatcher()) {
        every { config.getCourseUIConfig().isCourseDropdownNavigationEnabled } returns false
        every { preferencesManager.videoSettings } returns VideoSettings.default
        coEvery { interactor.getCourseStructureForVideos(any()) } returns CoreMocks.mockCourseStructure
        coEvery { downloadModelsSource.getDownloadModelsFlow() } returns flow { emit(listOf(CoreMocks.mockDownloadModel)) }
        advanceUntilIdle()
    }

    @Test
    fun `saveDownloadModels test`() = runTest(UnconfinedTestDispatcher()) {
        every { config.getCourseUIConfig().isCourseDropdownNavigationEnabled } returns false
        every { preferencesManager.videoSettings } returns VideoSettings.default
        coEvery { interactor.getCourseStructureForVideos(any()) } returns CoreMocks.mockCourseStructure
        every { downloadModelsSource.getDownloadModelsFlow() } returns flow { emit(emptyList()) }
        val viewModel = CourseVideoViewModel(
            "",
            config,
            interactor,
            resourceManager,
            networkConnection,
            preferencesManager,
            courseNotifier,
            downloadDialogManager,
            fileUtil,
            courseAnalytics,
            videoPreviewHelper,
            coreAnalytics,
            downloadModelsSource,
            workerController,
            downloadHelper,
        )
        coEvery { interactor.getCourseStructureForVideos(any()) } returns CoreMocks.mockCourseStructure
        coEvery { downloadModelsSource.getDownloadModelsFlow() } returns flow { emit(listOf(CoreMocks.mockDownloadModel)) }
        every { preferencesManager.videoSettings.wifiDownloadOnly } returns false
        every { networkConnection.isWifiConnected() } returns true
        coEvery { workerController.saveModels(any()) } returns Unit
        every { coreAnalytics.logEvent(any(), any()) } returns Unit
        val message = async {
            withTimeoutOrNull(5000) {
                viewModel.uiMessage.first() as? UIMessage.SnackBarMessage
            }
        }
        viewModel.saveDownloadModels("", "", "")
        advanceUntilIdle()

        assert(message.await()?.message.isNullOrEmpty())
    }

    @Test
    fun `saveDownloadModels only wifi download, with connection`() =
        runTest(UnconfinedTestDispatcher()) {
            every { config.getCourseUIConfig().isCourseDropdownNavigationEnabled } returns false
            every { preferencesManager.videoSettings } returns VideoSettings.default
            coEvery { interactor.getCourseStructureForVideos(any()) } returns CoreMocks.mockCourseStructure
            every { downloadModelsSource.getDownloadModelsFlow() } returns flow { emit(emptyList()) }
            val viewModel = CourseVideoViewModel(
                "",
                config,
                interactor,
                resourceManager,
                networkConnection,
                preferencesManager,
                courseNotifier,
                downloadDialogManager,
                fileUtil,
                courseAnalytics,
                videoPreviewHelper,
                coreAnalytics,
                downloadModelsSource,
                workerController,
                downloadHelper,
            )
            coEvery { interactor.getCourseStructureForVideos(any()) } returns CoreMocks.mockCourseStructure
            coEvery { downloadModelsSource.getDownloadModelsFlow() } returns flow { emit(listOf(CoreMocks.mockDownloadModel)) }
            every { preferencesManager.videoSettings.wifiDownloadOnly } returns true
            every { networkConnection.isWifiConnected() } returns true
            coEvery { workerController.saveModels(any()) } returns Unit
            coEvery { downloadModelsSource.getDownloadModelsFlow() } returns flow {
                emit(listOf((CoreMocks.mockDownloadModel)))
            }
            every { coreAnalytics.logEvent(any(), any()) } returns Unit
            val message = async {
                withTimeoutOrNull(5000) {
                    viewModel.uiMessage.first() as? UIMessage.SnackBarMessage
                }
            }

            viewModel.saveDownloadModels("", "", "")
            advanceUntilIdle()

            assert(message.await()?.message.isNullOrEmpty())
        }

    @Test
    fun `saveDownloadModels only wifi download, without connection`() =
        runTest(UnconfinedTestDispatcher()) {
            every { config.getCourseUIConfig().isCourseDropdownNavigationEnabled } returns false
            every { preferencesManager.videoSettings } returns VideoSettings.default
            every { downloadModelsSource.getDownloadModelsFlow() } returns flow { emit(emptyList()) }
            coEvery { interactor.getCourseStructureForVideos(any()) } returns CoreMocks.mockCourseStructure
            val viewModel = CourseVideoViewModel(
                "",
                config,
                interactor,
                resourceManager,
                networkConnection,
                preferencesManager,
                courseNotifier,
                downloadDialogManager,
                fileUtil,
                courseAnalytics,
                videoPreviewHelper,
                coreAnalytics,
                downloadModelsSource,
                workerController,
                downloadHelper,
            )
            every { preferencesManager.videoSettings.wifiDownloadOnly } returns true
            every { networkConnection.isWifiConnected() } returns false
            every { networkConnection.isOnline() } returns false
            coEvery { interactor.getCourseStructureForVideos(any()) } returns CoreMocks.mockCourseStructure
            coEvery { downloadModelsSource.getDownloadModelsFlow() } returns flow { emit(listOf(CoreMocks.mockDownloadModel)) }
            coEvery { workerController.saveModels(any()) } returns Unit
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
