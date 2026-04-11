package org.openedx.core.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

const val KEYBOARD_VISIBILITY_THRESHOLD = 0.15f

inline val isPreview: Boolean
    @ReadOnlyComposable
    @Composable
    get() {
        return LocalInspectionMode.current
    }

@Stable
inline val Int.px: Float
    @Composable
    get() {
        return this.toFloat().px
    }

@Stable
inline val Float.px: Float
    @Composable
    get() {
        val density = LocalDensity.current.density
        return this * density
    }

fun LazyListState.shouldLoadMore(rememberedIndex: MutableState<Int>, threshold: Int): Boolean {
    val firstVisibleIndex = this.firstVisibleItemIndex
    if (rememberedIndex.value != firstVisibleIndex) {
        rememberedIndex.value = firstVisibleIndex
        val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        return lastVisibleIndex >= layoutInfo.totalItemsCount - 1 - threshold
    }
    return false
}

fun LazyGridState.shouldLoadMore(rememberedIndex: MutableState<Int>, threshold: Int): Boolean {
    val firstVisibleIndex = this.firstVisibleItemIndex
    if (rememberedIndex.value != firstVisibleIndex) {
        rememberedIndex.value = firstVisibleIndex
        val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        return lastVisibleIndex >= layoutInfo.totalItemsCount - 1 - threshold
    }
    return false
}

fun Modifier.roundBorderWithoutBottom(borderWidth: Dp, cornerRadius: Dp): Modifier = composed(
    factory = {
        var path: Path
        this.then(
            Modifier.drawWithCache {
                val height = this.size.height
                val width = this.size.width
                onDrawWithContent {
                    drawContent()
                    path = Path().apply {
                        moveTo(width.times(0f), height.times(1f))
                        lineTo(width.times(0f), height.times(0f))
                        lineTo(width.times(1f), height.times(0f))
                        lineTo(width.times(1f), height.times(1f))
                    }
                    drawPath(
                        path = path,
                        color = Color.LightGray,
                        style = Stroke(
                            width = borderWidth.toPx(),
                            pathEffect = PathEffect.cornerPathEffect(cornerRadius.toPx())
                        )
                    )
                }
            }
        )
    }
)

@Composable
fun <T : Any> rememberSaveableMap(init: () -> MutableMap<String, T?>): MutableMap<String, T?> {
    return rememberSaveable(
        saver = mapSaver(
            save = {
                it.toMap()
            },
            restore = {
                it.toMutableMap() as MutableMap<String, T?>
            }
        )
    ) {
        init()
    }
}

@Composable
fun isImeVisibleState(threshold: Int = 0): State<Boolean> {
    val imeInsets = WindowInsets.ime
    val imeBottom = imeInsets.getBottom(LocalDensity.current)
    val isOpen = remember(imeBottom) { mutableStateOf(false) }

    LaunchedEffect(imeBottom) {
        isOpen.value = imeBottom > threshold
    }

    return isOpen
}

fun PagerState.calculateCurrentOffsetForPage(page: Int): Float {
    return (currentPage - page) + currentPageOffsetFraction
}

fun Modifier.crop(
    horizontal: Dp = 0.dp,
    vertical: Dp = 0.dp,
): Modifier = this.layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    fun Dp.toPxInt(): Int = this.toPx().toInt()

    layout(
        placeable.width - (horizontal * 2).toPxInt(),
        placeable.height - (vertical * 2).toPxInt()
    ) {
        placeable.placeRelative(-horizontal.toPx().toInt(), -vertical.toPx().toInt())
    }
}
