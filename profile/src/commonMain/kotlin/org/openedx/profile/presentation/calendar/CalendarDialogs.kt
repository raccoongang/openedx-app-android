package org.openedx.profile.presentation.calendar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.openedx.core.Res as coreRes
import org.openedx.core.core_cancel
import org.openedx.core.core_ic_warning
import org.openedx.core.domain.model.CalendarData
import org.openedx.core.presentation.dialog.DefaultDialogBox
import org.openedx.core.ui.OpenEdXButton
import org.openedx.core.ui.OpenEdXOutlinedButton
import org.openedx.core.ui.TextIcon
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appShapes
import org.openedx.core.ui.theme.appTypography
import org.openedx.profile.Res as profileRes
import org.openedx.profile.profile_begin_syncing
import org.openedx.profile.profile_calendar_access_dialog_description
import org.openedx.profile.profile_calendar_access_dialog_title
import org.openedx.profile.profile_calendar_name
import org.openedx.profile.profile_change_sync_options
import org.openedx.profile.profile_local_calendar_option
import org.openedx.profile.profile_no_google_calendars
import org.openedx.profile.profile_select_calendar
import org.openedx.profile.profile_color
import org.openedx.profile.profile_course_dates
import org.openedx.profile.profile_deleting_events
import org.openedx.profile.profile_disable_calendar_dialog_description
import org.openedx.profile.profile_disable_calendar_dialog_title
import org.openedx.profile.profile_disable_syncing
import org.openedx.profile.profile_grant_access_calendar
import org.openedx.profile.profile_new_calendar
import org.openedx.profile.profile_new_calendar_description
import androidx.compose.ui.graphics.Color as ComposeColor

@Composable
fun CalendarAccessDialog(
    onCancelClick: () -> Unit,
    onGrantCalendarAccessClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    DefaultDialogBox(
        modifier = modifier,
        onDismissClick = onCancelClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Image(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(coreRes.drawable.core_ic_warning),
                    contentDescription = null,
                )
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(profileRes.string.profile_calendar_access_dialog_title),
                    style = MaterialTheme.appTypography.titleLarge,
                    color = MaterialTheme.appColors.textDark,
                )
            }
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(profileRes.string.profile_calendar_access_dialog_description),
                style = MaterialTheme.appTypography.bodyMedium,
                color = MaterialTheme.appColors.textDark,
            )
            OpenEdXButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onGrantCalendarAccessClick,
                content = {
                    TextIcon(
                        text = stringResource(profileRes.string.profile_grant_access_calendar),
                        icon = Icons.AutoMirrored.Filled.OpenInNew,
                        color = MaterialTheme.appColors.primaryButtonText,
                        textStyle = MaterialTheme.appTypography.labelLarge,
                        iconModifier = Modifier.padding(start = 4.dp),
                    )
                },
            )
            OpenEdXOutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(coreRes.string.core_cancel),
                backgroundColor = MaterialTheme.appColors.background,
                borderColor = MaterialTheme.appColors.primaryButtonBackground,
                textColor = MaterialTheme.appColors.primaryButtonBackground,
                onClick = onCancelClick,
            )
        }
    }
}

@Composable
fun DisableCalendarSyncDialog(
    calendarData: CalendarData?,
    isDeleting: Boolean,
    onCancelClick: () -> Unit,
    onDisableSyncingClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    DefaultDialogBox(
        modifier = modifier,
        onDismissClick = { if (!isDeleting) onCancelClick() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Image(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(coreRes.drawable.core_ic_warning),
                    contentDescription = null,
                )
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(profileRes.string.profile_disable_calendar_dialog_title),
                    style = MaterialTheme.appTypography.titleLarge,
                    color = MaterialTheme.appColors.textDark,
                )
            }
            calendarData?.let {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.appShapes.cardShape)
                        .background(MaterialTheme.appColors.cardViewBackground)
                        .padding(vertical = 16.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(ComposeColor(calendarData.color)),
                    )
                    Text(
                        text = calendarData.title,
                        style = MaterialTheme.appTypography.bodyMedium.copy(
                            textDecoration = TextDecoration.LineThrough,
                        ),
                        color = MaterialTheme.appColors.textDark,
                    )
                }
            }
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(
                    profileRes.string.profile_disable_calendar_dialog_description,
                    calendarData?.title.orEmpty(),
                ),
                style = MaterialTheme.appTypography.bodyMedium,
                color = MaterialTheme.appColors.textDark,
            )
            if (isDeleting) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.appColors.primary,
                    )
                    Text(
                        text = stringResource(profileRes.string.profile_deleting_events),
                        style = MaterialTheme.appTypography.bodyMedium,
                        color = MaterialTheme.appColors.textDark,
                    )
                }
            } else {
                OpenEdXOutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(profileRes.string.profile_disable_syncing),
                    backgroundColor = MaterialTheme.appColors.background,
                    borderColor = MaterialTheme.appColors.primaryButtonBackground,
                    textColor = MaterialTheme.appColors.primaryButtonBackground,
                    onClick = onDisableSyncingClick,
                )
                OpenEdXButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(coreRes.string.core_cancel),
                    onClick = onCancelClick,
                )
            }
        }
    }
}

private sealed class SelectedCalendar {
    data object Local : SelectedCalendar()
    data class Google(val calendar: org.openedx.core.domain.model.UserCalendar) : SelectedCalendar()
}

@Composable
fun NewCalendarDialog(
    dialogType: NewCalendarDialogType,
    platformName: String,
    googleCalendars: List<org.openedx.core.domain.model.UserCalendar>,
    showLocalCalendarSection: Boolean,
    onCancelClick: () -> Unit,
    onBeginSyncingClick: (calendarTitle: String, calendarColor: CalendarColor) -> Unit,
    onGoogleCalendarClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val title = when (dialogType) {
        NewCalendarDialogType.CREATE_NEW -> stringResource(profileRes.string.profile_new_calendar)
        NewCalendarDialogType.UPDATE -> stringResource(profileRes.string.profile_change_sync_options)
    }
    val defaultTitleSuffix = stringResource(profileRes.string.profile_course_dates)
    val defaultTitle = "$platformName $defaultTitleSuffix"
    var calendarTitle by remember { mutableStateOf("") }
    var calendarColor by remember { mutableStateOf(CalendarColor.ACCENT) }
    val initialSelection: SelectedCalendar? = remember(googleCalendars, showLocalCalendarSection) {
        when {
            googleCalendars.isEmpty() && showLocalCalendarSection -> SelectedCalendar.Local
            googleCalendars.isEmpty() && !showLocalCalendarSection -> null
            else -> null
        }
    }
    var selectedCalendar by remember { mutableStateOf(initialSelection) }

    DefaultDialogBox(
        modifier = modifier,
        onDismissClick = onCancelClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = title,
                    color = MaterialTheme.appColors.textDark,
                    style = MaterialTheme.appTypography.titleLarge,
                )
                Icon(
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onCancelClick() },
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = MaterialTheme.appColors.primary,
                )
            }
            CalendarPickerDropdown(
                calendars = googleCalendars,
                showLocalOption = showLocalCalendarSection,
                selected = selectedCalendar,
                onSelectLocal = { selectedCalendar = SelectedCalendar.Local },
                onSelectGoogle = { selectedCalendar = SelectedCalendar.Google(it) },
            )
            if (googleCalendars.isEmpty() && !showLocalCalendarSection) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(profileRes.string.profile_no_google_calendars),
                    style = MaterialTheme.appTypography.bodyMedium,
                    color = MaterialTheme.appColors.textFieldHint,
                )
            }
            if (selectedCalendar == SelectedCalendar.Local) {
                CalendarTitleField(
                    defaultTitle = defaultTitle,
                    onValueChange = { calendarTitle = it },
                )
                ColorDropdown(
                    current = calendarColor,
                    onChange = { calendarColor = it },
                )
            }
            if (selectedCalendar != null) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(profileRes.string.profile_new_calendar_description),
                    style = MaterialTheme.appTypography.bodyMedium,
                    color = MaterialTheme.appColors.textDark,
                )
                OpenEdXButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(profileRes.string.profile_begin_syncing),
                    onClick = {
                        when (val s = selectedCalendar) {
                            is SelectedCalendar.Google -> onGoogleCalendarClick(s.calendar.id)
                            SelectedCalendar.Local -> onBeginSyncingClick(
                                calendarTitle.ifEmpty { defaultTitle },
                                calendarColor,
                            )
                            null -> {}
                        }
                    },
                )
            }
            OpenEdXOutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(coreRes.string.core_cancel),
                backgroundColor = MaterialTheme.appColors.background,
                borderColor = MaterialTheme.appColors.primaryButtonBackground,
                textColor = MaterialTheme.appColors.primaryButtonBackground,
                onClick = onCancelClick,
            )
        }
    }
}

@Composable
private fun CalendarPickerDropdown(
    calendars: List<org.openedx.core.domain.model.UserCalendar>,
    showLocalOption: Boolean,
    selected: SelectedCalendar?,
    onSelectLocal: () -> Unit,
    onSelectGoogle: (org.openedx.core.domain.model.UserCalendar) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val label = when (selected) {
        SelectedCalendar.Local -> stringResource(profileRes.string.profile_local_calendar_option)
        is SelectedCalendar.Google -> selected.calendar.title
        null -> stringResource(profileRes.string.profile_select_calendar)
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(MaterialTheme.appShapes.textFieldShape)
                .border(
                    1.dp,
                    MaterialTheme.appColors.textFieldBorder,
                    MaterialTheme.appShapes.textFieldShape,
                )
                .clickable { expanded = true },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                text = label,
                color = MaterialTheme.appColors.textDark,
                style = MaterialTheme.appTypography.bodyMedium,
            )
            Icon(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .rotate(if (expanded) 180f else 0f),
                imageVector = Icons.Default.ExpandMore,
                tint = MaterialTheme.appColors.textDark,
                contentDescription = null,
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.appColors.background),
        ) {
            if (showLocalOption) {
                DropdownMenuItem(
                    text = { Text(stringResource(profileRes.string.profile_local_calendar_option)) },
                    onClick = {
                        expanded = false
                        onSelectLocal()
                    },
                )
                if (calendars.isNotEmpty()) HorizontalDivider()
            }
            calendars.forEach { userCalendar ->
                DropdownMenuItem(
                    text = { Text(userCalendar.title) },
                    onClick = {
                        expanded = false
                        onSelectGoogle(userCalendar)
                    },
                )
            }
        }
    }
}

@Composable
private fun CalendarTitleField(
    defaultTitle: String,
    onValueChange: (String) -> Unit,
) {
    var text by remember { mutableStateOf("") }
    Column {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(profileRes.string.profile_calendar_name),
            color = MaterialTheme.appColors.textPrimary,
            style = MaterialTheme.appTypography.labelLarge,
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().height(48.dp),
            value = text,
            onValueChange = {
                if (it.length <= 40) text = it
                onValueChange(it.trim())
            },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.appColors.textFieldBorder,
                focusedTextColor = MaterialTheme.appColors.textPrimary,
                unfocusedTextColor = MaterialTheme.appColors.textPrimary,
            ),
            shape = MaterialTheme.appShapes.textFieldShape,
            placeholder = {
                Text(
                    text = defaultTitle,
                    color = MaterialTheme.appColors.textFieldHint,
                    style = MaterialTheme.appTypography.bodyMedium,
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(),
            textStyle = MaterialTheme.appTypography.bodyMedium,
            singleLine = true,
        )
    }
}

@Composable
private fun ColorDropdown(
    current: CalendarColor,
    onChange: (CalendarColor) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var width by remember { mutableStateOf(300.dp) }

    Column {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(profileRes.string.profile_color),
            color = MaterialTheme.appColors.textPrimary,
            style = MaterialTheme.appTypography.labelLarge,
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(MaterialTheme.appShapes.textFieldShape)
                .border(
                    1.dp,
                    MaterialTheme.appColors.textFieldBorder,
                    MaterialTheme.appShapes.textFieldShape,
                )
                .clickable { expanded = true },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(ComposeColor(current.color)),
            )
            Text(
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                text = stringResource(current.title),
                color = MaterialTheme.appColors.textDark,
                style = MaterialTheme.appTypography.bodyMedium,
            )
            Icon(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .rotate(if (expanded) 180f else 0f),
                imageVector = Icons.Default.ExpandMore,
                tint = MaterialTheme.appColors.textDark,
                contentDescription = null,
            )
        }
        DropdownMenu(
            modifier = Modifier
                .width(width)
                .background(MaterialTheme.appColors.background),
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            CalendarColor.entries.forEachIndexed { index, color ->
                DropdownMenuItem(
                    modifier = Modifier.background(MaterialTheme.appColors.background),
                    onClick = {
                        expanded = false
                        onChange(color)
                    },
                    text = {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(
                                Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(ComposeColor(color.color)),
                            )
                            Text(
                                text = stringResource(color.title),
                                style = MaterialTheme.appTypography.titleSmall,
                                color = MaterialTheme.appColors.textDark,
                            )
                        }
                    },
                )
                if (index < CalendarColor.entries.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.appColors.divider,
                    )
                }
            }
        }
    }
}
