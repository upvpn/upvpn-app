package app.upvpn.upvpn.ui

import android.util.Log
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import app.upvpn.upvpn.ui.components.VPNLayout
import app.upvpn.upvpn.ui.screens.HomeScreen
import app.upvpn.upvpn.ui.screens.LocationScreen
import app.upvpn.upvpn.ui.screens.SettingsScreen
import app.upvpn.upvpn.ui.screens.SignInScreen
import app.upvpn.upvpn.ui.state.SignInState
import app.upvpn.upvpn.ui.state.isVpnSessionActivityInProgress
import app.upvpn.upvpn.ui.viewmodels.HomeViewModel
import app.upvpn.upvpn.ui.viewmodels.LocationViewModel
import app.upvpn.upvpn.ui.viewmodels.SignInViewModel

enum class VPNScreen() {
    Login,
    Home,
    Location,
    Settings
}

@Composable
fun VPNApp(
    windowSize: WindowSizeClass,
    displayFeatures: List<DisplayFeature>,
    resizeWindow: (Boolean) -> Unit,
    showSnackBar: (String) -> Unit,
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentVPNScreen = VPNScreen.valueOf(
        backStackEntry?.destination?.route ?: VPNScreen.Login.name
    )

    val vm: SignInViewModel = viewModel(factory = VPNAppViewModelProvider.Factory)
    val uiState = vm.uiState.collectAsStateWithLifecycle()
    val signOutUiState = vm.signOutUiState.collectAsStateWithLifecycle()

    val (startDestination, userEmail) = when (uiState.value.signInState) {
        is SignInState.SignedIn -> {
            resizeWindow(false)
            VPNScreen.Home.name to (uiState.value.signInState as SignInState.SignedIn).email
        }

        else -> {
            resizeWindow(true)
            VPNScreen.Login.name to ""
        }
    }

    val homeVM: HomeViewModel = viewModel(factory = VPNAppViewModelProvider.Factory)
    val homeUiState = homeVM.uiState.collectAsStateWithLifecycle()

    val locationVM: LocationViewModel = viewModel(factory = VPNAppViewModelProvider.Factory)
    val locationUiState = locationVM.uiState.collectAsStateWithLifecycle()

    val recentLocations = locationVM.recentLocations.collectAsStateWithLifecycle()

    val onLocationSelected: (location: Location) -> Unit = {
        if (homeUiState.value.vpnUiState.isVpnSessionActivityInProgress()) {
            showSnackBar("VPN session is in progress")
        } else {
            locationVM.onLocationSelected(it)
        }
    }

    val isSelectedLocation: (location: Location) -> Boolean = {
        locationUiState.value.selectedLocation?.code == it.code
    }

    // handle in-app notifications
    val vpnNotifications = homeVM.vpnNotificationState.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = vpnNotifications.value) {
        for (notification in vpnNotifications.value) {
            showSnackBar(notification.msg)
            homeVM.ackVpnNotification(notification)
        }
    }

    if(BuildConfig.DEBUG) {
        Log.d("VPNApp", "CURRENT SCREEN: $currentVPNScreen")
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(route = VPNScreen.Login.name) {
            SignInScreen(
                windowSize,
                uiState.value,
                vm::onEmailChange,
                vm::onPasswordChange,
                vm::togglePasswordVisibility,
                vm::onSignInClick,
                showSnackBar,
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
                    locationUiState.value.locationState,
                    homeUiState.value,
                    recentLocations.value,
                    homeVM::connectPreVpnPermission,
                    homeVM::connectPostVpnPermission,
                    homeVM::disconnect,
                    openLocationScreen = {
                        navController.navigate(VPNScreen.Location.name)
                    },
                    reloadLocations = locationVM::onRefresh,
                    isSelectedLocation = isSelectedLocation,
                    onLocationSelected = onLocationSelected
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
                    isVpnSessionActivityInProgress = homeUiState.value.vpnUiState.isVpnSessionActivityInProgress(),
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
                    userEmail, signOutUiState.value.signOutState, vm::onSignOutClick
                )
            }
        }
    }
}
