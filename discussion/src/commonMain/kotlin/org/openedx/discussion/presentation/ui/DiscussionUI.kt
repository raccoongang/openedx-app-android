package org.openedx.discussion.presentation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.openedx.core.ui.AutoSizeText
import org.openedx.core.ui.IconText
import org.openedx.core.ui.RenderHtmlContent
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appShapes
import org.openedx.core.ui.theme.appTypography
import org.openedx.core.utils.InstantUtils
import org.openedx.discussion.*
import org.openedx.discussion.Res
import org.openedx.discussion.Res as discussionRes
import org.openedx.discussion.discussion_comments
import org.openedx.discussion.discussion_missed_posts
import org.openedx.discussion.discussion_responses
import org.openedx.discussion.discussion_votes
import org.openedx.discussion.domain.model.DiscussionComment
import org.openedx.discussion.domain.model.DiscussionType
import org.openedx.discussion.domain.model.Topic
import org.openedx.discussion.presentation.DiscussionActions
import org.openedx.core.Res as coreRes
import org.openedx.core.core_accessibility_user_profile_image
import org.openedx.core.core_ic_default_profile_picture

@Composable
fun ThreadMainItem(
    modifier: Modifier,
    thread: org.openedx.discussion.domain.model.Thread,
    onClick: (String, Boolean) -> Unit,
    onUserPhotoClick: (String) -> Unit
) {
    val profileImageUrl = if (thread.users?.get(thread.author)?.image?.hasImage == true) {
        thread.users[thread.author]?.image?.imageUrlFull
    } else {
        null
    }

    val votePainter = if (thread.voted) {
        painterResource(Res.drawable.discussion_ic_like_success)
    } else {
        painterResource(Res.drawable.discussion_ic_like)
    }
    val voteColor = if (thread.voted) {
        MaterialTheme.appColors.primary
    } else {
        MaterialTheme.appColors.textPrimaryVariant
    }
    val reportText = if (thread.abuseFlagged) {
        stringResource(discussionRes.string.discussion_unreport)
    } else {
        stringResource(discussionRes.string.discussion_report)
    }
    val reportColor = if (thread.abuseFlagged) {
        MaterialTheme.appColors.error
    } else {
        MaterialTheme.appColors.textPrimaryVariant
    }

    Column(
        modifier = modifier
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = profileImageUrl,
                placeholder = painterResource(coreRes.drawable.core_ic_default_profile_picture),
                error = painterResource(coreRes.drawable.core_ic_default_profile_picture),
                fallback = painterResource(coreRes.drawable.core_ic_default_profile_picture),
                contentDescription = stringResource(
                    coreRes.string.core_accessibility_user_profile_image,
                    thread.author
                ),
                modifier = Modifier
                    .size(48.dp)
                    .clip(MaterialTheme.appShapes.material3.medium)
                    .clickable {
                        if (thread.author.isNotEmpty()) {
                            onUserPhotoClick(thread.author)
                        }
                    }
            )
            Spacer(Modifier.width(16.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        if (thread.author.isNotEmpty()) {
                            onUserPhotoClick(thread.author)
                        }
                    },
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = thread.author.ifEmpty { stringResource(discussionRes.string.discussion_anonymous) },
                    color = MaterialTheme.appColors.textPrimary,
                    style = MaterialTheme.appTypography.titleMedium
                )
                Text(
                    text = InstantUtils.iso8601ToDateWithTime(thread.createdAt),
                    style = MaterialTheme.appTypography.labelSmall,
                    color = MaterialTheme.appColors.textPrimaryVariant
                )
            }
            IconText(
                text = stringResource(discussionRes.string.discussion_follow),
                painter = painterResource(
                    if (thread.following) {
                        Res.drawable.discussion_star_filled
                    } else {
                        Res.drawable.discussion_star
                    }
                ),
                textStyle = MaterialTheme.appTypography.labelLarge,
                color = MaterialTheme.appColors.textPrimaryVariant,
                onClick = {
                    onClick(DiscussionActions.ACTION_FOLLOW_THREAD, !thread.following)
                }
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        RenderHtmlContent(
            html = thread.rawBody,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconText(
                text = pluralStringResource(
                    Res.plurals.discussion_votes,
                    thread.voteCount,
                    thread.voteCount
                ),
                painter = votePainter,
                color = voteColor,
                textStyle = MaterialTheme.appTypography.labelLarge,
                onClick = {
                    onClick(DiscussionActions.ACTION_UPVOTE_THREAD, !thread.voted)
                }
            )
            IconText(
                text = reportText,
                painter = painterResource(Res.drawable.discussion_ic_report),
                textStyle = MaterialTheme.appTypography.labelLarge,
                color = reportColor,
                onClick = {
                    onClick(DiscussionActions.ACTION_REPORT_THREAD, !thread.abuseFlagged)
                }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.appColors.cardViewBorder)
    }
}

@Composable
fun CommentItem(
    modifier: Modifier,
    comment: DiscussionComment,
    shape: Shape = MaterialTheme.appShapes.cardShape,
    onClick: (String, String, Boolean) -> Unit,
    onAddCommentClick: () -> Unit = {},
    onUserPhotoClick: (String) -> Unit
) {
    val profileImageUrl = if (comment.profileImage?.hasImage == true) {
        comment.profileImage.imageUrlFull
    } else if (comment.users?.get(comment.author)?.image?.hasImage == true) {
        comment.users[comment.author]?.image?.imageUrlFull
    } else {
        null
    }

    val reportText = if (comment.abuseFlagged) {
        stringResource(discussionRes.string.discussion_unreport)
    } else {
        stringResource(discussionRes.string.discussion_report)
    }

    val reportColor = if (comment.abuseFlagged) {
        MaterialTheme.appColors.error
    } else {
        MaterialTheme.appColors.textPrimaryVariant
    }
    val votePainter = if (comment.voted) {
        painterResource(Res.drawable.discussion_ic_like_success)
    } else {
        painterResource(Res.drawable.discussion_ic_like)
    }
    val voteColor = if (comment.voted) {
        MaterialTheme.appColors.textAccent
    } else {
        MaterialTheme.appColors.textPrimaryVariant
    }

    Card(
        shape = shape,
        modifier = modifier.then(
            Modifier.border(
                1.dp,
                MaterialTheme.appColors.cardViewBorder,
                shape
            )
        ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = profileImageUrl,
                    placeholder = painterResource(coreRes.drawable.core_ic_default_profile_picture),
                    error = painterResource(coreRes.drawable.core_ic_default_profile_picture),
                    fallback = painterResource(coreRes.drawable.core_ic_default_profile_picture),
                    contentDescription = stringResource(
                        coreRes.string.core_accessibility_user_profile_image,
                        comment.author
                    ),
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable {
                            onUserPhotoClick(comment.author)
                        }
                )
                Spacer(Modifier.width(12.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            onUserPhotoClick(comment.author)
                        },
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = comment.author,
                        color = MaterialTheme.appColors.textPrimary,
                        style = MaterialTheme.appTypography.titleSmall
                    )
                    Text(
                        text = InstantUtils.iso8601ToDateWithTime(comment.createdAt),
                        style = MaterialTheme.appTypography.labelSmall,
                        color = MaterialTheme.appColors.textPrimaryVariant
                    )
                }
                IconText(
                    text = reportText,
                    painter = painterResource(Res.drawable.discussion_ic_report),
                    textStyle = MaterialTheme.appTypography.labelMedium,
                    color = reportColor,
                    onClick = {
                        onClick(
                            DiscussionActions.ACTION_REPORT_COMMENT,
                            comment.id,
                            !comment.abuseFlagged
                        )
                    }
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            RenderHtmlContent(
                html = comment.rawBody,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconText(
                    text = pluralStringResource(
                        Res.plurals.discussion_votes,
                        comment.voteCount,
                        comment.voteCount
                    ),
                    painter = votePainter,
                    color = voteColor,
                    textStyle = MaterialTheme.appTypography.labelLarge,
                    onClick = {
                        onClick(
                            DiscussionActions.ACTION_UPVOTE_COMMENT,
                            comment.id,
                            !comment.voted
                        )
                    }
                )
                IconText(
                    text = pluralStringResource(
                        Res.plurals.discussion_comments,
                        comment.childCount,
                        comment.childCount
                    ),
                    painter = painterResource(Res.drawable.discussion_ic_comment),
                    color = MaterialTheme.appColors.textPrimaryVariant,
                    textStyle = MaterialTheme.appTypography.labelLarge,
                    onClick = {
                        onAddCommentClick()
                    }
                )
            }
        }
    }
}

@Composable
fun CommentMainItem(
    modifier: Modifier,
    internalPadding: Dp = 16.dp,
    comment: DiscussionComment,
    onClick: (String, String, Boolean) -> Unit,
    onUserPhotoClick: (String) -> Unit
) {
    val profileImageUrl = if (comment.profileImage?.hasImage == true) {
        comment.profileImage.imageUrlFull
    } else if (comment.users?.get(comment.author)?.image?.hasImage == true) {
        comment.users[comment.author]?.image?.imageUrlFull
    } else {
        null
    }

    val reportText = if (comment.abuseFlagged) {
        stringResource(discussionRes.string.discussion_unreport)
    } else {
        stringResource(discussionRes.string.discussion_report)
    }
    val reportColor = if (comment.abuseFlagged) {
        MaterialTheme.appColors.error
    } else {
        MaterialTheme.appColors.textPrimaryVariant
    }

    val votePainter = if (comment.voted) {
        painterResource(Res.drawable.discussion_ic_like_success)
    } else {
        painterResource(Res.drawable.discussion_ic_like)
    }
    val voteColor = if (comment.voted) {
        MaterialTheme.appColors.textAccent
    } else {
        MaterialTheme.appColors.textPrimaryVariant
    }

    Surface(
        modifier = modifier,
        color = MaterialTheme.appColors.background
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(internalPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = profileImageUrl,
                    placeholder = painterResource(coreRes.drawable.core_ic_default_profile_picture),
                    error = painterResource(coreRes.drawable.core_ic_default_profile_picture),
                    fallback = painterResource(coreRes.drawable.core_ic_default_profile_picture),
                    contentDescription = stringResource(
                        coreRes.string.core_accessibility_user_profile_image,
                        comment.author
                    ),
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable {
                            onUserPhotoClick(comment.author)
                        }
                )
                Spacer(Modifier.width(12.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            onUserPhotoClick(comment.author)
                        },
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = comment.author,
                        color = MaterialTheme.appColors.textPrimary,
                        style = MaterialTheme.appTypography.titleMedium
                    )
                    Text(
                        text = InstantUtils.iso8601ToDateWithTime(comment.createdAt),
                        style = MaterialTheme.appTypography.labelSmall,
                        color = MaterialTheme.appColors.textPrimaryVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            RenderHtmlContent(
                html = comment.rawBody,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconText(
                    text = pluralStringResource(
                        Res.plurals.discussion_votes,
                        comment.voteCount,
                        comment.voteCount
                    ),
                    painter = votePainter,
                    color = voteColor,
                    textStyle = MaterialTheme.appTypography.labelLarge,
                    onClick = {
                        onClick(
                            DiscussionActions.ACTION_UPVOTE_COMMENT,
                            comment.id,
                            !comment.voted
                        )
                    }
                )
                IconText(
                    text = reportText,
                    painter = painterResource(Res.drawable.discussion_ic_report),
                    textStyle = MaterialTheme.appTypography.labelLarge,
                    color = reportColor,
                    onClick = {
                        onClick(
                            DiscussionActions.ACTION_REPORT_COMMENT,
                            comment.id,
                            !comment.abuseFlagged
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun ThreadItem(
    thread: org.openedx.discussion.domain.model.Thread,
    onClick: (org.openedx.discussion.domain.model.Thread) -> Unit,
) {
    val icon = when (thread.type) {
        DiscussionType.DISCUSSION -> painterResource(Res.drawable.discussion_ic_discussion)
        DiscussionType.QUESTION -> rememberVectorPainter(image = Icons.AutoMirrored.Outlined.HelpOutline)
    }
    val textType = when (thread.type) {
        DiscussionType.DISCUSSION -> stringResource(discussionRes.string.discussion_discussion)
        DiscussionType.QUESTION -> stringResource(discussionRes.string.discussion_question)
    }

    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.appColors.background)
            .clickable { onClick(thread) }
            .padding(vertical = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconText(
                text = textType,
                painter = icon,
                color = MaterialTheme.appColors.textPrimaryVariant,
                textStyle = MaterialTheme.appTypography.labelSmall
            )
            if (thread.unreadCommentCount > 0 && !thread.read) {
                Row(
                    modifier = Modifier,
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box {
                        Icon(
                            modifier = Modifier.size((MaterialTheme.appTypography.labelLarge.fontSize.value).dp),
                            painter = painterResource(Res.drawable.discussion_ic_unread_replies),
                            tint = MaterialTheme.appColors.textPrimaryVariant,
                            contentDescription = null
                        )
                        Image(
                            modifier = Modifier.size((MaterialTheme.appTypography.labelLarge.fontSize.value).dp),
                            painter = painterResource(Res.drawable.discussion_ic_unread_replies_dot),
                            contentDescription = null
                        )
                    }
                    Text(
                        text = pluralStringResource(
                            Res.plurals.discussion_missed_posts,
                            thread.unreadCommentCount,
                            thread.unreadCommentCount
                        ),
                        color = MaterialTheme.appColors.textPrimaryVariant,
                        style = MaterialTheme.appTypography.labelSmall
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = thread.title,
            style = MaterialTheme.appTypography.labelLarge,
            color = MaterialTheme.appColors.textPrimary,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(
                discussionRes.string.discussion_last_post,
                InstantUtils.iso8601ToDateWithTime(thread.updatedAt)
            ),
            style = MaterialTheme.appTypography.labelSmall,
            color = MaterialTheme.appColors.textPrimaryVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        IconText(
            text = pluralStringResource(
                Res.plurals.discussion_responses,
                thread.commentCount - 1,
                thread.commentCount - 1
            ),
            painter = painterResource(Res.drawable.discussion_ic_responses),
            color = MaterialTheme.appColors.textPrimary,
            textStyle = MaterialTheme.appTypography.labelLarge
        )
    }
}

@Composable
fun ThreadItemCategory(
    name: String,
    painterResource: Painter,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier.then(
            Modifier
                .border(
                    1.dp,
                    MaterialTheme.appColors.cardViewBorder,
                    MaterialTheme.appShapes.cardShape
                )
                .clip(MaterialTheme.appShapes.cardShape)
                .clickable { onClick() }
        ),
        shape = MaterialTheme.appShapes.cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(11.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource,
                contentDescription = null,
                tint = MaterialTheme.appColors.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            AutoSizeText(
                text = name,
                style = MaterialTheme.appTypography.bodyMedium,
                color = MaterialTheme.appColors.textPrimary,
                maxLines = 1
            )
        }
    }
}

@Composable
fun TopicItem(
    topic: Topic,
    onClick: (String, String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(topic.id, topic.name) }
            .padding(horizontal = 8.dp, vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = topic.name,
            style = MaterialTheme.appTypography.titleMedium,
            color = MaterialTheme.appColors.textPrimary
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            tint = MaterialTheme.appColors.primary,
            contentDescription = "Expandable Arrow"
        )
    }
}
