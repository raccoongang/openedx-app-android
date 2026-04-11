package org.openedx.core.presentation.global

import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.openedx.core.Res
import org.openedx.core.core_ic_unknown_error
import org.openedx.core.core_no_internet_connection
import org.openedx.core.core_no_internet_connection_description
import org.openedx.core.core_reload
import org.openedx.core.core_something_went_wrong_description
import org.openedx.core.core_try_again

enum class ErrorType(
    val iconResId: DrawableResource,
    val titleRes: StringResource,
    val descriptionRes: StringResource,
    val actionRes: StringResource,
) {
    CONNECTION_ERROR(
        iconResId = Res.drawable.core_no_internet_connection,
        titleRes = Res.string.core_no_internet_connection,
        descriptionRes = Res.string.core_no_internet_connection_description,
        actionRes = Res.string.core_reload,
    ),
    UNKNOWN_ERROR(
        iconResId = Res.drawable.core_ic_unknown_error,
        titleRes = Res.string.core_try_again,
        descriptionRes = Res.string.core_something_went_wrong_description,
        actionRes = Res.string.core_reload,
    ),
}
