package org.openedx.profile.domain.interactor

import org.openedx.profile.data.repository.ImageBody
import org.openedx.profile.data.repository.ProfileRepository

class ProfileInteractor(private val repository: ProfileRepository) {

    suspend fun getAccount() = repository.getAccount()

    suspend fun getAccount(username: String) = repository.getAccount(username)

    fun getCachedAccount() = repository.getCachedAccount()

    suspend fun updateAccount(fields: Map<String, Any?>) = repository.updateAccount(fields)

    suspend fun setProfileImage(imageBody: ImageBody) = repository.setProfileImage(imageBody)

    suspend fun deleteProfileImage() = repository.deleteProfileImage()

    suspend fun deactivateAccount(password: String) = repository.deactivateAccount(password)

    suspend fun logout() {
        repository.logout()
    }
}
