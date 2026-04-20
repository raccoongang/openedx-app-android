package org.openedx.profile.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openedx.core.data.model.ProfileImage
import org.openedx.core.utils.InstantUtils
import org.openedx.profile.domain.model.Account
import org.openedx.profile.domain.model.Account as DomainAccount

@Serializable
data class Account(
    @SerialName("username")
    val username: String? = null,
    @SerialName("bio")
    val bio: String? = null,
    @SerialName("requires_parental_consent")
    val requiresParentalConsent: Boolean? = null,
    @SerialName("name")
    val name: String? = null,
    @SerialName("country")
    val country: String? = null,
    @SerialName("is_active")
    val isActive: Boolean? = null,
    @SerialName("profile_image")
    val profileImage: ProfileImage? = null,
    @SerialName("year_of_birth")
    val yearOfBirth: Int? = null,
    @SerialName("level_of_education")
    val levelOfEducation: String? = null,
    @SerialName("goals")
    val goals: String? = null,
    @SerialName("language_proficiencies")
    val languageProficiencies: List<LanguageProficiency>? = null,
    @SerialName("gender")
    val gender: String? = null,
    @SerialName("mailing_address")
    val mailingAddress: String? = null,
    @SerialName("email")
    val email: String? = null,
    @SerialName("date_joined")
    val dateJoined: String? = null,
    @SerialName("account_privacy")
    val accountPrivacy: Privacy? = null
) {

    enum class Privacy {
        @SerialName("private")
        PRIVATE,

        @SerialName("all_users")
        ALL_USERS
    }

    fun mapToDomain(): Account {
        return Account(
            username = username ?: "",
            bio = bio ?: "",
            requiresParentalConsent = requiresParentalConsent ?: false,
            name = name ?: "",
            country = country ?: "",
            isActive = isActive ?: true,
            profileImage = profileImage?.mapToDomain() ?: org.openedx.core.domain.model.ProfileImage("", "", "", "", false),
            yearOfBirth = yearOfBirth,
            levelOfEducation = levelOfEducation ?: "",
            goals = goals ?: "",
            languageProficiencies = languageProficiencies?.let { languageProficiencyList ->
                languageProficiencyList.map { it.mapToDomain() }
            } ?: emptyList(),
            gender = gender ?: "",
            mailingAddress = mailingAddress ?: "",
            email = email,
            dateJoined = dateJoined?.let { InstantUtils.iso8601ToInstant(it) },
            accountPrivacy = if (accountPrivacy == Privacy.PRIVATE) {
                DomainAccount.Privacy.PRIVATE
            } else {
                DomainAccount.Privacy.ALL_USERS
            }
        )
    }
}
