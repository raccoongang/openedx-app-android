package org.openedx.profile.presentation.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.Report
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import org.openedx.core.domain.model.LanguageProficiency
import org.openedx.core.domain.model.RegistrationField
import org.openedx.core.ui.AutoSizeText
import org.openedx.core.ui.BackBtn
import org.openedx.core.ui.HandleUIMessage
import org.openedx.core.ui.IconText
import org.openedx.core.ui.OpenEdXButton
import org.openedx.core.ui.OpenEdXOutlinedButton
import org.openedx.core.ui.SheetContent
import org.openedx.core.ui.displayCutoutForLandscape
import org.openedx.core.ui.noRippleClickable
import org.openedx.core.ui.rememberSaveableMap
import org.openedx.core.ui.statusBarsInset
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appShapes
import org.openedx.core.ui.theme.appTypography
import org.openedx.core.utils.LocaleUtils
import org.openedx.foundation.extension.tagId
import org.openedx.foundation.presentation.UIMessage
import org.openedx.foundation.presentation.WindowSize
import org.openedx.foundation.presentation.windowSizeValue
import org.openedx.profile.*
import org.openedx.profile.Res as profileRes
import org.openedx.profile.domain.model.Account
import org.openedx.core.Res as coreRes
import org.openedx.core.core_accessibility_user_profile_image
import org.openedx.core.core_cancel
import org.openedx.core.core_ic_default_profile_picture

private const val BIO_TEXT_FIELD_LIMIT = 300
private const val LEAVE_PROFILE_WIDTH_FACTOR = 0.7f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    windowSize: WindowSize,
    uiState: EditProfileUIState,
    uiMessage: UIMessage?,
    selectedImageUri: String?,
    isImageDeleted: Boolean,
    leaveDialog: Boolean,
    onKeepEdit: () -> Unit,
    onDataChanged: (Boolean) -> Unit,
    onLimitedProfileChange: (Boolean) -> Unit,
    onBackClick: (Boolean) -> Unit,
    onSaveClick: (Map<String, Any?>) -> Unit,
    onSelectImageClick: () -> Unit,
    onDeleteImageClick: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutine = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val keyboardController = LocalSoftwareKeyboardController.current
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    var expandedList by rememberSaveable { mutableStateOf(emptyList<RegistrationField.Option>()) }
    var openWarningMessageDialog by rememberSaveable { mutableStateOf(false) }
    var bottomDialogTitle by rememberSaveable { mutableStateOf("") }
    var searchValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }

    val mapFields = rememberSaveableMap {
        mutableStateMapOf(
            Pair(
                YEAR_OF_BIRTH,
                if (uiState.account.yearOfBirth != null) uiState.account.yearOfBirth.toString() else ""
            ),
            Pair(LANGUAGE, uiState.account.languageProficiencies),
            Pair(COUNTRY, uiState.account.country),
            Pair(BIO, uiState.account.bio),
            Pair(ACCOUNT_PRIVACY, uiState.account.accountPrivacy.name.lowercase())
        )
    }

    val saveButtonEnabled = !(
            uiState.account.yearOfBirth.toString() == mapFields[YEAR_OF_BIRTH] &&
                    uiState.account.languageProficiencies == mapFields[LANGUAGE] &&
                    uiState.account.country == mapFields[COUNTRY] &&
                    uiState.account.bio == mapFields[BIO] &&
                    selectedImageUri == null &&
                    !isImageDeleted &&
                    uiState.isLimited == uiState.account.isLimited()
            )
    onDataChanged(saveButtonEnabled)

    val serverFieldName = rememberSaveable {
        mutableStateOf("")
    }

    val imageRes: Any? = if (!isImageDeleted) {
        when {
            selectedImageUri != null -> {
                selectedImageUri
            }

            uiState.account.profileImage.hasImage -> {
                uiState.account.profileImage.imageUrlFull
            }

            else -> {
                null
            }
        }
    } else {
        null
    }

    val modalListState = rememberLazyListState()
    var isOpenChangeImageDialogState by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(showBottomSheet) {
        if (!showBottomSheet) {
            focusManager.clearFocus()
            searchValue = TextFieldValue()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
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

        val popUpModifier by remember(key1 = windowSize) {
            mutableStateOf(
                windowSize.windowSizeValue(
                    expanded = Modifier
                        .widthIn(Dp.Unspecified, 560.dp)
                        .padding(bottom = 86.dp),
                    compact = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 60.dp)
                        .padding(horizontal = 24.dp)
                )
            )
        }

        HandleUIMessage(uiMessage = uiMessage, snackbarHostState = snackbarHostState)

        if (isOpenChangeImageDialogState && uiState.account.isOlderThanMinAge()) {
            ChangeImageDialog(
                onSelectFromGalleryClick = {
                    isOpenChangeImageDialogState = false
                    onSelectImageClick()
                },
                onRemoveImageClick = {
                    onDeleteImageClick()
                    isOpenChangeImageDialogState = false
                },
                onCancelClick = {
                    isOpenChangeImageDialogState = false
                }
            )
        } else {
            isOpenChangeImageDialogState = false
        }

        if (leaveDialog) {
            if (!windowSize.isLandscape || windowSize.isTablet) {
                LeaveProfile(
                    onDismissRequest = { onKeepEdit() },
                    onLeaveClick = { onBackClick(false) }
                )
            } else {
                LeaveProfileLandscape(
                    onDismissRequest = { onKeepEdit() },
                    onLeaveClick = { onBackClick(false) }
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .statusBarsInset()
                .displayCutoutForLandscape(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = topBarWidth,
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    modifier = Modifier
                        .testTag("txt_edit_profile_title")
                        .fillMaxWidth(),
                    text = stringResource(profileRes.string.profile_edit_profile),
                    color = MaterialTheme.appColors.textPrimary,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.appTypography.titleMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    BackBtn(modifier = Modifier.padding(end = 16.dp)) {
                        onBackClick(saveButtonEnabled)
                    }
                    if (saveButtonEnabled) {
                        IconText(
                            modifier = Modifier
                                .height(48.dp)
                                .padding(end = 24.dp),
                            text = stringResource(profileRes.string.profile_done),
                            icon = Icons.Filled.Done,
                            color = MaterialTheme.appColors.primary,
                            textStyle = MaterialTheme.appTypography.labelLarge,
                            onClick = {
                                onSaveClick(mapFields.toMap())
                            }
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier,
                color = MaterialTheme.appColors.background,
                shape = MaterialTheme.appShapes.screenBackgroundShape
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Column(
                        Modifier
                            .fillMaxHeight()
                            .then(contentWidth)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            modifier = Modifier.testTag("txt_edit_profile_type_label"),
                            text = stringResource(
                                if (uiState.isLimited) {
                                    profileRes.string.profile_limited_profile
                                } else {
                                    profileRes.string.profile_full_profile
                                }
                            ),
                            color = MaterialTheme.appColors.textSecondary,
                            style = MaterialTheme.appTypography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Box(contentAlignment = Alignment.BottomEnd) {
                            AsyncImage(
                                model = imageRes,
                                contentScale = ContentScale.Crop,
                                contentDescription = stringResource(
                                    coreRes.string.core_accessibility_user_profile_image,
                                    uiState.account.username
                                ),
                                placeholder = painterResource(coreRes.drawable.core_ic_default_profile_picture),
                                error = painterResource(coreRes.drawable.core_ic_default_profile_picture),
                                fallback = painterResource(coreRes.drawable.core_ic_default_profile_picture),
                                modifier = Modifier
                                    .testTag("img_edit_profile_user_image")
                                    .border(
                                        2.dp,
                                        MaterialTheme.appColors.onSurface,
                                        CircleShape
                                    )
                                    .padding(2.dp)
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .noRippleClickable {
                                        isOpenChangeImageDialogState = true
                                        if (!uiState.account.isOlderThanMinAge()) {
                                            openWarningMessageDialog = true
                                        }
                                    }
                            )
                            Icon(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.appColors.primary)
                                    .padding(5.dp)
                                    .clickable {
                                        isOpenChangeImageDialogState = true
                                        if (!uiState.account.isOlderThanMinAge()) {
                                            openWarningMessageDialog = true
                                        }
                                    },
                                painter = painterResource(profileRes.drawable.profile_ic_edit_image),
                                contentDescription = null,
                                tint = MaterialTheme.appColors.onPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            modifier = Modifier.testTag("txt_edit_profile_user_name"),
                            text = uiState.account.name,
                            style = MaterialTheme.appTypography.headlineSmall,
                            color = MaterialTheme.appColors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            modifier = Modifier
                                .testTag("txt_edit_profile_limited_profile_label")
                                .clickable {
                                    if (!LocaleUtils.isProfileLimited(mapFields[YEAR_OF_BIRTH].toString())) {
                                        val privacy = if (uiState.isLimited) {
                                            Account.Privacy.ALL_USERS
                                        } else {
                                            Account.Privacy.PRIVATE
                                        }
                                        mapFields[ACCOUNT_PRIVACY] = privacy
                                        onLimitedProfileChange(!uiState.isLimited)
                                    } else {
                                        openWarningMessageDialog = true
                                    }
                                },
                            text = stringResource(
                                if (uiState.isLimited) {
                                    profileRes.string.profile_switch_to_full
                                } else {
                                    profileRes.string.profile_switch_to_limited
                                }
                            ),
                            color = MaterialTheme.appColors.textAccent,
                            style = MaterialTheme.appTypography.labelLarge
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        ProfileFields(
                            disabled = uiState.isLimited,
                            onFieldClick = { field, title ->
                                when (field) {
                                    YEAR_OF_BIRTH -> {
                                        serverFieldName.value = YEAR_OF_BIRTH
                                        expandedList = LocaleUtils.getBirthYearsRange()
                                    }

                                    COUNTRY -> {
                                        serverFieldName.value = COUNTRY
                                        expandedList = LocaleUtils.getCountries()
                                    }

                                    LANGUAGE -> {
                                        serverFieldName.value = LANGUAGE
                                        expandedList = LocaleUtils.getLanguages()
                                    }
                                }
                                bottomDialogTitle = title
                                keyboardController?.hide()
                                coroutine.launch {
                                    val index = expandedList.indexOfFirst { option ->
                                        if (serverFieldName.value == LANGUAGE) {
                                            option.value ==
                                                    (mapFields[serverFieldName.value] as List<LanguageProficiency>)
                                                        .getOrNull(0)?.code
                                        } else {
                                            option.value == mapFields[serverFieldName.value]
                                        }
                                    }
                                    showBottomSheet = true
                                    modalListState.scrollToItem(if (index > 0) index else 0)
                                }
                            },
                            onValueChanged = {
                                mapFields[BIO] = it
                            },
                            mapFields = mapFields,
                            onDoneClick = { onSaveClick(mapFields.toMap()) }
                        )
                        Spacer(Modifier.height(52.dp))
                    }
                    if (openWarningMessageDialog) {
                        LimitedProfileDialog(
                            modifier = popUpModifier
                        ) {
                            openWarningMessageDialog = false
                        }
                    }
                }
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            shape = MaterialTheme.appShapes.screenBackgroundShape,
            containerColor = MaterialTheme.appColors.background,
            scrimColor = Color.Black.copy(alpha = 0.4f),
        ) {
            SheetContent(
                title = bottomDialogTitle,
                searchValue = searchValue,
                expandedList = expandedList,
                listState = modalListState,
                onItemClick = { item ->
                    if (serverFieldName.value == LANGUAGE) {
                        mapFields[serverFieldName.value] = listOf(LanguageProficiency(item.value))
                    } else {
                        mapFields[serverFieldName.value] = item.value
                    }
                    coroutine.launch {
                        sheetState.hide()
                        modalListState.scrollToItem(0)
                        showBottomSheet = false
                    }
                },
                searchValueChanged = { searchValue = TextFieldValue(it) }
            )
        }
    }
}

@Composable
fun LimitedProfileDialog(
    modifier: Modifier,
    onCloseClick: () -> Unit
) {
    val tint = MaterialTheme.appColors.textWarning
    Column(
        modifier
            .shadow(
                2.dp,
                MaterialTheme.appShapes.material3.medium
            )
            .background(
                MaterialTheme.appColors.warning,
                MaterialTheme.appShapes.material3.medium
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Report,
                contentDescription = null,
                tint = tint
            )
            Text(
                modifier = Modifier
                    .testTag("txt_edit_profile_limited_profile_message")
                    .weight(1f),
                text = stringResource(profileRes.string.profile_must_be_over),
                color = tint,
                style = MaterialTheme.appTypography.labelLarge
            )
            Icon(
                modifier = Modifier
                    .testTag("ic_edit_profile_limited_profile_close")
                    .clickable { onCloseClick() },
                imageVector = Icons.Filled.Close,
                contentDescription = null,
                tint = tint
            )
        }
    }
}

@Composable
fun ChangeImageDialog(
    onSelectFromGalleryClick: () -> Unit,
    onRemoveImageClick: () -> Unit,
    onCancelClick: () -> Unit,
) {
    Dialog(
        onDismissRequest = { onCancelClick() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp)
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.appColors.cardViewBackground,
                            MaterialTheme.appShapes.cardShape
                        )
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(4.dp))
                    HorizontalDivider(
                        modifier = Modifier
                            .width(32.dp)
                            .background(
                                MaterialTheme.appColors.bottomSheetToggle,
                                MaterialTheme.appShapes.material3.small
                            )
                            .clip(MaterialTheme.appShapes.material3.small),
                        thickness = 4.dp
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        modifier = Modifier.testTag("txt_edit_profile_change_image_title"),
                        text = stringResource(profileRes.string.profile_change_image),
                        style = MaterialTheme.appTypography.titleLarge,
                        color = MaterialTheme.appColors.textPrimary
                    )
                    Spacer(Modifier.height(20.dp))
                    OpenEdXButton(
                        text = stringResource(profileRes.string.profile_select_from_gallery),
                        onClick = onSelectFromGalleryClick,
                        content = {
                            IconText(
                                modifier = Modifier.testTag("it_select_from_gallery"),
                                text = stringResource(profileRes.string.profile_select_from_gallery),
                                painter = painterResource(profileRes.drawable.profile_ic_gallery),
                                color = Color.White,
                                textStyle = MaterialTheme.appTypography.labelLarge
                            )
                        }
                    )
                    Spacer(Modifier.height(16.dp))
                    OpenEdXOutlinedButton(
                        borderColor = MaterialTheme.appColors.error,
                        textColor = MaterialTheme.appColors.textPrimary,
                        text = stringResource(profileRes.string.profile_remove_photo),
                        onClick = onRemoveImageClick,
                        content = {
                            IconText(
                                modifier = Modifier.testTag("it_remove_photo"),
                                text = stringResource(profileRes.string.profile_remove_photo),
                                painter = painterResource(profileRes.drawable.profile_ic_remove_image),
                                color = MaterialTheme.appColors.error,
                                textStyle = MaterialTheme.appTypography.labelLarge
                            )
                        }
                    )
                    Spacer(Modifier.height(40.dp))
                    OpenEdXOutlinedButton(
                        borderColor = MaterialTheme.appColors.textPrimaryVariant,
                        textColor = MaterialTheme.appColors.textPrimary,
                        text = stringResource(coreRes.string.core_cancel),
                        onClick = onCancelClick
                    )
                    Spacer(Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
fun ProfileFields(
    disabled: Boolean,
    mapFields: MutableMap<String, Any?>,
    onFieldClick: (String, String) -> Unit,
    onValueChanged: (String) -> Unit,
    onDoneClick: () -> Unit
) {
    val languageProficiency = (mapFields[LANGUAGE] as List<LanguageProficiency>)
    val lang = if (languageProficiency.isNotEmpty()) {
        LocaleUtils.getLanguageByLanguageCode(languageProficiency[0].code)
    } else {
        ""
    }
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        val yearTitle = stringResource(profileRes.string.profile_year)
        val locationTitle = stringResource(profileRes.string.profile_location)
        val spokenLanguageTitle = stringResource(profileRes.string.profile_spoken_language)
        SelectableField(
            name = yearTitle,
            initialValue = mapFields[YEAR_OF_BIRTH].toString(),
            onClick = {
                onFieldClick(YEAR_OF_BIRTH, yearTitle)
            }
        )
        if (!disabled) {
            SelectableField(
                name = locationTitle,
                initialValue = LocaleUtils.getCountryByCountryCode(mapFields[COUNTRY].toString()),
                onClick = {
                    onFieldClick(COUNTRY, locationTitle)
                }
            )
            SelectableField(
                name = spokenLanguageTitle,
                initialValue = lang,
                onClick = {
                    onFieldClick(
                        LANGUAGE,
                        spokenLanguageTitle
                    )
                }
            )
            InputEditField(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(132.dp),
                name = stringResource(profileRes.string.profile_about_me),
                initialValue = mapFields[BIO].toString(),
                onValueChanged = {
                    onValueChanged(it.take(BIO_TEXT_FIELD_LIMIT))
                },
                onDoneClick = onDoneClick
            )
        }
    }
}

@Composable
fun SelectableField(
    name: String,
    initialValue: String?,
    disabled: Boolean = false,
    onClick: () -> Unit,
) {
    val colors = if (disabled) {
        OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.appColors.textFieldBorder,
            unfocusedContainerColor = MaterialTheme.appColors.textFieldBackground
        )
    } else {
        OutlinedTextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.appColors.textFieldText,
            unfocusedTextColor = MaterialTheme.appColors.textFieldText,
            unfocusedContainerColor = MaterialTheme.appColors.textFieldBackground,
            focusedContainerColor = MaterialTheme.appColors.textFieldBackground,
            unfocusedBorderColor = MaterialTheme.appColors.textFieldBorder,
            cursorColor = MaterialTheme.appColors.textFieldText,
            disabledBorderColor = MaterialTheme.appColors.textFieldBorder,
            disabledTextColor = MaterialTheme.appColors.textFieldHint,
            disabledPlaceholderColor = MaterialTheme.appColors.textFieldHint
        )
    }
    Column {
        Text(
            modifier = Modifier
                .testTag("txt_label_${name.tagId()}")
                .fillMaxWidth(),
            text = name,
            style = MaterialTheme.appTypography.labelLarge,
            color = MaterialTheme.appColors.textPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            readOnly = true,
            enabled = false,
            value = initialValue ?: "",
            colors = colors,
            shape = MaterialTheme.appShapes.textFieldShape,
            textStyle = MaterialTheme.appTypography.bodyMedium,
            onValueChange = { },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.appColors.textPrimaryVariant
                )
            },
            modifier = Modifier
                .testTag("tf_select_${name.tagId()}")
                .fillMaxWidth()
                .noRippleClickable {
                    onClick()
                },
            placeholder = {
                Text(
                    modifier = Modifier.testTag("txt_placeholder_${name.tagId()}"),
                    text = name,
                    color = MaterialTheme.appColors.textFieldText,
                    style = MaterialTheme.appTypography.bodyMedium
                )
            }
        )
    }
}

@Composable
fun InputEditField(
    modifier: Modifier,
    name: String,
    initialValue: String,
    disabled: Boolean = false,
    onValueChanged: (String) -> Unit,
    onDoneClick: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val keyboardType = KeyboardType.Text

    Column {
        Text(
            modifier = Modifier
                .testTag("txt_label_${name.tagId()}")
                .fillMaxWidth(),
            text = name,
            style = MaterialTheme.appTypography.labelLarge,
            color = MaterialTheme.appColors.textPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            enabled = !disabled,
            value = initialValue,
            onValueChange = {
                onValueChanged(it)
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.appColors.textFieldText,
                unfocusedTextColor = MaterialTheme.appColors.textFieldText,
                unfocusedContainerColor = MaterialTheme.appColors.textFieldBackground,
                focusedContainerColor = MaterialTheme.appColors.textFieldBackground,
                unfocusedBorderColor = MaterialTheme.appColors.textFieldBorder,
                cursorColor = MaterialTheme.appColors.textFieldText,
            ),
            shape = MaterialTheme.appShapes.textFieldShape,
            placeholder = {
                Text(
                    modifier = Modifier.testTag("txt_placeholder_${name.tagId()}"),
                    text = name,
                    color = MaterialTheme.appColors.textFieldHint,
                    style = MaterialTheme.appTypography.bodyMedium
                )
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = keyboardType,
                imeAction = ImeAction.Default
            ),
            keyboardActions = KeyboardActions {
                keyboardController?.hide()
                focusManager.clearFocus()
                onDoneClick()
            },
            textStyle = MaterialTheme.appTypography.bodyMedium,
            modifier = modifier.testTag("tf_input_${name.tagId()}")
        )
    }
}

@Composable
fun LeaveProfile(
    onDismissRequest: () -> Unit,
    onLeaveClick: () -> Unit,
) {
    val scrollState = rememberScrollState()
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        content = {
            Column(
                Modifier
                    .verticalScroll(scrollState)
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.appColors.background,
                        MaterialTheme.appShapes.cardShape
                    )
                    .clip(MaterialTheme.appShapes.cardShape)
                    .border(
                        1.dp,
                        MaterialTheme.appColors.cardViewBorder,
                        MaterialTheme.appShapes.cardShape
                    )
                    .padding(horizontal = 40.dp)
                    .padding(top = 48.dp, bottom = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    modifier = Modifier
                        .size(60.dp),
                    painter = painterResource(profileRes.drawable.profile_ic_save),
                    tint = MaterialTheme.appColors.textPrimary,
                    contentDescription = null
                )
                Spacer(Modifier.size(48.dp))
                Text(
                    modifier = Modifier
                        .testTag("txt_leave_profile_title"),
                    text = stringResource(profileRes.string.profile_leave_profile),
                    color = MaterialTheme.appColors.textPrimary,
                    style = MaterialTheme.appTypography.titleLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.size(12.dp))
                Text(
                    modifier = Modifier
                        .testTag("txt_leave_profile_description"),
                    text = stringResource(profileRes.string.profile_changes_you_made),
                    color = MaterialTheme.appColors.textPrimary,
                    style = MaterialTheme.appTypography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.size(40.dp))
                OpenEdXButton(
                    text = stringResource(profileRes.string.profile_leave),
                    onClick = onLeaveClick,
                    backgroundColor = MaterialTheme.appColors.primary,
                    content = {
                        Text(
                            modifier = Modifier
                                .testTag("txt_leave")
                                .fillMaxWidth(),
                            text = stringResource(profileRes.string.profile_leave),
                            color = MaterialTheme.appColors.primaryButtonText,
                            style = MaterialTheme.appTypography.labelLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                )
                Spacer(Modifier.height(24.dp))
                OpenEdXOutlinedButton(
                    borderColor = MaterialTheme.appColors.textFieldBorder,
                    textColor = MaterialTheme.appColors.textPrimary,
                    text = stringResource(profileRes.string.profile_keep_editing),
                    onClick = onDismissRequest
                )
            }
        }
    )
}

@Composable
fun LeaveProfileLandscape(
    onDismissRequest: () -> Unit,
    onLeaveClick: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        ),
        content = {
            Card(
                modifier = Modifier
                    .fillMaxWidth(LEAVE_PROFILE_WIDTH_FACTOR)
                    .clip(MaterialTheme.appShapes.courseImageShape),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.background),
                shape = MaterialTheme.appShapes.courseImageShape
            ) {
                Row(
                    Modifier
                        .padding(horizontal = 40.dp)
                        .padding(top = 48.dp, bottom = 38.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            modifier = Modifier.size(100.dp),
                            painter = painterResource(profileRes.drawable.profile_ic_save),
                            contentDescription = null,
                            tint = MaterialTheme.appColors.onBackground
                        )
                        Spacer(Modifier.height(20.dp))
                        Text(
                            modifier = Modifier
                                .testTag("txt_leave_profile_dialog_title")
                                .fillMaxWidth(),
                            text = stringResource(profileRes.string.profile_leave_profile),
                            color = MaterialTheme.appColors.textPrimary,
                            style = MaterialTheme.appTypography.titleLarge,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            modifier = Modifier
                                .testTag("txt_leave_profile_dialog_description")
                                .fillMaxWidth(),
                            text = stringResource(profileRes.string.profile_changes_you_made),
                            color = MaterialTheme.appColors.textFieldText,
                            style = MaterialTheme.appTypography.titleSmall,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(Modifier.width(42.dp))
                    Column(
                        Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        OpenEdXButton(
                            text = stringResource(profileRes.string.profile_leave),
                            backgroundColor = MaterialTheme.appColors.primary,
                            content = {
                                AutoSizeText(
                                    modifier = Modifier.testTag("txt_leave_profile_dialog_leave"),
                                    text = stringResource(profileRes.string.profile_leave),
                                    style = MaterialTheme.appTypography.bodyMedium,
                                    color = MaterialTheme.appColors.primaryButtonText
                                )
                            },
                            onClick = onLeaveClick
                        )
                        Spacer(Modifier.height(16.dp))
                        OpenEdXOutlinedButton(
                            borderColor = MaterialTheme.appColors.textFieldBorder,
                            textColor = MaterialTheme.appColors.textPrimary,
                            text = stringResource(profileRes.string.profile_keep_editing),
                            onClick = onDismissRequest,
                            content = {
                                AutoSizeText(
                                    modifier = Modifier
                                        .testTag("btn_leave_profile_dialog_keep_editing"),
                                    text = stringResource(profileRes.string.profile_keep_editing),
                                    style = MaterialTheme.appTypography.bodyMedium,
                                    color = MaterialTheme.appColors.textPrimary
                                )
                            }
                        )
                    }
                }
            }
        }
    )
}
