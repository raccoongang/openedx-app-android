package org.openedx.core.lmsdirectory

import android.content.Context
import coil.ImageLoader
import coil.request.ImageRequest

/**
 * Turns a directory image field into something Coil can load.
 *
 * The document carries image fields as plain strings. A value that looks like a
 * web address is downloaded; anything else is the name of a file shipped in the
 * app's assets. That one rule is what lets the same document serve an operator
 * who hosts their images and one who bundles them, with no second set of fields
 * to keep in step.
 */
object LmsImageSource {

    private const val ASSET_SCHEME = "file:///android_asset/"

    /**
     * A Coil model for [value], or null when there is nothing to show.
     *
     * Coil reads `file:///android_asset/…` natively, so a bundled image needs no
     * special case anywhere it is rendered — only here.
     */
    fun model(value: String?): String? {
        val trimmed = value?.trim().orEmpty()
        if (trimmed.isEmpty()) return null
        return if (isRemote(trimmed)) trimmed else ASSET_SCHEME + trimmed.trimStart('/')
    }

    fun isRemote(value: String): Boolean =
        value.startsWith("http://", ignoreCase = true) ||
            value.startsWith("https://", ignoreCase = true)

    /**
     * Warm images before the screens that show them are built.
     *
     * Worth doing only because the whole directory arrives at once: a platform's
     * sign-in background is known while the learner is still choosing, so by the
     * time they pick one it is already decoded and the branded screen does not
     * visibly assemble itself.
     */
    fun prefetch(context: Context, values: List<String>, loader: ImageLoader? = null) {
        val imageLoader = loader ?: coil.Coil.imageLoader(context)
        values.asSequence()
            .mapNotNull { model(it) }
            .distinct()
            .forEach { imageLoader.enqueue(ImageRequest.Builder(context).data(it).build()) }
    }
}
