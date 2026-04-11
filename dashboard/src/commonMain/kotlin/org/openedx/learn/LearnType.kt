package org.openedx.learn

import org.jetbrains.compose.resources.StringResource
import org.openedx.dashboard.Res
import org.openedx.dashboard.dashboard_courses
import org.openedx.dashboard.dashboard_programs

enum class LearnType(val titleRes: StringResource) {
    COURSES(Res.string.dashboard_courses),
    PROGRAMS(Res.string.dashboard_programs)
}
