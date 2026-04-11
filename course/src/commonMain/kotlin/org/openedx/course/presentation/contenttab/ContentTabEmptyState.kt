package org.openedx.course.presentation.contenttab

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.openedx.core.ui.IconText
import org.openedx.core.ui.OpenEdXButton
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appTypography
import org.openedx.core.Res as coreRes
import org.openedx.core.core_no_assignments
import org.openedx.core.core_no_course_content
import org.openedx.core.core_no_videos
import org.openedx.course.*
import org.openedx.course.Res
import org.openedx.foundation.presentation.rememberWindowSize

@Composable
fun ContentTabEmptyState(
    modifier: Modifier = Modifier,
    message: String,
    onReturnToCourseClick: () -> Unit,
    showReturnButton: Boolean = true
) {
    val windowSize = rememberWindowSize()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!windowSize.isLandscape) {
            Icon(
                modifier = Modifier
                    .size(120.dp),
                painter = painterResource(Res.drawable.course_ic_warning),
                contentDescription = null,
                tint = MaterialTheme.appColors.textFieldHint
            )
            Spacer(Modifier.height(24.dp))
        }
        Text(
            modifier = Modifier.padding(horizontal = 24.dp),
            text = message,
            color = MaterialTheme.appColors.textPrimary,
            style = MaterialTheme.appTypography.bodyLarge,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        if (showReturnButton) {
            Spacer(Modifier.height(16.dp))
            OpenEdXButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                textColor = MaterialTheme.appColors.secondaryButtonText,
                backgroundColor = MaterialTheme.appColors.secondaryButtonBackground,
                onClick = onReturnToCourseClick
            ) {
                IconText(
                    text = stringResource(Res.string.course_return_to_course_home),
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    color = MaterialTheme.appColors.secondaryButtonText,
                    textStyle = MaterialTheme.appTypography.labelLarge
                )
            }
        }
    }
}

@Composable
fun CourseContentAllEmptyState(
    modifier: Modifier = Modifier,
    onReturnToCourseClick: () -> Unit,
    showReturnButton: Boolean = true
) {
    ContentTabEmptyState(
        modifier = modifier,
        message = stringResource(coreRes.string.core_no_course_content),
        onReturnToCourseClick = onReturnToCourseClick,
        showReturnButton = showReturnButton
    )
}

@Composable
fun CourseContentVideoEmptyState(
    modifier: Modifier = Modifier,
    onReturnToCourseClick: () -> Unit,
    showReturnButton: Boolean = true
) {
    ContentTabEmptyState(
        modifier = modifier,
        message = stringResource(coreRes.string.core_no_videos),
        onReturnToCourseClick = onReturnToCourseClick,
        showReturnButton = showReturnButton
    )
}

@Composable
fun CourseContentAssignmentEmptyState(
    modifier: Modifier = Modifier,
    onReturnToCourseClick: () -> Unit,
    showReturnButton: Boolean = true
) {
    ContentTabEmptyState(
        modifier = modifier,
        message = stringResource(coreRes.string.core_no_assignments),
        onReturnToCourseClick = onReturnToCourseClick,
        showReturnButton = showReturnButton
    )
}

@Composable
fun CourseHomeGradesEmptyState(
    modifier: Modifier = Modifier,
) {
    ContentTabEmptyState(
        modifier = modifier,
        message = stringResource(Res.string.course_progress_no_assignments),
        onReturnToCourseClick = {},
        showReturnButton = false
    )
}
