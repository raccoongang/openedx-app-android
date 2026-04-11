package org.openedx.profile.domain.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.openedx.core.AppDataConstants.USER_MIN_YEAR
import org.openedx.core.domain.model.LanguageProficiency
import org.openedx.core.domain.model.ProfileImage

data class Account(
    val username: String,
    val bio: String,
    val requiresParentalConsent: Boolean,
    val name: String,
    val country: String,
    val isActive: Boolean,
    val profileImage: ProfileImage,
    val yearOfBirth: Int?,
    val levelOfEducation: String,
    val goals: String,
    val languageProficiencies: List<LanguageProficiency>,
    val gender: String,
    val mailingAddress: String,
    val email: String?,
    val dateJoined: Instant?,
    val accountPrivacy: Privacy
) {

    enum class Privacy {
        PRIVATE,
        ALL_USERS
    }

    fun isLimited() = accountPrivacy == Privacy.PRIVATE

    fun isOlderThanMinAge(): Boolean {
        val currentYear = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year
        return yearOfBirth != null && currentYear - yearOfBirth > USER_MIN_YEAR
    }
}
