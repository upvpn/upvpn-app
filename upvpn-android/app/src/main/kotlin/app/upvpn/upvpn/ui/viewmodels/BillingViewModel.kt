package app.upvpn.upvpn.ui.viewmodels

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.upvpn.upvpn.BuildConfig
import app.upvpn.upvpn.data.PlanRepository
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.queryProductDetails
import com.github.michaelbull.result.fold
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.min

data class ProductInfo(
    val productId: String,
    val name: String,
    val description: String,
    val formattedPrice: String,
    val productDetails: ProductDetails
)

@SuppressLint("DefaultLocale")
fun ProductInfo.prepaidUSD(): String? {
    val cents = productId.split(".").getOrNull(1)
    return cents?.toInt()?.let { String.format("$%.2f", it / 100f) }
}

fun ProductInfo.displayText(): String {
    return when (productDetails.productType) {
        ProductType.INAPP -> "Prepaid balance never expires. A VPN session is charged $0.02/hr + $0.04/GB + $0.05 base"
        ProductType.SUBS -> "Yearly at just $3.33/mo. Unlimited data."
        else -> ""
    }
}

fun ProductInfo.buyButtonText(): String {
    return when (productDetails.productType) {
        ProductType.INAPP -> "Add ${prepaidUSD()} to balance"
        ProductType.SUBS -> "Upgrade"
        else -> ""
    }
}


class BillingViewModel(
    private val planRepository: PlanRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    private val tag = "BillingViewModel"
    private var billingClient: BillingClient? = null

    private var retryCount = 0
    private val maxRetryAttempts = 5
    private var retryJob: Job? = null

    private val prepaidProductIds =
        listOf("prepaid.99", "prepaid.199", "prepaid.499", "prepaid.999")
    private val yearlyProductId = "subscription.yearly"

    private val _prepaidProducts = MutableStateFlow<List<ProductInfo>>(emptyList())
    val prepaidProducts = _prepaidProducts.asStateFlow()

    private val _yearlyProduct = MutableStateFlow<ProductInfo?>(null)
    val yearlyProduct = _yearlyProduct.asStateFlow()

    private val _selectedProduct = MutableStateFlow<ProductInfo?>(null)
    val selectedProduct = _selectedProduct.asStateFlow()

    private val _purchaseError = MutableStateFlow<String?>(null)
    val purchaseError = _purchaseError.asStateFlow()

    private val _isPurchasing = MutableStateFlow<Boolean>(false)
    val isPurchasing = _isPurchasing.asStateFlow()

    private fun getRetryDelay(): Long {
        return min(1L shl retryCount, 64) * 1000
    }

    fun initializeBillingClient(context: Context) {
        if (billingClient?.isReady == true) {
            return
        }

        billingClient = BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
            )
            .build()

        startBillingConnection()
    }

    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                for (purchase in purchases) {
                    // todo
                    if (BuildConfig.DEBUG) {
                        Log.d(tag, "purchased: $purchase")
                    }
                }

            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                Log.d(tag, "purchase cancelled")
            } else {
                Log.d(tag, "Purchase error: ${billingResult.debugMessage}")
            }
            _isPurchasing.update { false }
        }


    private fun startBillingConnection() {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.OK -> {
                        if (BuildConfig.DEBUG) {
                            Log.d(tag, "Billing client connected")
                        }
                        retryCount = 0
                        retryJob?.cancel()
                        // Fetch products once connection is established
                        viewModelScope.launch {
                            fetchProducts()
                        }
                    }

                    else -> {
                        if (BuildConfig.DEBUG) {
                            Log.d(tag, "Billing client setup failed: ${billingResult.debugMessage}")
                        }
                        retryConnection()
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                if (BuildConfig.DEBUG) {
                    Log.d(tag, "Billing service disconnected")
                }
                retryConnection()
            }
        })
    }

    private fun retryConnection() {
        retryJob?.cancel()

        if (retryCount >= maxRetryAttempts) {
            if (BuildConfig.DEBUG) {
                Log.d(tag, "Failed to connect after $maxRetryAttempts attempts")
            }
            return
        }

        retryJob = viewModelScope.launch {
            val delay = getRetryDelay()
            if (BuildConfig.DEBUG) {
                Log.d(
                    tag,
                    "Retrying connection in ${delay / 1000} seconds... (Attempt ${retryCount + 1}/$maxRetryAttempts)"
                )
            }
            delay(delay)
            retryCount++
            startBillingConnection()
        }
    }

    suspend fun fetchProducts() {
        viewModelScope.launch {
            fetchPrepaidProducts()
            fetchYearlyProduct()
        }
    }

    private suspend fun fetchPrepaidProducts() {
        val params = QueryProductDetailsParams.newBuilder().setProductList(
            prepaidProductIds.map {
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(it)
                    .setProductType(ProductType.INAPP)
                    .build()
            }
        ).build()
        val result = billingClient?.queryProductDetails(params)

        val newPrepaidProduct = mutableListOf<ProductInfo>()

        if (result?.billingResult?.responseCode == BillingClient.BillingResponseCode.OK) {
            result.productDetailsList?.forEach { productDetails ->
                newPrepaidProduct.add(
                    ProductInfo(
                        productDetails.productId,
                        productDetails.name,
                        productDetails.description,
                        productDetails.oneTimePurchaseOfferDetails?.formattedPrice ?: "",
                        productDetails
                    )
                )
            }
            _prepaidProducts.update { newPrepaidProduct.sortedBy { it.productDetails.oneTimePurchaseOfferDetails?.priceAmountMicros } }

        }
    }

    private suspend fun fetchYearlyProduct() {
        val params = QueryProductDetailsParams.newBuilder().setProductList(
            listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(yearlyProductId)
                    .setProductType(ProductType.SUBS)
                    .build()
            )
        ).build()

        val result = billingClient?.queryProductDetails(params)

        var newYearlyProduct: ProductInfo? = null

        if (result?.billingResult?.responseCode == BillingClient.BillingResponseCode.OK) {
            result.productDetailsList?.forEach { productDetails ->
                newYearlyProduct = ProductInfo(
                    productDetails.productId,
                    productDetails.name,
                    productDetails.description,
                    productDetails.subscriptionOfferDetails?.first()?.pricingPhases?.pricingPhaseList?.first()?.formattedPrice
                        ?: "",
                    productDetails
                )
            }
            _yearlyProduct.update { newYearlyProduct }
        }
    }

    fun setSelectedProduct(productInfo: ProductInfo) {
        // only allow setting product when not purchasing
        if (_isPurchasing.value.not()) {
            _selectedProduct.update { productInfo }
        }
    }

    fun isSelectedProduct(productInfo: ProductInfo): Boolean {
        return _selectedProduct.value?.productId == productInfo.productId
    }

    fun clearPurchaseError() {
        _purchaseError.update { null }
    }

    fun makePurchase(activity: Activity?, productInfo: ProductInfo) {
        if (activity == null) {
            _purchaseError.update { "cannot start billing flow: no activity" }
            return
        }

        if (billingClient?.isReady == true) {
            _isPurchasing.update { true }
            when (productInfo.productDetails.productType) {
                ProductType.INAPP -> viewModelScope.launch { purchaseInApp(activity, productInfo) }
                ProductType.SUBS -> viewModelScope.launch { purchaseSub(activity, productInfo) }
            }
        } else {
            _purchaseError.update { "Billing client is not ready" }
        }
    }

    private suspend fun purchaseInApp(activity: Activity, productInfo: ProductInfo) {

        val emailAndDeviceId = planRepository.getEmailAndDeviceId()

        emailAndDeviceId.fold(
            success = {
                val billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(
                        listOf(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productInfo.productDetails)
                                .build()
                        )
                    )
                    .setObfuscatedAccountId(it.deviceId.toString())
                    .setObfuscatedProfileId(it.email)
                    .build()

                val result = billingClient?.launchBillingFlow(activity, billingFlowParams)

                if (result != null && result.responseCode != BillingResponseCode.OK) {
                    if (BuildConfig.DEBUG) {
                        Log.d(tag, result.debugMessage)
                    }
                    _purchaseError.update { "cannot launch billing flow" }
                }
            },
            failure = {
                _purchaseError.update { it }
            }
        )
    }

    private suspend fun purchaseSub(activity: Activity, productInfo: ProductInfo) {
        val emailAndDeviceId = planRepository.getEmailAndDeviceId()

        emailAndDeviceId.fold(
            success = {
                val offerToken =
                    productInfo.productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken

                val params = BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productInfo.productDetails)

                if (offerToken != null) {
                    params.setOfferToken(offerToken)
                }

                val billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(listOf(params.build()))
                    .setObfuscatedAccountId(it.deviceId.toString())
                    .setObfuscatedProfileId(it.email)
                    .build()

                val result = billingClient?.launchBillingFlow(activity, billingFlowParams)

                if (result != null && result.responseCode != BillingResponseCode.OK) {
                    if (BuildConfig.DEBUG) {
                        Log.d(tag, result.debugMessage)
                    }
                    _purchaseError.update { "cannot launch billing flow" }
                }
            },
            failure = {
                _purchaseError.update { it }
            }
        )
    }

    override fun onCleared() {
        super.onCleared()
        retryJob?.cancel()
        billingClient?.endConnection()
    }
}
