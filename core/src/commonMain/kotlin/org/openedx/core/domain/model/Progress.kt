package org.openedx.core.domain.model

import kotlinx.parcelize.IgnoredOnParcel
import org.openedx.core.extension.safeDivBy

data class Progress(
    val completed: Int,
    val total: Int,
) {

    @IgnoredOnParcel
    val value: Float = completed.toFloat().safeDivBy(total.toFloat())

    companion object {
        val DEFAULT_PROGRESS = Progress(0, 0)
    }
}
