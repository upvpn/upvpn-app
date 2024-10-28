package app.upvpn.upvpn.ui.components

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.upvpn.upvpn.ui.screens.PlanScreen
import app.upvpn.upvpn.ui.viewmodels.PlanState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanBottomSheet(
    showPlanSheet: Boolean,
    dismissPlanSheet: () -> Unit,
    planState: PlanState,
    refresh: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false,
    )

    if (showPlanSheet) {
        ModalBottomSheet(
            modifier = Modifier.fillMaxHeight(),
            sheetState = sheetState,
            onDismissRequest = dismissPlanSheet
        ) {
            PlanScreen(planState, refresh, dismissPlanSheet)
        }
    }
}
