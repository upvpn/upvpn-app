package app.upvpn.upvpn.ui

import android.util.Log
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.window.layout.DisplayFeature
import app.upvpn.upvpn.BuildConfig
import app.upvpn.upvpn.model.Location
import app.upvpn.upvpn.ui.components.LocationsPopup
import app.upvpn.upvpn.ui.components.VPNLayout
import app.upvpn.upvpn.ui.screens.HelpScreen
import app.upvpn.upvpn.ui.screens.HomeScreen
import app.upvpn.upvpn.ui.screens.LocationScreen
import app.upvpn.upvpn.ui.screens.PlanScreen
import app.upvpn.upvpn.ui.screens.SettingsScreen
import app.upvpn.upvpn.ui.screens.SignInScreen
import app.upvpn.upvpn.ui.state.SignInState
import app.upvpn.upvpn.ui.state.getLocation
import app.upvpn.upvpn.ui.state.isVpnSessionActivityInProgress
import app.upvpn.upvpn.ui.viewmodels.AuthViewModel
import app.upvpn.upvpn.ui.viewmodels.HomeViewModel
import app.upvpn.upvpn.ui.viewmodels.LocationViewModel
import app.upvpn.upvpn.ui.viewmodels.PlanViewModel

enum class VPNScreen() {
    Login,
    Home,
    Location,
    Settings,
    Help,
    Plan,
}

@Composable
fun VPNApp(
    windowSize: WindowSizeClass,
    displayFeatures: List<DisplayFeature>,
    showSnackBar: (String) -> Unit,
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentVPNScreen = VPNScreen.valueOf(
        backStackEntry?.destination?.route ?: VPNScreen.Login.name
    )

    val authViewModel: AuthViewModel = viewModel(factory = VPNAppViewModelProvider.Factory)
    val uiState = authViewModel.uiState.collectAsStateWithLifecycle()
    val signOutUiState = authViewModel.signOutUiState.collectAsStateWithLifecycle()

    val (startDestination, userEmail) = when (uiState.value.signInState) {
        is SignInState.SignedIn -> {
            VPNScreen.Home.name to (uiState.value.signInState as SignInState.SignedIn).email
        }

        else -> {
            VPNScreen.Login.name to ""
        }
    }

    val homeVM: HomeViewModel = viewModel(factory = VPNAppViewModelProvider.Factory)
    val homeUiState = homeVM.uiState.collectAsStateWithLifecycle()
    val wgConfig = homeVM.wgConfig.collectAsStateWithLifecycle()

    val planVM: PlanViewModel = viewModel(factory = VPNAppViewModelProvider.Factory)
    val planState = planVM.planState.collectAsStateWithLifecycle()

    val locationVM: LocationViewModel = viewModel(factory = VPNAppViewModelProvider.Factory)
    val locationUiState = locationVM.uiState.collectAsStateWithLifecycle()

    val recentLocations = locationVM.recentLocations.collectAsStateWithLifecycle()

    val onLocationSelected: (location: Location) -> Unit = {
        if (homeUiState.value.vpnUiState.isVpnSessionActivityInProgress()) {
            showSnackBar("To change location, end current VPN session")
        } else {
            locationVM.onLocationSelected(it)
        }
    }

    val isSelectedLocation: (location: Location) -> Boolean = {
        locationUiState.value.selectedLocation?.code == it.code
    }

    // handle in-app notifications
    val vpnNotifications = homeVM.vpnNotificationState.collectAsStateWithLifecycle()

    // check for unauthenticated
    LaunchedEffect(key1 = vpnNotifications.value) {
        var unauthorized = false;
        for (notification in vpnNotifications.value) {
            if (notification.msg == "unauthorized") {
                unauthorized = true;
            }
        }
        if (unauthorized) {
            (authViewModel::onSignOutClick)()
        }
    }

    // show alert dialog for errors
    vpnNotifications.value.forEach { notification ->
        AlertDialog(
            onDismissRequest = { homeVM.ackVpnNotification(notification) },
            title = { Text("Oh No") },
            text = { Text(notification.msg) },
            confirmButton = {
                TextButton(onClick = { homeVM.ackVpnNotification(notification) }) {
                    Text("OK")
                }
            }
        )
    }

    // always update location from the vpn state if present
    LaunchedEffect(key1 = homeUiState.value.vpnUiState.getLocation()) {
        homeUiState.value.vpnUiState.getLocation()?.let {
            locationVM.onLocationSelected(it)
        }
    }

    if (BuildConfig.DEBUG) {
        Log.d("VPNApp", "CURRENT SCREEN: $currentVPNScreen")
    }

    // error dialogs
    uiState.value.signInError?.let { errorMessage ->
        AlertDialog(
            onDismissRequest = { authViewModel.clearSignInError() },
            title = { Text("Sign In") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { authViewModel.clearSignInError() }) {
                    Text("OK")
                }
            }
        )
    }

    uiState.value.signUpError?.let { errorMessage ->
        AlertDialog(
            onDismissRequest = { authViewModel.clearSignUpError() },
            title = { Text("Sign Up") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { authViewModel.clearSignUpError() }) {
                    Text("OK")
                }
            }
        )
    }

    signOutUiState.value.signOutError?.let { errorMessage ->
        AlertDialog(
            onDismissRequest = { authViewModel.clearSignOutError() },
            title = { Text("Sign Out") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { authViewModel.clearSignOutError() }) {
                    Text("OK")
                }
            }
        )
    }

    var showPopup by remember { mutableStateOf(false) }
    val onLocationSelectorClick = {
        showPopup = !showPopup
    }

    LocationsPopup(
        locationUiState = locationUiState.value,
        isSelectedLocation = isSelectedLocation,
        onLocationSelected = onLocationSelected,
        showPopup = showPopup,
        dismissPopup = { showPopup = false },
        onRefresh = locationVM::onRefresh
    )

    NavHost(navController = navController, startDestination = startDestination) {
        composable(route = VPNScreen.Login.name) {
            SignInScreen(
                windowSize,
                uiState.value,
                authViewModel::onEmailChange,
                authViewModel::onPasswordChange,
                authViewModel::togglePasswordVisibility,
                authViewModel::onSubmit,
                showSnackBar,
                authViewModel::setAuthAction,
                authViewModel::onSignUpCodeChange,
                authViewModel::onRequestSignUpCode,
            )
        }
        composable(route = VPNScreen.Home.name) {
            VPNLayout(
                windowSize = windowSize,
                currentVPNScreen = currentVPNScreen,
                onNavItemPressed = { screen ->
                    navController.navigate(screen.name)
                },
                modifier = modifier,
            ) {
                HomeScreen(
                    windowSize,
                    displayFeatures,
                    locationUiState.value.selectedLocation,
                    locationUiState.value,
                    homeUiState.value,
                    recentLocations.value,
                    connectPreVpnPermission = { selectedLocation ->
                        selectedLocation?.let { locationVM.addRecentLocation(it) }
                        homeVM.connectPreVpnPermission(selectedLocation)
                    },
                    homeVM::connectPostVpnPermission,
                    homeVM::disconnect,
                    onLocationSelectorClick = onLocationSelectorClick,
                    reloadLocations = locationVM::onRefresh,
                    isSelectedLocation = isSelectedLocation,
                    onLocationSelected = onLocationSelected,
                    wgConfigKV = wgConfig.value,
                )
            }
        }
        composable(route = VPNScreen.Location.name) {
            VPNLayout(
                windowSize = windowSize,
                currentVPNScreen = currentVPNScreen,
                onNavItemPressed = { screen ->
                    navController.navigate(screen.name)
                }
            ) {
                LocationScreen(
                    uiState = locationUiState.value,
                    onSearchValueChange = locationVM::onSearchValueChange,
                    onRefresh = locationVM::onRefresh,
                    clearSearchQuery = locationVM::clearSearchQuery,
                    isSelectedLocation = isSelectedLocation,
                    onLocationSelected = onLocationSelected
                )
            }
        }

        composable(route = VPNScreen.Settings.name) {
            VPNLayout(
                windowSize = windowSize,
                currentVPNScreen = currentVPNScreen,
                onNavItemPressed = { screen ->
                    navController.navigate(screen.name)
                }
            ) {
                SettingsScreen(
                    isVpnSessionActivityInProgress = homeUiState.value.vpnUiState.isVpnSessionActivityInProgress(),
                    userEmail,
                    signOutUiState.value.signOutState,
                    authViewModel::onSignOutClick,
                    navigateTo = { screen -> navController.navigate(screen.name) },
                )
            }
        }

        composable(route = VPNScreen.Help.name) {
            VPNLayout(
                windowSize = windowSize,
                currentVPNScreen = currentVPNScreen,
                onNavItemPressed = { screen ->
                    navController.navigate(screen.name)
                }) {

                HelpScreen(navigateUp = { navController.navigateUp() })

            }
        }

        composable(route = VPNScreen.Plan.name) {
            VPNLayout(
                windowSize = windowSize,
                currentVPNScreen = currentVPNScreen,
                onNavItemPressed = { screen ->
                    navController.navigate(screen.name)
                }) {

                PlanScreen(
                    planState = planState.value,
                    refresh = { planVM.fetchPlan() },
                    navigateUp = { navController.navigateUp() })
            }
        }


    }
}
