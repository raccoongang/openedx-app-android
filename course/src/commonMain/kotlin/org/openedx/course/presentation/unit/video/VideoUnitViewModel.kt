package org.openedx.course.presentation.unit.video

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
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
import org.openedx.core.system.notifier.VideoProgressUpdated
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

    fun setTranscriptLanguage(lang: String) {
        if (lang == transcriptLanguage) return
        transcriptLanguage = lang
        downloadSubtitles()
    }

    var isDownloaded = false

    private val _currentVideoTime = MutableStateFlow(0L)
    val currentVideoTime: StateFlow<Long> = _currentVideoTime.asStateFlow()

    // Emits the start position once loaded from DB (null until then). Compose can
    // wait on this before creating native players that take startPositionMs only once.
    private val _initialStartPositionMs = MutableStateFlow<Long?>(null)
    val initialStartPositionMs: StateFlow<Long?> = _initialStartPositionMs.asStateFlow()

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

    @OptIn(DelicateCoroutinesApi::class)
    fun saveVideoProgress() {
        // Use GlobalScope so the DB write survives even if the ViewModel is cleared
        // immediately after this call (happens on Compose Navigation pop when the
        // NavBackStackEntry is destroyed before viewModelScope work completes).
        val snapshotTime = _currentVideoTime.value
        val snapshotDuration = duration
        val url = videoUrl
        val id = blockId
        val notifierRef = notifier
        GlobalScope.launch(Dispatchers.Default) {
            courseRepository.saveVideoProgress(id, url, snapshotTime, snapshotDuration)
            notifierRef.send(VideoProgressUpdated())
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
                val saved = videoProgress.videoTime ?: 0L
                _currentVideoTime.value = saved
                _initialStartPositionMs.value = saved
            } catch (e: Exception) {
                e.printStackTrace()
                _initialStartPositionMs.value = 0L
            }
        }
    }
}
