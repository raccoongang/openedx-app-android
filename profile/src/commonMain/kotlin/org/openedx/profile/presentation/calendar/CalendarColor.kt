package org.openedx.profile.presentation.calendar

import org.jetbrains.compose.resources.StringResource
import org.openedx.profile.Res as profileRes
import org.openedx.profile.calendar_color_accent
import org.openedx.profile.calendar_color_blue
import org.openedx.profile.calendar_color_brown
import org.openedx.profile.calendar_color_green
import org.openedx.profile.calendar_color_orange
import org.openedx.profile.calendar_color_purple
import org.openedx.profile.calendar_color_red
import org.openedx.profile.calendar_color_yellow

private const val ACCENT_COLOR = 0xFFD13329L
private const val RED_COLOR = 0xFFFF2967L
private const val ORANGE_COLOR = 0xFFFF9501L
private const val YELLOW_COLOR = 0xFFFFCC01L
private const val GREEN_COLOR = 0xFF64DA38L
private const val BLUE_COLOR = 0xFF1AAEF8L
private const val PURPLE_COLOR = 0xFFCC73E1L
private const val BROWN_COLOR = 0xFFA2845EL

enum class CalendarColor(
    val title: StringResource,
    val color: Long
) {
    ACCENT(profileRes.string.calendar_color_accent, ACCENT_COLOR),
    RED(profileRes.string.calendar_color_red, RED_COLOR),
    ORANGE(profileRes.string.calendar_color_orange, ORANGE_COLOR),
    YELLOW(profileRes.string.calendar_color_yellow, YELLOW_COLOR),
    GREEN(profileRes.string.calendar_color_green, GREEN_COLOR),
    BLUE(profileRes.string.calendar_color_blue, BLUE_COLOR),
    PURPLE(profileRes.string.calendar_color_purple, PURPLE_COLOR),
    BROWN(profileRes.string.calendar_color_brown, BROWN_COLOR)
}
