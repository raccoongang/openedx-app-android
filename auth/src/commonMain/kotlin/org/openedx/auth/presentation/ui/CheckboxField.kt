package org.openedx.auth.presentation.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.openedx.core.ui.noRippleClickable
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appTypography

@Composable
internal fun CheckboxField(
    text: String,
    defaultValue: Boolean,
    onValueChanged: (Boolean) -> Unit
) {
    var checkedState by remember { mutableStateOf(defaultValue) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = checkedState,
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.appColors.primary,
                uncheckedColor = MaterialTheme.appColors.textFieldText
            ),
            onCheckedChange = {
                checkedState = it
                onValueChanged(it)
            }
        )
        Text(
            modifier = Modifier.noRippleClickable {
                checkedState = !checkedState
                onValueChanged(checkedState)
            },
            text = text,
            style = MaterialTheme.appTypography.bodySmall,
        )
    }
}

@Preview
@Composable
private fun CheckboxFieldPreview() {
    OpenEdXTheme {
        CheckboxField(
            text = "Test",
            defaultValue = true,
        ) {}
    }
}
