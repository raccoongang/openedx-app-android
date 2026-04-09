package org.openedx.whatsnew

import android.content.Context
import kotlinx.serialization.json.Json
import org.openedx.core.config.Config
import org.openedx.core.presentation.global.AppData
import org.openedx.core.presentation.global.WhatsNewGlobalManager
import org.openedx.whatsnew.data.model.WhatsNewItem
import org.openedx.whatsnew.data.storage.WhatsNewPreferences

class WhatsNewManager(
    private val context: Context,
    private val config: Config,
    private val whatsNewPreferences: WhatsNewPreferences,
    private val appData: AppData,
) : WhatsNewGlobalManager {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun getNewestData(): org.openedx.whatsnew.domain.model.WhatsNewItem {
        val jsonString = context.resources.openRawResource(R.raw.whats_new)
            .bufferedReader()
            .use { it.readText() }
        val whatsNewListData = json.decodeFromString<List<WhatsNewItem>>(jsonString)
        return whatsNewListData[0].mapToDomain(context)
    }

    override fun shouldShowWhatsNew(): Boolean {
        val dataVersion = getNewestData().version
        return appData.versionName == dataVersion &&
            whatsNewPreferences.lastWhatsNewVersion != dataVersion &&
            config.isWhatsNewEnabled()
    }
}
