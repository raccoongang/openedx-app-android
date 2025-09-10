package org.openedx.core.oex.foundation

interface PurchaseProviderInterface {
    // Configure
    fun configure(configuration: Any?)

    // CourseDetails Screen
    suspend fun purchaseCourse(courseID: String, userEmail: String?, priceTier: String): Boolean
    suspend fun getCoursePriceTier(courseID: String): String?
    suspend fun localizedPrice(tier: String): String

    // Discovery Screen
    suspend fun getLocalizedCoursesTiers(courseIDs: List<String>): Map<String, String>
    suspend fun restorePurchases(): RestorePurchasesResult
}

enum class RestorePurchasesResult {
    RESTORED,
    NOTHING_TO_RESTORE
}

enum class PurchaseError {
    PURCHASE_FAILED,
    PRODUCT_NOT_FOUND,
    USER_CANCELLED,
    NETWORK_ERROR
}

class PurchaseException(
    val error: PurchaseError,
    cause: Throwable? = null
) : Exception(error.name, cause)

class InAppPurchasesManagerMock : PurchaseProviderInterface {

    override fun configure(configuration: Any?) {
        // no-op
    }

    override suspend fun purchaseCourse(
        courseId: String,
        userEmail: String?,
        priceTier: String
    ): Boolean {
        return true
    }

    override suspend fun getCoursePriceTier(courseId: String): String? {
        return null
    }

    override suspend fun localizedPrice(tier: String): String {
        return ""
    }

    override suspend fun getLocalizedCoursesTiers(courseIds: List<String>): Map<String, String> {
        return emptyMap()
    }

    override suspend fun restorePurchases(): RestorePurchasesResult {
        return RestorePurchasesResult.RESTORED
    }
}