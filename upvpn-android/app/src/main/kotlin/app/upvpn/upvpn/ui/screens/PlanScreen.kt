package app.upvpn.upvpn.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.upvpn.upvpn.model.UserPlan
import app.upvpn.upvpn.model.forDisplay
import app.upvpn.upvpn.model.prettyBalance
import app.upvpn.upvpn.model.prettyStartedAt
import app.upvpn.upvpn.ui.VPNAppViewModelProvider
import app.upvpn.upvpn.ui.viewmodels.BillingViewModel
import app.upvpn.upvpn.ui.viewmodels.PlanState
import app.upvpn.upvpn.ui.viewmodels.ProductInfo
import app.upvpn.upvpn.ui.viewmodels.bonusText
import app.upvpn.upvpn.ui.viewmodels.buyButtonText
import app.upvpn.upvpn.ui.viewmodels.compactFormattedPrice
import app.upvpn.upvpn.ui.viewmodels.displayText
import app.upvpn.upvpn.util.getActivityOrNull

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanScreen(
    planState: PlanState,
    isRefreshing: Boolean,
    refresh: () -> Unit,
    navigateUp: () -> Unit
) {
    val context = LocalContext.current
    val billingViewModel: BillingViewModel = viewModel(factory = VPNAppViewModelProvider.Factory)
    val prepaidProducts = billingViewModel.prepaidProducts.collectAsStateWithLifecycle()
    val yearlyProduct = billingViewModel.yearlyProduct.collectAsStateWithLifecycle()
    val selectedProduct = billingViewModel.selectedProduct.collectAsStateWithLifecycle()
    val purchaseError = billingViewModel.purchaseError.collectAsStateWithLifecycle()
    val isPurchasing = billingViewModel.isPurchasing.collectAsStateWithLifecycle()

    val state = rememberPullToRefreshState()

    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()

    // tracks pull-to-refresh UI separately from data loading state so that
    // auto refresh on screen show doesn't display the pull-to-refresh spinner
    var userInitiatedRefresh by remember { mutableStateOf(false) }

    // reload plan whenever screen show
    LaunchedEffect(lifecycleState) {
        if (lifecycleState == Lifecycle.State.RESUMED) {
            refresh()
        }
    }

    LaunchedEffect(isRefreshing) {
        if (isRefreshing.not()) {
            userInitiatedRefresh = false
        }
    }

    LaunchedEffect(Unit) {
        billingViewModel.initializeBillingClient(context)
    }

    // purchase error dialogs
    purchaseError.value?.let { errorMessage ->
        AlertDialog(
            onDismissRequest = { billingViewModel.clearPurchaseError() },
            title = { Text("Purchase") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { billingViewModel.clearPurchaseError() }) {
                    Text("OK")
                }
            }
        )
    }

    PullToRefreshBox(
        state = state,
        isRefreshing = userInitiatedRefresh && isRefreshing,
        onRefresh = {
            userInitiatedRefresh = true
            refresh()
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(text = "Plan", style = MaterialTheme.typography.titleLarge)
                    },
                    navigationIcon = {
                        IconButton(onClick = navigateUp) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Go Back"
                            )
                        }
                    }
                )
            },
            modifier = Modifier
                .fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
            ) {
                when (planState) {
                    is PlanState.Error -> Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { refresh() }
                        ) {
                            Text("Couldn't load plan, retry")
                            Icon(Icons.Default.Refresh, "reload")
                        }
                    }

                    PlanState.Loading -> Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    }

                    is PlanState.Plan -> {
                        // the yearly plan is only for sale to pay as you go users
                        val yearlyForSale = yearlyProduct.value
                            ?.takeIf { planState.userPlan is UserPlan.PayAsYouGo }

                        Box(
                            Modifier.fillMaxSize(),

                            ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(20.dp),
                                modifier = Modifier
                                    .verticalScroll(rememberScrollState())
                                    .fillMaxSize()
                            ) {
                                CurrentPlan(planState)

                                // prepaid credit is for sale on every plan: premium locations
                                // are billed from the pay as you go balance even on yearly
                                if (prepaidProducts.value.isNotEmpty()) {
                                    PrepaidPlans(
                                        prepaidProducts.value,
                                        selectedProduct.value,
                                        billingViewModel::setSelectedProduct,
                                    )
                                }

                                if (yearlyForSale != null) {
                                    YearlyPlan(
                                        yearlyForSale,
                                        selectedProduct.value,
                                        billingViewModel::setSelectedProduct,
                                    )
                                }

                                Spacer(modifier = Modifier.height(100.dp))
                            }

                            // only show buy button when at least one product is for sale
                            if (prepaidProducts.value.isNotEmpty() || yearlyForSale != null) {
                                PurchaseButton(
                                    isPurchasing.value,
                                    selectedProduct.value,
                                    billingViewModel::makePurchase,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.BottomCenter)
                                        .padding(vertical = 20.dp, horizontal = 15.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

}

@Composable
fun CurrentPlan(plan: PlanState.Plan) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 15.dp),
    ) {
        Text(
            text = "CURRENT PLAN",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(15.dp, 4.dp)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()

        ) {

            when (plan.userPlan) {
                is UserPlan.PayAsYouGo -> Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp, 10.dp)
                ) {
                    Text(
                        text = plan.userPlan.forDisplay(),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Balance ${plan.userPlan.prettyBalance()}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                is UserPlan.AnnualSubscription -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .padding(15.dp, 10.dp)
                            .fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                plan.userPlan.forDisplay(),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            plan.userPlan.content.prettyStartedAt()?.let { since ->
                                Text(
                                    text = "Since $since",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.alpha(0.7f)
                                )
                            }
                        }
                        HorizontalDivider()
                        // every plan carries a pay as you go balance: it pays for premium locations
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Pay as you go",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Balance ${plan.userPlan.prettyBalance()}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Text(
                            text = "Used for premium locations",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.alpha(0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PriceCapsule(
    price: String,
    isSelected: Boolean = false,
    // short promo drawn beside the price, e.g. "$5 FREE"
    bonus: String? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .border(
                width = 2.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            // same size for every chip, with room for "$25 $5 FREE" on one line
            .defaultMinSize(minWidth = 120.dp)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        // price and bonus side by side when they fit; otherwise the bonus moves under the
        // price as a whole, so a long localized price ("Rp 419.000 $5 FREE") never breaks
        // mid-text. The flow never exceeds the chip's column, so chips cannot overlap.
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            itemVerticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = price,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            )
            if (bonus != null) {
                Text(
                    text = bonus,
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}

@Composable
fun PrepaidPlans(
    prepaidProducts: List<ProductInfo>,
    selectedProduct: ProductInfo?,
    setSelectedProduct: (ProductInfo) -> Unit,
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 15.dp)
            .fillMaxSize()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier.padding(15.dp)
        ) {
            Text(
                "Prepaid Credit",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                "Add to Pay-as-you-go balance",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.alpha(0.7f)
            )
            HorizontalDivider(modifier = Modifier.padding(bottom = 10.dp))

            // two chips per row; sized by content so any number of products fits
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ) {
                prepaidProducts.chunked(2).forEach { rowProducts ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        rowProducts.forEach { prepaidProduct ->
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.weight(1f)
                            ) {
                                PriceCapsule(
                                    prepaidProduct.compactFormattedPrice(),
                                    isSelected = selectedProduct?.productId == prepaidProduct.productId,
                                    bonus = prepaidProduct.bonusText(),
                                    onClick = { setSelectedProduct(prepaidProduct) })
                            }
                        }
                        // keep a lone last chip in the left column
                        if (rowProducts.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun YearlyPlan(
    yearlyProduct: ProductInfo,
    selectedProduct: ProductInfo?,
    setSelectedProduct: (ProductInfo) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 15.dp)
            .clickable { setSelectedProduct(yearlyProduct) }
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(15.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("Yearly Plan", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Unlimited data. Just $3.33/mo",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.alpha(0.7f)
                )
            }
            PriceCapsule(
                "${yearlyProduct.formattedPrice}/year",
                isSelected = selectedProduct?.productId == yearlyProduct.productId,
                onClick = { setSelectedProduct(yearlyProduct) })
        }
    }
}

@Composable
fun PurchaseButton(
    isPurchasing: Boolean,
    selectedProduct: ProductInfo?,
    makePurchase: (Activity?, ProductInfo) -> Unit,
    modifier: Modifier
) {
    val context = LocalContext.current
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (selectedProduct != null) {
            Text(
                selectedProduct.displayText(),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp)
            )
        }
        Button(
            onClick = {
                selectedProduct?.let {
                    makePurchase(context.getActivityOrNull(), it)
                }
            },
            enabled = selectedProduct != null && isPurchasing.not(),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            if (isPurchasing) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.CenterVertically)
                )
            } else {
                Text(
                    selectedProduct?.buyButtonText() ?: "Buy Now",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
