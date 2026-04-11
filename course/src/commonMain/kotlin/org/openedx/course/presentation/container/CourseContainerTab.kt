package org.openedx.course.presentation.container

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.TextSnippet
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Moving
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.StringResource
import org.openedx.core.ui.TabItem
import org.openedx.course.Res
import org.openedx.course.course_container_content_tab_all
import org.openedx.course.course_container_content_tab_assignment
import org.openedx.course.course_container_content_tab_video
import org.openedx.course.course_container_nav_content
import org.openedx.course.course_container_nav_dates
import org.openedx.course.course_container_nav_discussions
import org.openedx.course.course_container_nav_downloads
import org.openedx.course.course_container_nav_home
import org.openedx.course.course_container_nav_more
import org.openedx.course.course_container_nav_progress

enum class CourseContainerTab(
    override val labelRes: StringResource,
    override val icon: ImageVector,
) : TabItem {
    HOME(Res.string.course_container_nav_home, Icons.Default.Home),
    CONTENT(Res.string.course_container_nav_content, Icons.AutoMirrored.Filled.List),
    PROGRESS(Res.string.course_container_nav_progress, Icons.Default.Moving),
    DATES(Res.string.course_container_nav_dates, Icons.Outlined.CalendarMonth),
    OFFLINE(Res.string.course_container_nav_downloads, Icons.Filled.CloudDownload),
    DISCUSSIONS(Res.string.course_container_nav_discussions, Icons.AutoMirrored.Filled.Chat),
    MORE(Res.string.course_container_nav_more, Icons.AutoMirrored.Filled.TextSnippet),
}

enum class CourseContentTab(
    val labelRes: StringResource
) {
    ALL(Res.string.course_container_content_tab_all),
    VIDEOS(Res.string.course_container_content_tab_video),
    ASSIGNMENTS(Res.string.course_container_content_tab_assignment)
}
