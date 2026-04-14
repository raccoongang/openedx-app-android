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
    val username: String?,
    @SerialName("bio")
    val bio: String?,
    @SerialName("requires_parental_consent")
    val requiresParentalConsent: Boolean?,
    @SerialName("name")
    val name: String?,
    @SerialName("country")
    val country: String?,
    @SerialName("is_active")
    val isActive: Boolean?,
    @SerialName("profile_image")
    val profileImage: ProfileImage?,
    @SerialName("year_of_birth")
    val yearOfBirth: Int?,
    @SerialName("level_of_education")
    val levelOfEducation: String?,
    @SerialName("goals")
    val goals: String?,
    @SerialName("language_proficiencies")
    val languageProficiencies: List<LanguageProficiency>?,
    @SerialName("gender")
    val gender: String?,
    @SerialName("mailing_address")
    val mailingAddress: String?,
    @SerialName("email")
    val email: String?,
    @SerialName("date_joined")
    val dateJoined: String?,
    @SerialName("account_privacy")
    val accountPrivacy: Privacy?
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
