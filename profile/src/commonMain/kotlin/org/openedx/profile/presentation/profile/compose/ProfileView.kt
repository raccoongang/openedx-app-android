package org.openedx.profile.presentation.profile.compose

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import org.openedx.profile.*
import org.openedx.profile.Res as profileRes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.openedx.core.Res as coreRes
import org.openedx.core.core_profile
import org.openedx.core.ui.HandleUIMessage
import org.openedx.core.ui.OpenEdXOutlinedButton
import org.openedx.core.ui.Toolbar
import org.openedx.core.ui.displayCutoutForLandscape
import org.openedx.core.ui.statusBarsInset
import org.openedx.core.ui.theme.appColors
import org.openedx.foundation.presentation.UIMessage
import org.openedx.foundation.presentation.WindowSize
import org.openedx.foundation.presentation.windowSizeValue
import org.openedx.profile.presentation.profile.ProfileUIState
import org.openedx.profile.presentation.ui.ProfileInfoSection
import org.openedx.profile.presentation.ui.ProfileTopic

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileView(
    windowSize: WindowSize,
    uiState: ProfileUIState,
    uiMessage: UIMessage?,
    refreshing: Boolean,
    onAction: (ProfileViewAction) -> Unit,
    onSettingsClick: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val pullRefreshState = rememberPullToRefreshState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->

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

        HandleUIMessage(uiMessage = uiMessage, snackbarHostState = snackbarHostState)

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .statusBarsInset()
                .displayCutoutForLandscape(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Toolbar(
                label = stringResource(coreRes.string.core_profile),
                canShowSettingsIcon = true,
                onSettingsClick = onSettingsClick
            )

            Surface(
                color = MaterialTheme.appColors.background
            ) {
                PullToRefreshBox(
                    modifier = Modifier,
                    state = pullRefreshState,
                    isRefreshing = refreshing,
                    onRefresh = { onAction(ProfileViewAction.SwipeRefresh) }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when (uiState) {
                            is ProfileUIState.Loading -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = MaterialTheme.appColors.primary)
                                }
                            }

                            is ProfileUIState.Data -> {
                                Column(
                                    Modifier
                                        .fillMaxHeight()
                                        .then(contentWidth)
                                        .verticalScroll(rememberScrollState()),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(24.dp)
                                ) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    ProfileTopic(
                                        image = uiState.account.profileImage.imageUrlFull,
                                        title = uiState.account.name,
                                        subtitle = "@${uiState.account.username}"
                                    )
                                    ProfileInfoSection(uiState.account)
                                    OpenEdXOutlinedButton(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        text = stringResource(profileRes.string.profile_edit_profile),
                                        onClick = {
                                            onAction(ProfileViewAction.EditAccountClick)
                                        },
                                        borderColor = MaterialTheme.appColors.primaryButtonBackground,
                                        textColor = MaterialTheme.appColors.textAccent
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

interface ProfileViewAction {
    object EditAccountClick : ProfileViewAction
    object SwipeRefresh : ProfileViewAction
}
