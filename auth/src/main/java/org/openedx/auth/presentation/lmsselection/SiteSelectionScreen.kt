package org.openedx.auth.presentation.lmsselection

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.openedx.auth.R
import org.openedx.core.lmsdirectory.LmsImageSource
import org.openedx.core.lmsdirectory.LmsSummary
import org.openedx.core.lmsdirectory.LmsThemeController
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appShapes
import org.openedx.core.ui.theme.appTypography

@Composable
internal fun SiteSelectionScreen(
    state: SiteSelectionUIState,
    callbacks: SiteSelectionCallbacks,
) {
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        containerColor = MaterialTheme.appColors.background,
        topBar = {
            Surface(color = MaterialTheme.appColors.background) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(id = R.string.auth_lms_curated_title),
                        style = MaterialTheme.appTypography.titleMedium,
                        color = MaterialTheme.appColors.textPrimary,
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            val context = LocalContext.current
            LaunchedEffect(state.imageReferences) {
                // Decoding these now is the whole reason the branded sign-in appears
                // whole instead of assembling itself after the platform is tapped.
                LmsImageSource.prefetch(context, state.imageReferences)
            }

            if (state.providerName.isNotBlank()) {
                Text(
                    text = stringResource(id = R.string.auth_lms_provider_subtitle, state.providerName),
                    style = MaterialTheme.appTypography.labelLarge,
                    color = MaterialTheme.appColors.textPrimaryVariant,
                )
            }

            when (val catalog = state.catalog) {
                is CatalogState.Loading -> LoadingRow()

                is CatalogState.Loaded -> state.platforms.forEach { item ->
                    CatalogRow(item) { callbacks.onPlatformSelected(item) }
                }

                is CatalogState.Empty -> Message(stringResource(id = R.string.auth_lms_empty))

                is CatalogState.Error -> {
                    Message(catalog.message)
                    TextButton(onClick = callbacks.onRetry) {
                        Text(
                            text = stringResource(id = R.string.auth_lms_retry),
                            style = MaterialTheme.appTypography.labelLarge,
                            color = MaterialTheme.appColors.primary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Message(text: String) {
    Text(
        text = text,
        style = MaterialTheme.appTypography.bodyMedium,
        color = MaterialTheme.appColors.textPrimaryVariant,
    )
}

@Composable
private fun LoadingRow() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = MaterialTheme.appColors.primary)
    }
}

@Composable
private fun CatalogRow(item: LmsSummary, onSelect: () -> Unit) {
    CatalogRow(
        title = item.title,
        shortDescription = item.shortDescription,
        baseUrl = item.baseUrl,
        logoUrl = item.logoUrl,
        accentColor = item.accentColor,
        onSelect = onSelect,
    )
}

@Composable
private fun CatalogRow(
    title: String,
    shortDescription: String,
    baseUrl: String,
    logoUrl: String?,
    accentColor: String?,
    onSelect: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = MaterialTheme.appShapes.textFieldShape,
        color = MaterialTheme.appColors.background,
        border = BorderStroke(1.dp, MaterialTheme.appColors.textFieldBorder.copy(alpha = 0.5f)),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LmsRowLogo(logoUrl = logoUrl, title = title, accentColor = accentColor)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    maxLines = 1,
                    style = MaterialTheme.appTypography.bodyLarge,
                    color = MaterialTheme.appColors.textPrimary,
                )
                if (shortDescription.isNotBlank()) {
                    Text(
                        text = shortDescription,
                        maxLines = 1,
                        style = MaterialTheme.appTypography.bodyMedium,
                        color = MaterialTheme.appColors.textPrimaryVariant,
                    )
                }
                Text(
                    text = hostOf(baseUrl),
                    maxLines = 1,
                    style = MaterialTheme.appTypography.labelMedium,
                    color = MaterialTheme.appColors.textPrimaryVariant,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.appColors.textPrimaryVariant,
            )
        }
    }
}

/**
 * Platform logo for a catalog row. Loads the LMS's logo when available; otherwise
 * falls back to a colored initial badge tinted with the platform's accent color —
 * mirroring the iOS directory rows.
 */
@Composable
private fun LmsRowLogo(logoUrl: String?, title: String, accentColor: String?) {
    val logoModifier = Modifier
        .size(48.dp)
        .clip(RoundedCornerShape(10.dp))
    val logoModel = LmsImageSource.model(logoUrl)
    if (logoModel != null) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(logoModel)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = logoModifier,
        )
    } else {
        val accent = LmsThemeController.parseHexColor(accentColor) ?: MaterialTheme.appColors.primary
        Box(
            modifier = logoModifier.background(accent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = title.trim().take(1).uppercase(),
                style = MaterialTheme.appTypography.titleMedium,
                color = accent,
            )
        }
    }
}

private fun hostOf(url: String): String {
    return url.removePrefix("https://").removePrefix("http://").trimEnd('/')
}
