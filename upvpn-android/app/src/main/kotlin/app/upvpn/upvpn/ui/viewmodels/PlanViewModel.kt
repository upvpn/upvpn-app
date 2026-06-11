package app.upvpn.upvpn.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.upvpn.upvpn.data.PlanRepository
import app.upvpn.upvpn.model.UserPlan
import com.github.michaelbull.result.fold
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


sealed class PlanState {
    data object Loading : PlanState()
    data class Plan(val userPlan: UserPlan) : PlanState()
    data class Error(val msg: String) : PlanState()
}

fun PlanState.isYearlyPlan(): Boolean {
    return when (this) {
        is PlanState.Plan -> when (this.userPlan) {
            is UserPlan.AnnualSubscription -> true
            else -> false
        }

        else -> false
    }
}

class PlanViewModel(
    private val planRepository: PlanRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private var _planState = MutableStateFlow<PlanState>(PlanState.Loading)

    val planState = _planState.asStateFlow()

    private var _isRefreshing = MutableStateFlow(false)

    val isRefreshing = _isRefreshing.asStateFlow()

    // don't fetchPlan in init because user might not yet have
    // authenticated

    fun fetchPlan() {
        // preserve existing plan data during refresh so the screen
        // doesn't flash a blank loading state when re-entered
        if (_planState.value !is PlanState.Plan) {
            _planState.update { PlanState.Loading }
        }
        _isRefreshing.update { true }
        viewModelScope.launch(dispatcher) {
            val result = planRepository.getUserPlan()
            result.fold(
                success = { plan ->
                    _planState.update { PlanState.Plan(userPlan = plan) }
                },
                failure = { error ->
                    // keep showing stale plan data on refresh failure;
                    // only surface the error screen when nothing is loaded yet
                    if (_planState.value !is PlanState.Plan) {
                        _planState.update { PlanState.Error(msg = error) }
                    }
                }
            )
            _isRefreshing.update { false }
        }
    }
}
