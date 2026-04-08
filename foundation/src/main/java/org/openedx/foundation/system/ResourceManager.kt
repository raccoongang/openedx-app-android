package org.openedx.foundation.system

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import androidx.annotation.ArrayRes
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.FontRes
import androidx.annotation.PluralsRes
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import java.io.InputStream

class AndroidResourceManager(private val context: Context) : ResourceManager {

    override fun getString(@StringRes id: Int): String = context.getString(id)

    override fun getString(@StringRes id: Int, vararg formatArgs: Any): String = context.getString(id, *formatArgs)

    fun getStringArray(@ArrayRes id: Int): Array<String> = context.resources.getStringArray(id)

    fun getIntArray(@ArrayRes id: Int): IntArray = context.resources.getIntArray(id)

    fun getColor(@ColorRes id: Int): Int = ContextCompat.getColor(context, id)

    fun getFont(@FontRes id: Int): Typeface? = ResourcesCompat.getFont(context, id)

    fun getRaw(@RawRes id: Int): InputStream = context.resources.openRawResource(id)

    fun getQuantityString(@PluralsRes id: Int, quantity: Int): String =
        context.resources.getQuantityString(id, quantity)

    fun getQuantityString(@PluralsRes id: Int, quantity: Int, vararg formatArgs: Any): String =
        context.resources.getQuantityString(id, quantity, *formatArgs)

    fun getDrawable(@DrawableRes id: Int): Drawable? = ContextCompat.getDrawable(context, id)
}
