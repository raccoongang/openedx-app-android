package org.openedx.profile.data.repository

import org.openedx.core.ApiConstants
import org.openedx.core.DatabaseManager
import org.openedx.core.config.Config
import org.openedx.core.data.storage.CorePreferences
import org.openedx.profile.data.api.ProfileApi
import org.openedx.profile.data.storage.ProfilePreferences
import org.openedx.profile.domain.model.Account

class ProfileRepositoryImpl(
    private val config: Config,
    private val api: ProfileApi,
    private val profilePreferences: ProfilePreferences,
    private val corePreferences: CorePreferences,
    private val databaseManager: DatabaseManager
) : ProfileRepository {

    override suspend fun getAccount(): Account {
        val account = api.getAccount(corePreferences.user?.username!!)
        profilePreferences.profile = account
        return account.mapToDomain()
    }

    override suspend fun getAccount(username: String): Account {
        val account = api.getAccount(username)
        return account.mapToDomain()
    }

    override fun getCachedAccount(): Account? {
        return profilePreferences.profile?.mapToDomain()
    }

    override suspend fun updateAccount(fields: Map<String, Any?>): Account {
        val dataAccount = api.updateAccount(corePreferences.user?.username!!, fields)
        profilePreferences.profile = dataAccount
        return dataAccount.mapToDomain()
    }

    override suspend fun setProfileImage(imageBody: ImageBody) {
        api.setProfileImage(
            username = corePreferences.user?.username!!,
            contentDisposition = "attachment;filename=filename.${imageBody.extension}",
            contentType = imageBody.mimeType,
            mobile = true,
            fileBytes = imageBody.bytes,
        )
    }

    override suspend fun deleteProfileImage() {
        api.deleteProfileImage(corePreferences.user?.username!!)
    }

    override suspend fun deactivateAccount(password: String) {
        api.deactivateAccount(password)
    }

    override suspend fun logout() {
        try {
            api.revokeAccessToken(
                config.getOAuthClientId(),
                corePreferences.refreshToken,
                ApiConstants.TOKEN_TYPE_REFRESH
            )
        } finally {
            corePreferences.clearCorePreferences()
            databaseManager.clearTables()
        }
    }
}
