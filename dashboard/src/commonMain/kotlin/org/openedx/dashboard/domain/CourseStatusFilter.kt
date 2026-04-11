package org.openedx.dashboard.domain

import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.StringResource
import org.openedx.core.ui.TabItem
import org.openedx.dashboard.Res
import org.openedx.dashboard.dashboard_course_filter_all
import org.openedx.dashboard.dashboard_course_filter_completed
import org.openedx.dashboard.dashboard_course_filter_expired
import org.openedx.dashboard.dashboard_course_filter_in_progress

enum class CourseStatusFilter(
    val key: String,
    override val labelRes: StringResource,
    override val icon: ImageVector? = null,
) : TabItem {
    ALL("all", Res.string.dashboard_course_filter_all),
    IN_PROGRESS("in_progress", Res.string.dashboard_course_filter_in_progress),
    COMPLETE("completed", Res.string.dashboard_course_filter_completed),
    EXPIRED("expired", Res.string.dashboard_course_filter_expired)
}
