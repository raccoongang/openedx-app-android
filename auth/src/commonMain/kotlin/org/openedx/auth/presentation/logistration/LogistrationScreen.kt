package org.openedx.auth.presentation.logistration

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import org.openedx.auth.*
import org.openedx.core.ui.AuthButtonsPanel
import org.openedx.core.ui.SearchBar
import org.openedx.core.ui.displayCutoutForLandscape
import org.openedx.core.ui.noRippleClickable
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appTypography
import org.openedx.core.ui.theme.compose.LogistrationLogoView

@Composable
fun LogistrationScreen(
    onSearchClick: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onSignInClick: () -> Unit,
    isRegistrationEnabled: Boolean,
) {
    var textFieldValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }
    val scrollState = rememberScrollState()
    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        containerColor = MaterialTheme.appColors.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        Surface(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .navigationBarsPadding()
                .displayCutoutForLandscape(),
            color = MaterialTheme.appColors.background
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 32.dp,
                )
            ) {
                LogistrationLogoView()
                Text(
                    text = stringResource(Res.string.pre_auth_title),
                    style = MaterialTheme.appTypography.headlineSmall,
                    modifier = Modifier
                        .testTag("txt_screen_title")
                        .padding(bottom = 40.dp)
                )
                val focusManager = LocalFocusManager.current
                Column(Modifier.padding(bottom = 8.dp)) {
                    Text(
                        modifier = Modifier
                            .testTag("txt_search_label")
                            .padding(bottom = 10.dp),
                        style = MaterialTheme.appTypography.titleMedium,
                        text = stringResource(Res.string.pre_auth_search_title),
                    )
                    SearchBar(
                        modifier = Modifier
                            .testTag("tf_discovery_search")
                            .fillMaxWidth()
                            .height(48.dp),
                        label = stringResource(Res.string.pre_auth_search_hint),
                        requestFocus = false,
                        searchValue = textFieldValue,
                        clearOnSubmit = true,
                        keyboardActions = {
                            focusManager.clearFocus()
                            onSearchClick(textFieldValue.text)
                        },
                        onValueChanged = { text ->
                            textFieldValue = text
                        },
                        onClearValue = {
                            textFieldValue = TextFieldValue("")
                        }
                    )
                }

                Text(
                    modifier = Modifier
                        .testTag("txt_explore_all_courses")
                        .padding(bottom = 32.dp)
                        .noRippleClickable {
                            onSearchClick("")
                        },
                    text = stringResource(Res.string.pre_auth_explore_all_courses),
                    color = MaterialTheme.appColors.primary,
                    style = MaterialTheme.appTypography.labelLarge,
                    textDecoration = TextDecoration.Underline
                )

                Spacer(modifier = Modifier.weight(1f))

                AuthButtonsPanel(
                    onRegisterClick = onRegisterClick,
                    onSignInClick = onSignInClick,
                    showRegisterButton = isRegistrationEnabled
                )
            }
        }
    }
}

