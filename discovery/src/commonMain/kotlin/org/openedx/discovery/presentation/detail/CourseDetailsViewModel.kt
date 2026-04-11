package org.openedx.discovery.presentation.detail

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.openedx.core.config.Config
import org.openedx.core.data.storage.CorePreferences
import org.openedx.core.system.connection.NetworkConnection
import org.openedx.core.system.notifier.CourseDashboardUpdate
import org.openedx.core.system.notifier.DiscoveryNotifier
import org.openedx.core.worker.CalendarSyncScheduler
import org.openedx.discovery.domain.interactor.DiscoveryInteractor
import org.openedx.discovery.domain.model.Course
import org.openedx.discovery.presentation.DiscoveryAnalytics
import org.openedx.discovery.presentation.DiscoveryAnalyticsEvent
import org.openedx.discovery.presentation.DiscoveryAnalyticsKey
import org.openedx.foundation.presentation.BaseViewModel
import org.openedx.foundation.system.ResourceManager
import org.openedx.foundation.Res as foundationRes
import org.openedx.foundation.foundation_error_no_connection
import org.openedx.foundation.foundation_error_unknown_error

class CourseDetailsViewModel(
    val courseId: String,
    private val config: Config,
    private val corePreferences: CorePreferences,
    private val networkConnection: NetworkConnection,
    private val interactor: DiscoveryInteractor,
    private val resourceManager: ResourceManager,
    private val notifier: DiscoveryNotifier,
    private val analytics: DiscoveryAnalytics,
    private val calendarSyncScheduler: CalendarSyncScheduler,
) : BaseViewModel(
    noConnectionMessage = resourceManager.getString(foundationRes.string.foundation_error_no_connection),
    defaultErrorMessage = resourceManager.getString(foundationRes.string.foundation_error_unknown_error),
) {
    val apiHostUrl get() = config.getApiHostURL()
    val isUserLoggedIn get() = corePreferences.user != null
    val isRegistrationEnabled: Boolean get() = config.isRegistrationEnabled()

    private val _uiState = MutableStateFlow<CourseDetailsUIState>(CourseDetailsUIState.Loading)
    val uiState: StateFlow<CourseDetailsUIState> = _uiState.asStateFlow()

    private var course: Course? = null

    val hasInternetConnection: Boolean
        get() = networkConnection.isOnline()

    init {
        getCourseDetail()
    }

    fun getCourseDetail() {
        _uiState.value = CourseDetailsUIState.Loading
        viewModelScope.launch {
            try {
                course = if (hasInternetConnection) {
                    interactor.getCourseDetails(courseId)
                } else {
                    interactor.getCourseDetailsFromCache(courseId)
                }
                course?.let {
                    _uiState.value = CourseDetailsUIState.CourseData(
                        course = it,
                        isUserLoggedIn = isUserLoggedIn
                    )
                } ?: run {
                    handleErrorUiMessage(
                        throwable = null,
                    )
                }
            } catch (e: Exception) {
                handleErrorUiMessage(
                    throwable = e,
                )
            }
        }
    }

    fun enrollInACourse(id: String, title: String) {
        viewModelScope.launch {
            try {
                val courseData = _uiState.value
                if (courseData is CourseDetailsUIState.CourseData) {
                    courseEnrollClickedEvent(id, title)
                }
                interactor.enrollInACourse(id)
                val course = interactor.getCourseDetails(id)
                if (courseData is CourseDetailsUIState.CourseData) {
                    _uiState.value = courseData.copy(course = course)
                    courseEnrollSuccessEvent(id, title)
                    calendarSyncScheduler.requestImmediateSync(id)
                    notifier.send(CourseDashboardUpdate())
                }
            } catch (e: Exception) {
                handleErrorUiMessage(
                    throwable = e,
                )
            }
        }
    }

    fun getCourseAboutBody(bgColor: ULong, textColor: ULong): String {
        val darkThemeStyle = "<style>\n" +
                "      body {\n" +
                "        background-color: #${getColorFromULong(bgColor)};\n" +
                "        color: #${getColorFromULong(textColor)};\n" +
                "      }\n" +
                "    </style>"
        val buff = StringBuilder().apply {
            if (bgColor != ULong.MIN_VALUE) append(darkThemeStyle)
            append("<body>")
            append("<div class=\"header\">")
            append(course?.overview ?: "")
            append("</div>")
            append("</body>")
        }
        return buff.toString()
    }

    private fun getColorFromULong(color: ULong): String {
        if (color == ULong.MIN_VALUE) return "black"
        return color.toLong().toULong().toString(16).padStart(16, '0').substring(
            startIndex = 2,
            endIndex = 8
        )
    }

    private fun courseEnrollClickedEvent(courseId: String, courseTitle: String) {
        logEvent(DiscoveryAnalyticsEvent.COURSE_ENROLL_CLICKED, courseId, courseTitle)
    }

    private fun courseEnrollSuccessEvent(courseId: String, courseTitle: String) {
        logEvent(DiscoveryAnalyticsEvent.COURSE_ENROLL_SUCCESS, courseId, courseTitle)
    }

    private fun logEvent(
        event: DiscoveryAnalyticsEvent,
        courseId: String,
        courseTitle: String,
    ) {
        analytics.logEvent(
            event.eventName,
            buildMap {
                put(DiscoveryAnalyticsKey.NAME.key, event.biValue)
                put(DiscoveryAnalyticsKey.COURSE_ID.key, courseId)
                put(DiscoveryAnalyticsKey.COURSE_NAME.key, courseTitle)
                put(DiscoveryAnalyticsKey.CONVERSION.key, courseId)
            }
        )
    }
}
