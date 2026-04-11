package org.openedx.whatsnew.domain.model

import org.jetbrains.compose.resources.DrawableResource

data class WhatsNewMessage(
    val image: DrawableResource,
    val title: String,
    val message: String
)
