package org.openedx.core.domain.model

import org.openedx.core.extension.safeDivBy

data class Progress(
    val completed: Int,
    val total: Int,
) {

    val value: Float = completed.toFloat().safeDivBy(total.toFloat())

    companion object {
        val DEFAULT_PROGRESS = Progress(0, 0)
    }
}
