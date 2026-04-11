package org.openedx.profile.presentation.calendar

import org.jetbrains.compose.resources.StringResource
import org.openedx.core.Res as coreRes
import org.openedx.core.core_not_synced
import org.openedx.core.core_to_sync

enum class SyncCourseTab(
    val title: StringResource
) {
    SYNCED(coreRes.string.core_to_sync),
    NOT_SYNCED(coreRes.string.core_not_synced)
}
