package org.openedx.whatsnew

import kotlinx.serialization.json.Json
import org.openedx.core.config.Config
import org.openedx.core.presentation.global.AppData
import org.openedx.core.presentation.global.WhatsNewGlobalManager
import org.openedx.whatsnew.data.model.WhatsNewItem
import org.openedx.whatsnew.data.storage.WhatsNewPreferences

class WhatsNewManagerImpl(
    private val config: Config,
    private val whatsNewPreferences: WhatsNewPreferences,
    private val appData: AppData,
) : WhatsNewManager, WhatsNewGlobalManager {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    override fun getNewestData(): org.openedx.whatsnew.domain.model.WhatsNewItem {
        val whatsNewListData = json.decodeFromString<List<WhatsNewItem>>(WHATS_NEW_JSON)
        return whatsNewListData[0].mapToDomain()
    }

    override fun shouldShowWhatsNew(): Boolean {
        val dataVersion = getNewestData().version
        return appData.versionName == dataVersion &&
            whatsNewPreferences.lastWhatsNewVersion != dataVersion &&
            config.isWhatsNewEnabled()
    }
}

private const val WHATS_NEW_JSON = """
[
  {
    "version": "1.0",
    "messages": [
      {
        "image": "screen_1",
        "title": "Improved language support",
        "message": "We have added more translations throughout the app so you can learn on edX your way!"
      },
      {
        "image": "screen_2",
        "title": "Download videos offline",
        "message": "Easily download videos without having an internet connection, so you can keep learning when there isn't a network around"
      },
      {
        "image": "screen_2",
        "title": "Reduced Network Usage",
        "message": "Now you can download your content faster to get right into your next lesson!"
      },
      {
        "image": "screen_3",
        "title": "Learning Site Switching",
        "message": "Switch more easily between multiple learning sites. Find the new options within account settings and easily manage your accounts"
      }
    ]
  },
  {
    "version": "0.9",
    "messages": [
      {
        "image": "screen_1",
        "title": "Sync to calendar",
        "message": "Never miss a deadline again—sync course dates to your phone's calendar to receive reminders!"
      }
    ]
  }
]
"""
