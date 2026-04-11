package org.openedx.course.presentation.outline

import org.jetbrains.compose.resources.DrawableResource
import org.openedx.core.BlockType
import org.openedx.core.domain.model.Block
import org.openedx.course.*
import org.openedx.course.Res
import org.openedx.course.course_ic_video
import org.openedx.course.course_ic_pen
import org.openedx.course.course_ic_discussion
import org.openedx.course.course_ic_block

fun getUnitBlockIcon(block: Block): DrawableResource {
    return when (block.type) {
        BlockType.VIDEO -> Res.drawable.course_ic_video
        BlockType.PROBLEM -> Res.drawable.course_ic_pen
        BlockType.DISCUSSION -> Res.drawable.course_ic_discussion
        else -> Res.drawable.course_ic_block
    }
}
