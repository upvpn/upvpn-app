package app.upvpn.upvpn.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.upvpn.upvpn.model.UserPlan
import app.upvpn.upvpn.model.forDisplay
import app.upvpn.upvpn.model.prettyBalance
import app.upvpn.upvpn.ui.viewmodels.PlanState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanScreen(planState: PlanState, refresh: () -> Unit, navigateUp: () -> Unit) {

    val state = rememberPullToRefreshState()

    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()

    // reload plan whenever screen show
    LaunchedEffect(lifecycleState) {
        if (lifecycleState == Lifecycle.State.RESUMED) {
            refresh()
        }
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
                        Column(
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                                .fillMaxSize()
                        ) {
                            CurrentPlan(planState)
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
