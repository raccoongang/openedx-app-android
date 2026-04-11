@file:Suppress("DEPRECATION")

package org.openedx.course.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.renderscript.Allocation
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import coil3.ImageLoader
import coil3.asDrawable
import coil3.asImage
import coil3.request.ImageRequest

class ImageProcessorImpl(private val context: Context) : ImageProcessor {

    override fun loadAndProcessImage(
        imageUrl: String,
        defaultImageResId: Int,
        onComplete: (Any) -> Unit,
    ) {
        loadImage(
            defaultImage = defaultImageResId,
            imageUrl = imageUrl,
            onComplete = { drawable ->
                val rawBitmap = (drawable as BitmapDrawable).bitmap
                val softwareBitmap = if (rawBitmap.config == Bitmap.Config.HARDWARE) {
                    rawBitmap.copy(Bitmap.Config.ARGB_8888, false)
                } else {
                    rawBitmap
                }
                val bitmap = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                    applyBlur(softwareBitmap, 10f)
                } else {
                    softwareBitmap
                }
                onComplete(bitmap)
            }
        )
    }

    fun loadImage(
        @DrawableRes
        defaultImage: Int,
        imageUrl: String,
        onComplete: (result: Drawable) -> Unit
    ) {
        val loader = ImageLoader.Builder(context).build()
        val defaultImg = ContextCompat.getDrawable(context, defaultImage)?.asImage()
        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .target { image ->
                onComplete(image.asDrawable(context.resources))
            }
            .error(defaultImg)
            .placeholder(defaultImg)
            .build()
        loader.enqueue(request)
    }

    fun applyBlur(
        bitmap: Bitmap,
        blurRadio: Float
    ): Bitmap {
        val renderScript = RenderScript.create(context)
        val bitmapAlloc = Allocation.createFromBitmap(renderScript, bitmap)
        ScriptIntrinsicBlur.create(renderScript, bitmapAlloc.element).apply {
            setRadius(blurRadio)
            setInput(bitmapAlloc)
            repeat(times = 3) {
                forEach(bitmapAlloc)
            }
        }
        val newBitmap: Bitmap = Bitmap.createBitmap(
            bitmap.width,
            bitmap.height,
            Bitmap.Config.ARGB_8888
        )
        bitmapAlloc.copyTo(newBitmap)
        renderScript.destroy()
        return newBitmap
    }
}
