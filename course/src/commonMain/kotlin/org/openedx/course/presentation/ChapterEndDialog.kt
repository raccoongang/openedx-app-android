package org.openedx.course.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.openedx.core.ui.AutoSizeText
import org.openedx.core.ui.OpenEdXButton
import org.openedx.core.ui.OpenEdXOutlinedButton
import org.openedx.core.ui.TextIcon
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appTypography
import org.openedx.course.*
import org.openedx.course.Res
import org.openedx.core.Res as coreRes
import org.openedx.core.core_cancel

@Composable
fun ChapterEndDialog(
    sectionName: String,
    nextSectionName: String,
    isVerticalNavigation: Boolean,
    onBackToOutlineClick: () -> Unit,
    onProceedClick: () -> Unit,
    onCancelClick: () -> Unit,
) {
    Dialog(onDismissRequest = onCancelClick) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.appColors.background,
            ),
            shape = MaterialTheme.shapes.large,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Close button row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    IconButton(onClick = onCancelClick) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(coreRes.string.core_cancel),
                            tint = MaterialTheme.appColors.textPrimary,
                        )
                    }
                }

                // Diamond icon
                Image(
                    modifier = Modifier.size(100.dp),
                    painter = painterResource(Res.drawable.course_id_diamond),
                    contentDescription = null,
                )

                Spacer(modifier = Modifier.height(36.dp))

                // "Good job!" title
                AutoSizeText(
                    text = stringResource(Res.string.course_good_job),
                    style = MaterialTheme.appTypography.titleLarge,
                    color = MaterialTheme.appColors.textPrimary,
                    maxLines = 1,
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Section finished text
                AutoSizeText(
                    text = stringResource(Res.string.course_section_finished, sectionName),
                    style = MaterialTheme.appTypography.bodyMedium,
                    color = MaterialTheme.appColors.textPrimaryVariant,
                    maxLines = 2,
                )

                Spacer(modifier = Modifier.height(36.dp))

                if (nextSectionName.isNotEmpty()) {
                    // "To proceed with..." text
                    Text(
                        text = stringResource(Res.string.course_to_proceed, nextSectionName),
                        style = MaterialTheme.appTypography.bodyMedium,
                        color = MaterialTheme.appColors.textPrimaryVariant,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // "Next section" button
                    OpenEdXButton(
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        onClick = onProceedClick,
                    ) {
                        val icon = if (isVerticalNavigation) {
                            Icons.Default.ArrowDownward
                        } else {
                            Icons.AutoMirrored.Filled.ArrowForward
                        }
                        TextIcon(
                            text = stringResource(Res.string.course_next_section),
                            icon = icon,
                            color = MaterialTheme.appColors.primaryButtonText,
                            textStyle = MaterialTheme.appTypography.labelLarge,
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // "Back to outline" button
                OpenEdXOutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = MaterialTheme.appColors.textAccent,
                    textColor = MaterialTheme.appColors.textAccent,
                    text = stringResource(Res.string.course_back_to_outline),
                    onClick = onBackToOutlineClick,
                )
            }
        }
    }
}
