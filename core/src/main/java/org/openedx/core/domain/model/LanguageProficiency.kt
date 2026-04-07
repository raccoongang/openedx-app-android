package org.openedx.core.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class LanguageProficiency(
    @SerialName("code")
    val code: String
) : Parcelable
