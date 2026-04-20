package org.openedx.core.presentation.dialog.selectorbottomsheet

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import org.jetbrains.compose.resources.stringResource
import org.openedx.core.Res
import org.openedx.core.core_select_value
import org.openedx.core.domain.model.RegistrationField
import org.openedx.core.ui.SheetContent

/**
 * Reusable replacement for Android's `SelectBottomDialogFragment` — shows a searchable list of
 * `RegistrationField.Option` values in a bottom sheet. Caller owns visibility state.
 *
 * Used in SignUp (country / gender selectors), EditProfile (same fields), VideoUnit
 * (transcript language). Matches the Android SheetContent API so call sites can migrate
 * without changes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectBottomSheet(
    values: List<RegistrationField.Option>,
    onItemClick: (RegistrationField.Option) -> Unit,
    onDismiss: () -> Unit,
    title: String = stringResource(Res.string.core_select_value),
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val listState = rememberLazyListState()
    var searchValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        SheetContent(
            searchValue = searchValue,
            title = title,
            expandedList = values,
            onItemClick = { item ->
                onItemClick(item)
                onDismiss()
            },
            listState = listState,
            searchValueChanged = { searchValue = TextFieldValue(it) },
        )
    }
}
