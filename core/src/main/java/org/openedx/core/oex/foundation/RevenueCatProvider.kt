package org.openedx.core.oex.foundation

import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.ProductType
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.PurchasesErrorCode
import com.revenuecat.purchases.interfaces.LogInCallback
import com.revenuecat.purchases.models.StoreProduct
import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.interfaces.GetStoreProductsCallback
import com.revenuecat.purchases.interfaces.PurchaseCallback
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class RevenueCatConfiguration(
    val application: Application,
    val apiKey: String,
    val userID: String?
)

object ActivityProvider : Application.ActivityLifecycleCallbacks {
    private val ref: AtomicReference<Activity?> = AtomicReference(null)

    fun init(app: Application) {
        app.unregisterActivityLifecycleCallbacks(this) // на всякий случай
        app.registerActivityLifecycleCallbacks(this)
    }

    fun current(): Activity? = ref.get()

    override fun onActivityResumed(activity: Activity) {
        ref.set(activity)
        Log.d("RCProvider", "Activity resumed: ${activity::class.java.simpleName}")
    }
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        if (ref.get() === activity) ref.set(null)
        Log.d("RCProvider", "Activity destroyed: ${activity::class.java.simpleName}")
    }
}

class PurchaseProviderRevenueCat : PurchaseProviderInterface {
    // Debug helper
    private fun dbg(msg: String) = Log.d(TAG, msg)

    companion object {
        private const val TAG = "RCProvider"
    }

    private var isConfigured = false

    private val coursePriceTiers = LinkedHashMap<String, String>()

    override fun configure(configuration: Any?) {
        Log.d(TAG, "configure(): isConfigured=$isConfigured, configuration=$configuration")
        if (configuration !is RevenueCatConfiguration) return
        if (!isConfigured) {
            ActivityProvider.init(configuration.application)

            val builder = PurchasesConfiguration.Builder(configuration.application, configuration.apiKey)
            val uid = configuration.userID?.takeIf { it.isNotBlank() }
            if (uid != null) {
                builder.appUserID(uid)
            }
            val cfg = builder.build()
            Purchases.configure(cfg)
            Log.d(TAG, "configure(): Purchases configured; logLevel=VERBOSE")
            Purchases.logLevel = LogLevel.VERBOSE

            if (uid != null) {
                Purchases.sharedInstance.logIn(
                    uid,
                    object : LogInCallback {
                        override fun onError(error: PurchasesError) {
                            Log.w(TAG, "logIn error: $error")
                        }

                        override fun onReceived(customerInfo: CustomerInfo, created: Boolean) {
                            Log.d(TAG, "logIn ok, created=$created")
                        }
                    }
                )
            }
            isConfigured = true
        }
    }

    private suspend fun fetchCoursesPriceTiersFromServer(courseIDs: List<String>): Map<String, String> {
        Log.d(TAG, "fetchCoursesPriceTiersFromServer(): requested ${courseIDs.size} IDs: $courseIDs")
        withContext(Dispatchers.IO) {
            try { Thread.sleep(200) } catch (_: InterruptedException) {}
        }
        val mapping = mapOf(
            "course-v1:TestOrg11+TestNum11+TestRun11" to "test.edx.tier1",
            "course-v1:TestOrg1+TestNum1+TestRun1" to "test.edx.tier2",
            "course-v1:TestOrg10+TestNum10+TestRun10" to "test.edx.tier3",
            "course-v1:TestOrg12+TestNum12+TestRun12" to "test.edx.tier4",
            "course-v1:TestOrg13+TestNum13+TestRun13" to "test.edx.tier5",
            "course-v1:TestOrg14+TestNum14+TestRun14" to "test.edx.tier6",
            "course-v1:TestOrg15+TestNum15+TestRun15" to "test.edx.tier7",
            "course-v1:TestOrg17+TestNum17+TestRun17" to "test.edx.tier8",
            "course-v1:TestOrg18+TestNum18+TestRun18" to "test.edx.tier9",
            "course-v1:TestOrg2+TesNum2+TestRun2" to "test.edx.tier10",
            "course-v1:TestOrg3+TesNum3+TestRun3" to "test.edx.tier7",
            "course-v1:TestOrg6+TestNum6+TestRun6" to "test.edx.tier3",
        )
        Log.d(TAG, "fetchCoursesPriceTiersFromServer(): returning mapping size=${mapping.size}")
        mapping.forEach { (c, t) -> Log.v(TAG, "  map $c -> $t") }
        return mapping
    }

    private suspend fun fetchProducts(ids: List<String>): List<StoreProduct> =
        suspendCancellableCoroutine { cont ->
            if (ids.isEmpty()) {
                Log.w(TAG, "fetchProducts(): empty ids; returning empty list")
                cont.resume(emptyList()); return@suspendCancellableCoroutine
            }

            Log.d(TAG, "fetchProducts(): querying INAPP products: $ids")
            Purchases.sharedInstance.getProducts(
                productIds = ids,
                type = ProductType.INAPP,
                callback = object : GetStoreProductsCallback {
                    override fun onReceived(storeProducts: List<StoreProduct>) {
                        Log.d(TAG, "fetchProducts(): received ${storeProducts.size} products -> " +
                                storeProducts.joinToString { "${it.id}:${it.price.formatted}" })
                        cont.resume(storeProducts)
                    }
                    override fun onError(error: PurchasesError) {
                        Log.w(TAG, "fetchProducts(): error code=${error.code} message=${error.message}")
                        cont.resumeWithException(
                            PurchaseException(PurchaseError.PRODUCT_NOT_FOUND, Exception(error.message))
                        )
                    }
                }
            )
        }

    private suspend fun purchase(storeProduct: StoreProduct): CustomerInfo {
        Log.d(TAG, "purchase(): starting for ${storeProduct.id} at ${storeProduct.price.formatted}")
        return suspendCancellableCoroutine { cont ->
            val activity = ActivityProvider.current()
                ?: return@suspendCancellableCoroutine cont.resumeWithException(
                    PurchaseException(
                        PurchaseError.PURCHASE_FAILED,
                        IllegalStateException("No foreground Activity")
                    )
                )

            val params = PurchaseParams.Builder(activity, storeProduct).build()

            Purchases.sharedInstance.purchase(
                purchaseParams = params,
                callback = object : PurchaseCallback {
                    override fun onCompleted(
                        storeTransaction: com.revenuecat.purchases.models.StoreTransaction,
                        customerInfo: CustomerInfo
                    ) {
                        Log.d(TAG, "purchase(): completed, txn productId=${storeTransaction.productIds}")
                        if (!cont.isCompleted) cont.resume(customerInfo)
                    }

                    override fun onError(
                        error: PurchasesError,
                        userCancelled: Boolean
                    ) {
                        Log.w(TAG, "purchase(): error code=${error.code} userCancelled=$userCancelled msg=${error.message}")
                        if (error.code == PurchasesErrorCode.ProductAlreadyPurchasedError) {
                            // Treat as a successful purchase by restoring/refreshing customer info
                            Purchases.sharedInstance.restorePurchases(object : ReceiveCustomerInfoCallback {
                                override fun onReceived(customerInfo: CustomerInfo) {
                                    val owned = hasEntitlementForNonSubscription(customerInfo, storeProduct.id)
                                    Log.d(TAG, "purchase(): ITEM_ALREADY_OWNED -> restore returned; owned=$owned")
                                    if (!cont.isCompleted) {
                                        if (owned) cont.resume(customerInfo)
                                        else cont.resumeWithException(PurchaseException(PurchaseError.PURCHASE_FAILED))
                                    }
                                }

                                override fun onError(error: PurchasesError) {
                                    Log.w(TAG, "purchase(): restore after ITEM_ALREADY_OWNED failed: $error")
                                    if (!cont.isCompleted) {
                                        cont.resumeWithException(PurchaseException(PurchaseError.PURCHASE_FAILED, Exception(error.message)))
                                    }
                                }
                            })
                            return
                        }

                        val mapped = when (error.code) {
                            PurchasesErrorCode.NetworkError -> PurchaseError.NETWORK_ERROR
                            PurchasesErrorCode.PurchaseCancelledError -> PurchaseError.USER_CANCELLED
                            PurchasesErrorCode.PurchaseInvalidError -> PurchaseError.PURCHASE_FAILED
                            PurchasesErrorCode.ProductNotAvailableForPurchaseError -> PurchaseError.PRODUCT_NOT_FOUND
                            else -> PurchaseError.PURCHASE_FAILED
                        }
                        if (!cont.isCompleted) {
                            cont.resumeWithException(PurchaseException(mapped, Exception(error.message)))
                        }
                    }
                }
            )
        }
    }

    private fun hasEntitlementForNonSubscription(customerInfo: CustomerInfo, productId: String): Boolean {
        val all = customerInfo.nonSubscriptionTransactions
        val result = all.any { it.productId == productId }
        Log.d(TAG, "hasEntitlementForNonSubscription(productId=$productId) -> $result (txns=${all.size})")
        return result
    }

    override suspend fun purchaseCourse(
        courseID: String,
        userEmail: String?,
        priceTier: String
    ): Boolean = withContext(Dispatchers.Main) {
        try {
            Log.d(TAG, "purchaseCourse(): courseID=$courseID, priceTier=$priceTier, email=$userEmail")
            val attrs = mutableMapOf<String, String>()
            userEmail?.let { attrs["email"] = it }
            attrs["last_course_id"] = courseID
            Purchases.sharedInstance.setAttributes(attrs)
            Log.v(TAG, "purchaseCourse(): setAttributes=$attrs")

            val products = fetchProducts(listOf(priceTier))
            Log.d(TAG, "purchaseCourse(): fetched ${products.size} product(s) for tier=$priceTier")
            val product = products.firstOrNull()
                ?: throw PurchaseException(PurchaseError.PRODUCT_NOT_FOUND)

            val info = purchase(product)
            Log.d(TAG, "purchaseCourse(): purchase returned; allPurchasedProductIds=${info.allPurchasedProductIds}")

            if (hasEntitlementForNonSubscription(info, priceTier)) {
                Log.d(TAG, "purchaseCourse(): entitlement confirmed for $priceTier")
                true
            } else {
                throw PurchaseException(PurchaseError.PURCHASE_FAILED)
            }
        } catch (e: PurchaseException) {
            Log.w(TAG, "purchaseCourse(): failed with mapped error=${e.error}", e)
            throw e
        } catch (t: Throwable) {
            Log.e(TAG, "purchaseCourse(): unexpected error", t)
            val msg = t.message?.lowercase().orEmpty()
            if (msg.contains("network") || msg.contains("internet") || msg.contains("offline")) {
                throw PurchaseException(PurchaseError.NETWORK_ERROR, t)
            }
            throw PurchaseException(PurchaseError.PURCHASE_FAILED, t)
        }
    }

    override suspend fun getCoursePriceTier(courseID: String): String? {
        var result = coursePriceTiers[courseID]
        if (result == null) {
            try {
                val mapping = fetchCoursesPriceTiersFromServer(listOf(courseID))
                synchronized(coursePriceTiers) {
                    coursePriceTiers.putAll(mapping)
                }
                result = mapping[courseID]
            } catch (t: Throwable) {
                Log.w(TAG, "getCoursePriceTier(): failed to fetch mapping for $courseID", t)
            }
        }
        Log.d(TAG, "getCoursePriceTier($courseID) -> $result")
        return result
    }

    override suspend fun localizedPrice(tier: String): String {
        Log.d(TAG, "localizedPrice(): tier=$tier")
        val product = fetchProducts(listOf(tier)).firstOrNull()
        val price = product?.price?.formatted ?: ""
        Log.d(TAG, "localizedPrice(): tier=$tier -> '$price'")
        return price
    }

    override suspend fun getLocalizedCoursesTiers(courseIDs: List<String>): Map<String, String> {
        Log.d(TAG, "getLocalizedCoursesTiers(): requested for ${courseIDs.size} courseIDs")
        // 1) Получаем соответствия courseID -> productId (tier)
        val mapping = fetchCoursesPriceTiersFromServer(courseIDs)
        Log.d(TAG, "getLocalizedCoursesTiers(): mapping size=${mapping.size}")

        synchronized(coursePriceTiers) {
            coursePriceTiers.clear()
            coursePriceTiers.putAll(mapping)
        }

        val allTiers = mapping.values.distinct()
        Log.d(TAG, "getLocalizedCoursesTiers(): distinct tiers to fetch -> $allTiers")
        val products = fetchProducts(allTiers)
        Log.d(TAG, "getLocalizedCoursesTiers(): received ${products.size} products from store")

        val byId = products.associateBy { it.id } // id == productId
        val missing = mutableListOf<String>()
        val out = LinkedHashMap<String, String>()
        mapping.forEach { (courseId, tier) ->
            val p = byId[tier]
            if (p != null) {
                out[courseId] = p.price.formatted
            } else {
                missing.add("$courseId -> $tier")
            }
        }
        if (missing.isNotEmpty()) {
            Log.w(TAG, "getLocalizedCoursesTiers(): missing tiers (no product found): $missing")
        }
        Log.d(TAG, "getLocalizedCoursesTiers(): returning ${out.size} localized prices")
        return out
    }

    override suspend fun restorePurchases(): RestorePurchasesResult =
        suspendCancellableCoroutine { cont ->
            Purchases.sharedInstance.restorePurchases(object : ReceiveCustomerInfoCallback {
                override fun onReceived(customerInfo: CustomerInfo) {
                    Log.d(TAG, "restorePurchases(): allPurchasedProductIds=${customerInfo.allPurchasedProductIds}")
                    val any = customerInfo.allPurchasedProductIds.isNotEmpty()
                    cont.resume(if (any) RestorePurchasesResult.RESTORED else RestorePurchasesResult.NOTHING_TO_RESTORE)
                }
                override fun onError(error: PurchasesError) {
                    Log.w(TAG, "restorePurchases error: $error")
                    cont.resume(RestorePurchasesResult.NOTHING_TO_RESTORE)
                }
            })
        }
}
