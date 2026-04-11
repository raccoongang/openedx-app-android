package org.openedx.whatsnew

import org.openedx.whatsnew.domain.model.WhatsNewItem

interface WhatsNewManager {
    fun getNewestData(): WhatsNewItem?
    fun shouldShowWhatsNew(): Boolean
}
