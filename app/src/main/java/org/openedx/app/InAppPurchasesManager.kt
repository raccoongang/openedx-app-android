package org.openedx.app

import org.openedx.core.oex.foundation.PurchaseProviderInterface
import org.openedx.core.oex.foundation.RestorePurchasesResult

class InAppPurchasesManager(
    private var service: PurchaseProviderInterface? = null
) : PurchaseProviderInterface {

    fun setService(service: PurchaseProviderInterface?) {
        this.service = service
    }

    override fun configure(configuration: Any?) {
        service?.configure(configuration)
    }

    override suspend fun purchaseCourse(
        courseId: String,
        userEmail: String?,
        priceTier: String
    ): Boolean {
        val s = service ?: return false
        return s.purchaseCourse(courseId, userEmail, priceTier)
    }

    override suspend fun getCoursePriceTier(courseId: String): String? {
        val s = service ?: return null
        return s.getCoursePriceTier(courseId)
    }

    override suspend fun localizedPrice(tier: String): String {
        val s = service ?: return ""
        return s.localizedPrice(tier)
    }

    override suspend fun getLocalizedCoursesTiers(
        courseIds: List<String>
    ): Map<String, String> {
        val s = service ?: return emptyMap()
        return s.getLocalizedCoursesTiers(courseIds)
    }

    override suspend fun restorePurchases(): RestorePurchasesResult {
        val s = service ?: return RestorePurchasesResult.NOTHING_TO_RESTORE
        return s.restorePurchases()
    }
}

