package org.openedx.foundation.extension

import androidx.fragment.app.Fragment
import org.openedx.foundation.presentation.WindowSize
import org.openedx.foundation.presentation.getScreenHeight
import org.openedx.foundation.presentation.getScreenWidth

fun Fragment.computeWindowSizeClasses(): WindowSize {
    val configuration = requireContext().resources.configuration
    return WindowSize(
        width = getScreenWidth(configuration.screenWidthDp),
        height = getScreenHeight(configuration.screenHeightDp),
    )
}
