package app.upvpn.upvpn.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.upvpn.upvpn.VPNApplication
import app.upvpn.upvpn.ui.viewmodels.AuthViewModel
import app.upvpn.upvpn.ui.viewmodels.BillingViewModel
import app.upvpn.upvpn.ui.viewmodels.HomeViewModel
import app.upvpn.upvpn.ui.viewmodels.LocationViewModel
import app.upvpn.upvpn.ui.viewmodels.PlanViewModel

object VPNAppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            val application =
                (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as VPNApplication)
            AuthViewModel(
                application.container.serviceConnectionManager,
                application.container.vpnRepository
            )
        }

        initializer {
            val application =
                (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as VPNApplication)
            LocationViewModel(application.container.vpnRepository)
        }

        initializer {
            val application =
                (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as VPNApplication)
            HomeViewModel(
                application.container.serviceConnectionManager,
                application.container.vpnRepository,
                application.container.inAppReviewManager
            )
        }

        initializer {
            val application =
                (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as VPNApplication)
            PlanViewModel(application.container.planRepository)
        }

        initializer {
            val application =
                (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as VPNApplication)
            BillingViewModel(application.container.planRepository)
        }
    }
}
