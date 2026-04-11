package org.openedx.core.ui

import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.StringResource

interface TabItem {
    val labelRes: StringResource
    val icon: ImageVector?
}
