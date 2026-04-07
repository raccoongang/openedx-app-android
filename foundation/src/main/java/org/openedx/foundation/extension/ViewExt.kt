package org.openedx.foundation.extension

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Toast
import androidx.fragment.app.DialogFragment

fun Context.dpToPixel(dp: Int): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp.toFloat(),
        resources.displayMetrics,
    )
}

fun Context.dpToPixel(dp: Float): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp,
        resources.displayMetrics,
    )
}

fun View.requestApplyInsetsWhenAttached() {
    if (isAttachedToWindow) {
        requestApplyInsets()
    } else {
        addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                v.removeOnAttachStateChangeListener(this)
                v.requestApplyInsets()
            }

            override fun onViewDetachedFromWindow(v: View) = Unit
        })
    }
}

fun DialogFragment.setWidthPercent(percentage: Int) {
    val percent = percentage.toFloat() / 100
    val dm = resources.displayMetrics
    val width = dm.widthPixels
    dialog?.window?.setLayout((width * percent).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
}

fun Context.toastMessage(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun WebView.applyDarkModeIfEnabled(isDarkMode: Boolean) {
    if (isDarkMode) {
        evaluateJavascript(
            """
            (function() {
                document.body.style.backgroundColor = '#1C1C1E';
                document.body.style.color = '#FFFFFF';
                var links = document.getElementsByTagName('a');
                for(var i=0; i<links.length; i++) {
                    links[i].style.color = '#3C91E4';
                }
            })();
            """.trimIndent(),
            null,
        )
    }
}
