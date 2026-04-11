package org.openedx.profile.presentation.manageaccount.compose

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
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.openedx.core.Res as coreRes
import org.openedx.core.core_manage_account
import org.openedx.core.ui.HandleUIMessage
import org.openedx.core.ui.IconText
import org.openedx.core.ui.OpenEdXOutlinedButton
import org.openedx.core.ui.Toolbar
import org.openedx.core.ui.displayCutoutForLandscape
import org.openedx.core.ui.settingsHeaderBackground
import org.openedx.core.ui.statusBarsInset
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appShapes
import org.openedx.core.ui.theme.appTypography
import org.openedx.foundation.presentation.UIMessage
import org.openedx.foundation.presentation.WindowSize
import org.openedx.foundation.presentation.windowSizeValue
import org.openedx.profile.presentation.manageaccount.ManageAccountUIState
import org.openedx.profile.presentation.ui.ProfileTopic
import org.openedx.profile.*
import org.openedx.profile.Res as profileRes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageAccountView(
    windowSize: WindowSize,
    uiState: ManageAccountUIState,
    uiMessage: UIMessage?,
    refreshing: Boolean,
    onAction: (ManageAccountViewAction) -> Unit
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

        val topBarWidth by remember(key1 = windowSize) {
            mutableStateOf(
                windowSize.windowSizeValue(
                    expanded = Modifier.widthIn(Dp.Unspecified, 560.dp),
                    compact = Modifier
                        .fillMaxWidth()
                )
            )
        }

        HandleUIMessage(uiMessage = uiMessage, snackbarHostState = snackbarHostState)

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .settingsHeaderBackground()
                .statusBarsInset(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.CenterEnd
            ) {
                Toolbar(
                    modifier = topBarWidth
                        .displayCutoutForLandscape(),
                    label = stringResource(coreRes.string.core_manage_account),
                    canShowBackBtn = true,
                    labelTint = MaterialTheme.appColors.settingsTitleContent,
                    iconTint = MaterialTheme.appColors.settingsTitleContent,
                    onBackClick = {
                        onAction(ManageAccountViewAction.BackClick)
                    }
                )
            }
            Surface(
                color = MaterialTheme.appColors.background,
                shape = MaterialTheme.appShapes.screenBackgroundShape,
            ) {
                PullToRefreshBox(
                    modifier = Modifier,
                    state = pullRefreshState,
                    isRefreshing = refreshing,
                    onRefresh = { onAction(ManageAccountViewAction.SwipeRefresh) }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .displayCutoutForLandscape(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when (uiState) {
                            is ManageAccountUIState.Loading -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = MaterialTheme.appColors.primary)
                                }
                            }

                            is ManageAccountUIState.Data -> {
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
                                        subtitle = uiState.account.email ?: ""
                                    )
                                    OpenEdXOutlinedButton(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        text = stringResource(profileRes.string.profile_edit_profile),
                                        onClick = {
                                            onAction(ManageAccountViewAction.EditAccountClick)
                                        },
                                        borderColor = MaterialTheme.appColors.primaryButtonBackground,
                                        textColor = MaterialTheme.appColors.textAccent
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    IconText(
                                        text = stringResource(profileRes.string.profile_delete_profile),
                                        painter = painterResource(profileRes.drawable.profile_ic_trash),
                                        textStyle = MaterialTheme.appTypography.labelLarge,
                                        color = MaterialTheme.appColors.error,
                                        onClick = {
                                            onAction(ManageAccountViewAction.DeleteAccount)
                                        }
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

interface ManageAccountViewAction {
    object EditAccountClick : ManageAccountViewAction
    object SwipeRefresh : ManageAccountViewAction
    object DeleteAccount : ManageAccountViewAction
    object BackClick : ManageAccountViewAction
}
