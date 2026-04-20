package org.openedx.course.utils

/**
 * iOS ImageProcessor — no Bitmap preprocessing, just passes the URL through.
 * CollapsingLayout + Coil3 AsyncImage handle the actual loading / blurring on iOS.
 */
class IosImageProcessor : ImageProcessor {
    override fun loadAndProcessImage(
        imageUrl: String,
        defaultImageResId: Int,
        onComplete: (Any) -> Unit,
    ) {
        onComplete(imageUrl)
    }
}
