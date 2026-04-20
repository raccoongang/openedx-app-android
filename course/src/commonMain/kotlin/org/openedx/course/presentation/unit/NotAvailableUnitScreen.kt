package org.openedx.course.presentation.unit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.openedx.core.Res as coreRes
import org.openedx.core.core_explore_other_parts_when_reconnect
import org.openedx.core.core_explore_other_parts_when_reconnect_or_download
import org.openedx.core.core_not_available_offline
import org.openedx.core.core_not_downloaded
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appShapes
import org.openedx.core.ui.theme.appTypography
import org.openedx.course.Res
import org.openedx.course.course_explore_other_parts_on_web
import org.openedx.course.course_ic_not_supported_block
import org.openedx.course.course_open_in_browser
import org.openedx.course.course_this_interactive_component
import org.openedx.foundation.presentation.WindowSize
import org.openedx.foundation.presentation.windowSizeValue

@Composable
fun NotAvailableUnitScreen(
    windowSize: WindowSize,
    unitType: NotAvailableUnitType,
    blockUrl: String = "",
) {
    val uriHandler = LocalUriHandler.current
    val title: String
    val description: String
    val buttonAction: (() -> Unit)?
    when (unitType) {
        NotAvailableUnitType.MOBILE_UNSUPPORTED -> {
            title = stringResource(Res.string.course_this_interactive_component)
            description = stringResource(Res.string.course_explore_other_parts_on_web)
            buttonAction = if (blockUrl.isNotEmpty()) ({ uriHandler.openUri(blockUrl) }) else null
        }
        NotAvailableUnitType.OFFLINE_UNSUPPORTED -> {
            title = stringResource(coreRes.string.core_not_available_offline)
            description = stringResource(coreRes.string.core_explore_other_parts_when_reconnect)
            buttonAction = null
        }
        NotAvailableUnitType.NOT_DOWNLOADED -> {
            title = stringResource(coreRes.string.core_not_downloaded)
            description =
                stringResource(coreRes.string.core_explore_other_parts_when_reconnect_or_download)
            buttonAction = null
        }
    }

    val scrollState = rememberScrollState()
    Scaffold(modifier = Modifier.fillMaxSize()) { paddings ->
        val contentWidth by remember(windowSize) {
            mutableStateOf(
                windowSize.windowSizeValue(
                    expanded = Modifier.width(326.dp),
                    compact = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                ),
            )
        }

        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .padding(paddings)
                    .then(contentWidth),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    modifier = Modifier.size(100.dp),
                    painter = painterResource(Res.drawable.course_ic_not_supported_block),
                    contentDescription = null,
                    tint = MaterialTheme.appColors.textPrimary,
                )
                Spacer(Modifier.height(36.dp))
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = title,
                    style = MaterialTheme.appTypography.titleLarge,
                    color = MaterialTheme.appColors.textPrimary,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = description,
                    style = MaterialTheme.appTypography.bodyLarge,
                    color = MaterialTheme.appColors.textPrimary,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(40.dp))
                if (buttonAction != null) {
                    Button(
                        modifier = Modifier
                            .width(216.dp)
                            .height(42.dp),
                        shape = MaterialTheme.appShapes.buttonShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.appColors.primaryButtonBackground,
                        ),
                        onClick = buttonAction,
                    ) {
                        Text(
                            text = stringResource(Res.string.course_open_in_browser),
                            color = MaterialTheme.appColors.primaryButtonText,
                            style = MaterialTheme.appTypography.labelLarge,
                        )
                    }
                    Spacer(Modifier.height(20.dp))
                }
            }
        }
    }
}
