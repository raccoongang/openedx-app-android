package org.openedx.app

import org.openedx.core.oex.foundation.PurchaseProviderInterface
import org.openedx.foundation.interfaces.Analytics

class PluginManager(
    private val analyticsManager: AnalyticsManager,
    private val purchasesManager: PurchaseProviderInterface,
) {

    fun addPlugin(analytics: Analytics) {
        analyticsManager.addAnalyticsTracker(analytics)
    }

    fun addPlugin(purchaseProvider: PurchaseProviderInterface) {
        (purchasesManager as? InAppPurchasesManager)?.setService(purchaseProvider)
    }
}
