package org.openedx.profile.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.stringResource
import org.openedx.profile.*
import org.openedx.profile.Res as profileRes
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.painterResource
import org.openedx.core.Res as coreRes
import org.openedx.core.core_accessibility_user_profile_image
import org.openedx.core.core_ic_default_profile_picture
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appShapes
import org.openedx.core.ui.theme.appTypography
import org.openedx.profile.domain.model.Account

@Composable
fun ProfileTopic(image: String, title: String, subtitle: String) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = image,
            contentDescription = stringResource(
                coreRes.string.core_accessibility_user_profile_image,
                title
            ),
            modifier = Modifier
                .testTag("img_profile")
                .size(80.dp)
                .clip(CircleShape),
            placeholder = painterResource(coreRes.drawable.core_ic_default_profile_picture),
            error = painterResource(coreRes.drawable.core_ic_default_profile_picture),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            if (title.isNotEmpty()) {
                Text(
                    modifier = Modifier
                        .testTag("txt_profile_name")
                        .fillMaxWidth(),
                    text = title,
                    color = MaterialTheme.appColors.textPrimary,
                    style = MaterialTheme.appTypography.titleLarge
                )
            }
            Text(
                modifier = Modifier
                    .testTag("txt_profile_username")
                    .fillMaxWidth(),
                text = subtitle,
                color = MaterialTheme.appColors.textPrimary,
                style = MaterialTheme.appTypography.bodyMedium
            )
        }
    }
}

@Composable
fun ProfileInfoSection(account: Account) {
    if (account.bio.isNotEmpty()) {
        Column {
            Card(
                modifier = Modifier,
                shape = MaterialTheme.appShapes.cardShape,
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.cardViewBackground)
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (account.bio.isNotEmpty()) {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = stringResource(profileRes.string.profile_about_me),
                            style = MaterialTheme.appTypography.titleSmall,
                            color = MaterialTheme.appColors.textPrimary
                        )
                        Text(
                            modifier = Modifier.testTag("txt_profile_bio"),
                            text = account.bio,
                            style = MaterialTheme.appTypography.bodyMedium,
                            color = MaterialTheme.appColors.textPrimary
                        )
                    }
                }
            }
        }
    }
}
