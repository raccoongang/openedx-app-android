package org.openedx.course.presentation.unit.video

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.openedx.core.module.TranscriptProvider
import org.openedx.core.system.connection.NetworkConnection
import org.openedx.core.system.notifier.CourseCompletionSet
import org.openedx.core.system.notifier.CourseNotifier
import org.openedx.core.system.notifier.CourseSubtitleLanguageChanged
import org.openedx.core.system.notifier.CourseVideoPositionChanged
import org.openedx.course.data.repository.CourseRepository
import org.openedx.course.presentation.CourseAnalytics
import org.openedx.foundation.system.ResourceManager

open class VideoUnitViewModel(
    val courseId: String,
    val videoUrl: String,
    val blockId: String,
    private val courseRepository: CourseRepository,
    private val notifier: CourseNotifier,
    private val networkConnection: NetworkConnection,
    private val transcriptProvider: TranscriptProvider,
    courseAnalytics: CourseAnalytics,
    resourceManager: ResourceManager,
) : BaseVideoViewModel(courseId, courseAnalytics, resourceManager) {

    var transcripts = emptyMap<String, String>()
    var isPlaying = true
    var transcriptLanguage = "en"
        private set

    var isDownloaded = false

    private val _currentVideoTime = MutableStateFlow(0L)
    val currentVideoTime: StateFlow<Long> = _currentVideoTime.asStateFlow()

    var duration = 0L

    protected val isUpdatedMutable = MutableStateFlow(true)
    val isUpdated: StateFlow<Boolean> = isUpdatedMutable.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex = _currentIndex.asStateFlow()

    private val _transcriptObject = MutableStateFlow<Any?>(null)
    val transcriptObject: StateFlow<Any?> = _transcriptObject.asStateFlow()

    private var timeList: List<Long>? = emptyList()

    val hasInternetConnection: Boolean
        get() = networkConnection.isOnline()

    private var isBlockAlreadyCompleted = false

    init {
        initVideoProgress()
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        viewModelScope.launch {
            notifier.notifier.collect {
                if (it is CourseVideoPositionChanged && videoUrl == it.videoUrl) {
                    isUpdatedMutable.value = false
                    _currentVideoTime.value = it.videoTime
                    saveVideoProgress()
                    isUpdatedMutable.value = true
                    isPlaying = it.isPlaying
                } else if (it is CourseSubtitleLanguageChanged) {
                    transcriptLanguage = it.value
                    _transcriptObject.value = null
                    downloadSubtitles()
                }
            }
        }
    }

    override fun onPause(owner: LifecycleOwner) {
        saveVideoProgress()
        super.onPause(owner)
    }

    private fun saveVideoProgress() {
        viewModelScope.launch {
            courseRepository.saveVideoProgress(
                blockId,
                videoUrl,
                _currentVideoTime.value,
                duration
            )
        }
    }

    fun downloadSubtitles() {
        viewModelScope.launch(Dispatchers.Default) {
            transcriptProvider.downloadTranscripts(getTranscriptUrl())?.let { result ->
                _transcriptObject.value = result.transcriptObject
                timeList = result.timeList
            }
        }
    }

    private fun getTranscriptUrl(): String {
        val defaultTranscripts = transcripts[transcriptLanguage]
        return when {
            !defaultTranscripts.isNullOrEmpty() -> defaultTranscripts
            transcripts.values.isNotEmpty() -> {
                transcriptLanguage = transcripts.keys.first()
                transcripts[transcriptLanguage] ?: ""
            }

            else -> ""
        }
    }

    open fun markBlockCompleted(blockId: String, medium: String) {
        if (!isBlockAlreadyCompleted) {
            logLoadedCompletedEvent(videoUrl, false, getCurrentVideoTime(), medium)
            viewModelScope.launch {
                try {
                    isBlockAlreadyCompleted = true
                    courseRepository.markBlocksCompletion(
                        courseId,
                        listOf(blockId)
                    )
                    notifier.send(CourseCompletionSet())
                } catch (e: Exception) {
                    e.printStackTrace()
                    isBlockAlreadyCompleted = false
                }
            }
        }
    }

    fun setCurrentVideoTime(value: Long) {
        _currentVideoTime.value = value
        timeList?.let {
            val index = it.indexOfLast { subtitleTime ->
                subtitleTime < value
            }
            if (index != currentIndex.value) {
                _currentIndex.value = index
            }
        }
    }

    fun getCurrentVideoTime() = currentVideoTime.value

    private fun initVideoProgress() {
        viewModelScope.launch {
            try {
                val videoProgress = courseRepository.getVideoProgress(blockId)
                _currentVideoTime.value = videoProgress.videoTime ?: 0L
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
