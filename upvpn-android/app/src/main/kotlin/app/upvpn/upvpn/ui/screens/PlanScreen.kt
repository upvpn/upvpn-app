package app.upvpn.upvpn.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
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
import app.upvpn.upvpn.ui.VPNAppViewModelProvider
import app.upvpn.upvpn.ui.viewmodels.BillingViewModel
import app.upvpn.upvpn.ui.viewmodels.PlanState
import app.upvpn.upvpn.ui.viewmodels.ProductInfo
import app.upvpn.upvpn.ui.viewmodels.buyButtonText
import app.upvpn.upvpn.ui.viewmodels.displayText
import app.upvpn.upvpn.ui.viewmodels.isYearlyPlan
import app.upvpn.upvpn.util.getActivityOrNull

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanScreen(planState: PlanState, refresh: () -> Unit, navigateUp: () -> Unit) {
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

    // reload plan whenever screen show
    LaunchedEffect(lifecycleState) {
        if (lifecycleState == Lifecycle.State.RESUMED) {
            refresh()
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
        isRefreshing = planState == PlanState.Loading,
        onRefresh = refresh
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

                                if (prepaidProducts.value.isEmpty()
                                        .not() && planState.isYearlyPlan().not()
                                ) {
                                    PrepaidPlans(
                                        prepaidProducts.value,
                                        selectedProduct.value,
                                        billingViewModel::setSelectedProduct,
                                    )
                                }

                                if (yearlyProduct.value != null && planState.isYearlyPlan().not()) {
                                    YearlyPlan(
                                        yearlyProduct.value!!,
                                        selectedProduct.value,
                                        billingViewModel::setSelectedProduct,
                                    )
                                }

                                Spacer(modifier = Modifier.height(100.dp))
                            }

                            // only show buy button when at least one product is available
                            if ((prepaidProducts.value.isEmpty()
                                    .not() || yearlyProduct.value != null) &&
                                planState.isYearlyPlan().not()
                            ) {
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
                        text = "Balance ${plan.userPlan.content.prettyBalance()}",
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
                        Text(
                            plan.userPlan.forDisplay(),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        HorizontalDivider()
                        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                            Icon(
                                Icons.Filled.Star,
                                "Star",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "You're on the best plan",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
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
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = price,
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        )
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

            Column(modifier = Modifier.height(100.dp), verticalArrangement = Arrangement.Center) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(25.dp)
                ) {
                    prepaidProducts.forEach { prepaidProduct ->
                        item {
                            Row(horizontalArrangement = Arrangement.Center) {
                                PriceCapsule(
                                    prepaidProduct.formattedPrice,
                                    isSelected = selectedProduct?.productId == prepaidProduct.productId,
                                    onClick = { setSelectedProduct(prepaidProduct) })
                            }
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
                    "Get unlimited data",
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
