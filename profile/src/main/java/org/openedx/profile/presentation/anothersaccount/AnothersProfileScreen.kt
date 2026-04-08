package org.openedx.profile.presentation.anothersaccount

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.openedx.core.R
import org.openedx.core.ui.BackBtn
import org.openedx.core.ui.HandleUIMessage
import org.openedx.core.ui.statusBarsInset
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appTypography
import org.openedx.foundation.presentation.UIMessage
import org.openedx.foundation.presentation.WindowSize
import org.openedx.foundation.presentation.WindowType
import org.openedx.foundation.presentation.windowSizeValue
import org.openedx.profile.ProfileMocks
import org.openedx.profile.presentation.ui.ProfileInfoSection
import org.openedx.profile.presentation.ui.ProfileTopic

@Composable
fun AnothersProfileScreen(
    windowSize: WindowSize,
    uiState: AnothersProfileUIState,
    uiMessage: UIMessage?,
    onBackClick: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val contentWidth by remember(key1 = windowSize) {
        mutableStateOf(
            windowSize.windowSizeValue(
                expanded = Modifier.widthIn(Dp.Unspecified, 420.dp),
                compact = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )
        )
    }

    val topBarWidth by remember(key1 = windowSize) {
        mutableStateOf(
            windowSize.windowSizeValue(
                expanded = Modifier.widthIn(Dp.Unspecified, 560.dp),
                compact = Modifier
                    .fillMaxWidth()
            )
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .statusBarsInset(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .then(topBarWidth),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(id = R.string.core_profile),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.appTypography.titleMedium
                    )
                    BackBtn {
                        onBackClick()
                    }
                }
            }
        }
    ) { paddingValues ->
        HandleUIMessage(uiMessage = uiMessage, snackbarHostState = snackbarHostState)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .background(MaterialTheme.appColors.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (uiState) {
                is AnothersProfileUIState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.appColors.primary)
                    }
                }

                is AnothersProfileUIState.Data -> {
                    Column(
                        Modifier
                            .fillMaxHeight()
                            .then(contentWidth)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ProfileTopic(
                            image = uiState.account.profileImage.imageUrlFull,
                            title = uiState.account.name,
                            subtitle = uiState.account.username
                        )

                        Spacer(modifier = Modifier.height(36.dp))

                        ProfileInfoSection(uiState.account)

                        Spacer(modifier = Modifier.height(36.dp))
                    }
                }
            }
        }
    }
}

@Preview(uiMode = UI_MODE_NIGHT_NO)
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Preview(name = "NEXUS_5_Light", device = Devices.NEXUS_5, uiMode = UI_MODE_NIGHT_NO)
@Preview(name = "NEXUS_5_Dark", device = Devices.NEXUS_5, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun ProfileScreenPreview() {
    OpenEdXTheme {
        AnothersProfileScreen(
            windowSize = WindowSize(WindowType.Compact, WindowType.Compact),
            uiState = AnothersProfileUIState.Data(ProfileMocks.account),
            uiMessage = null,
            onBackClick = {}
        )
    }
}

@Preview(name = "NEXUS_9_Light", device = Devices.NEXUS_9, uiMode = UI_MODE_NIGHT_NO)
@Preview(name = "NEXUS_9_Dark", device = Devices.NEXUS_9, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun ProfileScreenTabletPreview() {
    OpenEdXTheme {
        AnothersProfileScreen(
            windowSize = WindowSize(WindowType.Medium, WindowType.Medium),
            uiState = AnothersProfileUIState.Data(ProfileMocks.account),
            uiMessage = null,
            onBackClick = {}
        )
    }
}
