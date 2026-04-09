package org.openedx.core.presentation.settings.video

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource as androidStringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.openedx.core.domain.model.VideoQuality
import org.openedx.core.ui.Toolbar
import org.openedx.core.domain.model.desResId
import org.openedx.core.domain.model.titleResId
import org.openedx.core.ui.displayCutoutForLandscape
import org.openedx.core.ui.statusBarsInset
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appTypography
import org.openedx.foundation.extension.nonZero
import org.openedx.foundation.extension.tagId
import org.openedx.foundation.presentation.WindowSize
import org.openedx.foundation.presentation.WindowType
import org.openedx.foundation.presentation.windowSizeValue

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun VideoQualityScreen(
    windowSize: WindowSize,
    title: String,
    selectedVideoQuality: VideoQuality,
    onQualityChanged: (VideoQuality) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .semantics {
                testTagsAsResourceId = true
            },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->

        val topBarWidth by remember(key1 = windowSize) {
            mutableStateOf(
                windowSize.windowSizeValue(
                    expanded = Modifier.widthIn(Dp.Unspecified, 560.dp),
                    compact = Modifier
                        .fillMaxWidth()
                )
            )
        }

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

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .statusBarsInset()
                    .displayCutoutForLandscape(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Toolbar(
                    modifier = topBarWidth,
                    label = title,
                    canShowBackBtn = true,
                    onBackClick = onBackClick
                )

                Column(
                    modifier = Modifier
                        .then(contentWidth)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    VideoQuality.entries.forEach { videoQuality ->
                        QualityOption(
                            title = androidStringResource(id = videoQuality.titleResId),
                            description = videoQuality.desResId.nonZero()
                                ?.let { androidStringResource(id = videoQuality.desResId) } ?: "",
                            selected = selectedVideoQuality == videoQuality,
                            onClick = {
                                onQualityChanged(videoQuality)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QualityOption(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .testTag("btn_video_quality_${title.tagId()}")
            .fillMaxWidth()
            .height(90.dp)
            .clickable {
                onClick()
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                modifier = Modifier.testTag("txt_video_quality_title_${title.tagId()}"),
                text = title,
                color = MaterialTheme.appColors.textPrimary,
                style = MaterialTheme.appTypography.titleMedium
            )
            if (description.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    modifier = Modifier.testTag("txt_video_quality_description_${title.tagId()}"),
                    text = description,
                    color = MaterialTheme.appColors.textSecondary,
                    style = MaterialTheme.appTypography.labelMedium
                )
            }
        }
        if (selected) {
            Icon(
                modifier = Modifier.testTag("ic_video_quality_selected_${title.tagId()}"),
                imageVector = Icons.Filled.Done,
                tint = MaterialTheme.appColors.primary,
                contentDescription = null
            )
        }
    }
    HorizontalDivider()
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun VideoQualityScreenPreview() {
    OpenEdXTheme {
        VideoQualityScreen(
            windowSize = WindowSize(WindowType.Compact, WindowType.Compact),
            title = "",
            selectedVideoQuality = VideoQuality.OPTION_720P,
            onQualityChanged = {},
            onBackClick = {}
        )
    }
}
