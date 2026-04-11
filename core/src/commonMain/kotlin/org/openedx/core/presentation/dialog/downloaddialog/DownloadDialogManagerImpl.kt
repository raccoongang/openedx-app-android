package org.openedx.core.presentation.dialog.downloaddialog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.openedx.core.BlockType
import org.openedx.core.data.storage.CorePreferences
import org.openedx.core.domain.interactor.CourseInteractor
import org.openedx.core.domain.model.Block
import org.openedx.core.domain.model.DownloadCoursePreview
import org.openedx.core.module.DownloadWorkerController
import org.openedx.core.module.db.DownloadModel
import org.openedx.core.system.StorageManager
import org.openedx.core.system.connection.NetworkConnection

class DownloadDialogManagerImpl(
    private val networkConnection: NetworkConnection,
    private val corePreferences: CorePreferences,
    private val interactor: CourseInteractor,
    private val workerController: DownloadWorkerController,
    private val storageManager: StorageManager,
) : DownloadDialogManager {

    private val internalState = MutableSharedFlow<DownloadDialogUIState>()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val _pendingDialog = MutableStateFlow<PendingDownloadDialog?>(null)
    override val pendingDialog: StateFlow<PendingDownloadDialog?> = _pendingDialog.asStateFlow()

    override fun dismissDialog() {
        _pendingDialog.value = null
    }

    init {
        coroutineScope.launch {
            internalState.collect { state ->
                val dialogType = when {
                    state.isDownloadFailed -> DownloadDialogType.DOWNLOAD_FAILED
                    state.isAllBlocksDownloaded -> DownloadDialogType.REMOVE_DOWNLOAD
                    !networkConnection.isOnline() -> DownloadDialogType.NO_CONNECTION
                    storageManager.getFreeStorage() < state.sizeSum * DownloadDialogManager.DOWNLOAD_SIZE_FACTOR -> DownloadDialogType.STORAGE_ERROR
                    corePreferences.videoSettings.wifiDownloadOnly && !networkConnection.isWifiConnected() -> DownloadDialogType.WIFI_REQUIRED
                    !corePreferences.videoSettings.wifiDownloadOnly && !networkConnection.isWifiConnected() -> DownloadDialogType.DOWNLOAD_ON_CELLULAR
                    state.sizeSum >= DownloadDialogManager.MAX_CELLULAR_SIZE -> DownloadDialogType.CONFIRM_DOWNLOAD
                    else -> null
                }

                if (dialogType != null) {
                    _pendingDialog.value = PendingDownloadDialog(
                        dialogType = dialogType,
                        uiState = state,
                        onConfirm = {
                            state.onConfirmClick()
                            _pendingDialog.value = null
                        },
                        onDismiss = {
                            state.onDismissClick()
                            _pendingDialog.value = null
                        },
                    )
                } else {
                    state.onConfirmClick()
                    state.saveDownloadModels()
                }
            }
        }
    }

    override fun showPopup(
        subSectionsBlocks: List<Block>,
        courseId: String,
        isBlocksDownloaded: Boolean,
        onlyVideoBlocks: Boolean,
        fragmentManager: Any?,
        removeDownloadModels: (blockId: String, courseId: String) -> Unit,
        saveDownloadModels: (blockId: String) -> Unit,
        onDismissClick: () -> Unit,
        onConfirmClick: () -> Unit,
    ) {
        createDownloadItems(
            subSectionsBlocks = subSectionsBlocks,
            courseId = courseId,
            fragmentManager = fragmentManager,
            isBlocksDownloaded = isBlocksDownloaded,
            onlyVideoBlocks = onlyVideoBlocks,
            removeDownloadModels = removeDownloadModels,
            saveDownloadModels = saveDownloadModels,
            onDismissClick = onDismissClick,
            onConfirmClick = onConfirmClick
        )
    }

    override fun showPopup(
        coursePreview: DownloadCoursePreview,
        isBlocksDownloaded: Boolean,
        fragmentManager: Any?,
        removeDownloadModels: (blockId: String, courseId: String) -> Unit,
        saveDownloadModels: () -> Unit,
        onDismissClick: () -> Unit,
        onConfirmClick: () -> Unit,
    ) {
        createCourseDownloadItems(
            coursePreview = coursePreview,
            fragmentManager = fragmentManager,
            isBlocksDownloaded = isBlocksDownloaded,
            removeDownloadModels = removeDownloadModels,
            saveDownloadModels = saveDownloadModels,
            onDismissClick = onDismissClick,
            onConfirmClick = onConfirmClick
        )
    }

    override fun showRemoveDownloadModelPopup(
        downloadDialogItem: DownloadDialogItem,
        fragmentManager: Any?,
        removeDownloadModels: () -> Unit,
    ) {
        coroutineScope.launch {
            internalState.emit(
                DownloadDialogUIState(
                    downloadDialogItems = listOf(downloadDialogItem),
                    isAllBlocksDownloaded = true,
                    isDownloadFailed = false,
                    sizeSum = downloadDialogItem.size,
                    fragmentManager = fragmentManager,
                    removeDownloadModels = removeDownloadModels,
                    saveDownloadModels = {}
                )
            )
        }
    }

    override fun showDownloadFailedPopup(
        downloadModel: List<DownloadModel>,
        fragmentManager: Any?,
    ) {
        createDownloadItems(
            downloadModels = downloadModel,
            fragmentManager = fragmentManager,
        )
    }

    private fun createDownloadItems(
        downloadModels: List<DownloadModel>,
        fragmentManager: Any?,
    ) {
        coroutineScope.launch {
            val courseIds = downloadModels.map { it.courseId }.distinct()
            val blockIds = downloadModels.map { it.id }
            val allDownloadDialogItems = mutableListOf<DownloadDialogItem>()

            courseIds.forEach { courseId ->
                val courseStructure = interactor.getCourseStructureFromCache(courseId)
                val allSubSectionBlocks =
                    courseStructure.blockData.filter { it.type == BlockType.SEQUENTIAL }

                allSubSectionBlocks.forEach { subSectionBlock ->
                    val verticalBlocks =
                        courseStructure.blockData.filter { it.id in subSectionBlock.descendants }
                    val blocks = courseStructure.blockData.filter {
                        it.id in verticalBlocks.flatMap { it.descendants } && it.id in blockIds
                    }
                    val totalSize = blocks.sumOf { it.getFileSize() }

                    if (totalSize > 0) {
                        allDownloadDialogItems.add(
                            DownloadDialogItem(
                                title = subSectionBlock.displayName,
                                size = totalSize
                            )
                        )
                    }
                }
            }

            internalState.emit(
                DownloadDialogUIState(
                    downloadDialogItems = allDownloadDialogItems,
                    isAllBlocksDownloaded = false,
                    isDownloadFailed = true,
                    sizeSum = allDownloadDialogItems.sumOf { it.size },
                    fragmentManager = fragmentManager,
                    removeDownloadModels = {},
                    saveDownloadModels = {
                        coroutineScope.launch {
                            workerController.saveModels(downloadModels)
                        }
                    }
                )
            )
        }
    }

    private fun createDownloadItems(
        subSectionsBlocks: List<Block>,
        courseId: String,
        fragmentManager: Any?,
        isBlocksDownloaded: Boolean,
        onlyVideoBlocks: Boolean,
        removeDownloadModels: (blockId: String, courseId: String) -> Unit,
        saveDownloadModels: (blockId: String) -> Unit,
        onDismissClick: () -> Unit = {},
        onConfirmClick: () -> Unit = {},
    ) {
        coroutineScope.launch {
            val courseStructure = interactor.getCourseStructure(courseId, false)
            val downloadModelIds = interactor.getAllDownloadModels().map { it.id }

            val downloadDialogItems = subSectionsBlocks.mapNotNull { subSectionBlock ->
                val verticalBlocks =
                    courseStructure.blockData.filter { it.id in subSectionBlock.descendants }
                val blocks = verticalBlocks.flatMap { verticalBlock ->
                    courseStructure.blockData.filter {
                        it.id in verticalBlock.descendants &&
                                (isBlocksDownloaded == (it.id in downloadModelIds)) &&
                                (!onlyVideoBlocks || it.type == BlockType.VIDEO)
                    }
                }
                val size = blocks.sumOf { it.getFileSize() }
                if (size > 0) {
                    DownloadDialogItem(
                        title = subSectionBlock.displayName,
                        size = size
                    )
                } else {
                    null
                }
            }

            internalState.emit(
                DownloadDialogUIState(
                    downloadDialogItems = downloadDialogItems,
                    isAllBlocksDownloaded = isBlocksDownloaded,
                    isDownloadFailed = false,
                    sizeSum = downloadDialogItems.sumOf { it.size },
                    fragmentManager = fragmentManager,
                    removeDownloadModels = {
                        subSectionsBlocks.forEach {
                            removeDownloadModels(it.id, courseId)
                        }
                    },
                    saveDownloadModels = { subSectionsBlocks.forEach { saveDownloadModels(it.id) } },
                    onDismissClick = onDismissClick,
                    onConfirmClick = onConfirmClick,
                )
            )
        }
    }

    private fun createCourseDownloadItems(
        coursePreview: DownloadCoursePreview,
        fragmentManager: Any?,
        isBlocksDownloaded: Boolean,
        removeDownloadModels: (blockId: String, courseId: String) -> Unit,
        saveDownloadModels: () -> Unit,
        onDismissClick: () -> Unit = {},
        onConfirmClick: () -> Unit = {},
    ) {
        coroutineScope.launch {
            val downloadDialogItems = listOf(
                DownloadDialogItem(
                    title = coursePreview.name,
                    size = coursePreview.totalSize,
                    icon = Icons.Default.School
                )
            )

            internalState.emit(
                DownloadDialogUIState(
                    downloadDialogItems = downloadDialogItems,
                    isAllBlocksDownloaded = isBlocksDownloaded,
                    isDownloadFailed = false,
                    sizeSum = downloadDialogItems.sumOf { it.size },
                    fragmentManager = fragmentManager,
                    removeDownloadModels = {
                        coroutineScope.launch {
                            val downloadModels = interactor.getAllDownloadModels().filter {
                                it.courseId == coursePreview.id
                            }
                            downloadModels.forEach {
                                removeDownloadModels(it.id, coursePreview.id)
                            }
                        }
                    },
                    saveDownloadModels = saveDownloadModels,
                    onDismissClick = onDismissClick,
                    onConfirmClick = onConfirmClick,
                )
            )
        }
    }
}
