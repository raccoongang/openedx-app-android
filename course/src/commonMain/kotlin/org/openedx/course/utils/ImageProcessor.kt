package org.openedx.course.utils

interface ImageProcessor {
    fun loadAndProcessImage(
        imageUrl: String,
        defaultImageResId: Int,
        onComplete: (Any) -> Unit,
    )
}
