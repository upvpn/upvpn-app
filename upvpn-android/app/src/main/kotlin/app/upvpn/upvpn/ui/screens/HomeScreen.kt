package app.upvpn.upvpn.ui.screens

import android.app.Activity
import android.content.res.Configuration
import android.net.VpnService
import android.os.SystemClock
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.window.layout.DisplayFeature
import app.upvpn.upvpn.model.Location
import app.upvpn.upvpn.model.random
import app.upvpn.upvpn.service.client.WgConfigKV
import app.upvpn.upvpn.ui.VPNScreen
import app.upvpn.upvpn.ui.components.AllLocations
import app.upvpn.upvpn.ui.components.LocationComponent
import app.upvpn.upvpn.ui.components.LocationSelector
import app.upvpn.upvpn.ui.components.Logo
import app.upvpn.upvpn.ui.components.StatsCard
import app.upvpn.upvpn.ui.components.VPNLayout
import app.upvpn.upvpn.ui.state.HomeUiState
import app.upvpn.upvpn.ui.state.LocationUiState
import app.upvpn.upvpn.ui.state.VpnUiState
import app.upvpn.upvpn.ui.state.progress
import app.upvpn.upvpn.ui.state.shieldResourceId
import app.upvpn.upvpn.ui.state.switchChecked
import app.upvpn.upvpn.ui.state.switchEnabled
import app.upvpn.upvpn.ui.state.vpnDisplayText
import app.upvpn.upvpn.ui.theme.UpVPNTheme
import app.upvpn.upvpn.util.locationForPreview
import app.upvpn.upvpn.util.msTimerString
import com.google.accompanist.adaptive.HorizontalTwoPaneStrategy
import com.google.accompanist.adaptive.TwoPane
import com.google.accompanist.adaptive.VerticalTwoPaneStrategy
import kotlinx.coroutines.delay


@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(showSystemUi = true)
@Composable
fun PreviewHomeScreen() {
    val locations = listOf<Location>().random(5)
    VPNLayout(windowSize = WindowSizeClass.calculateFromSize(DpSize(300.dp, 720.dp)),
        currentVPNScreen = VPNScreen.Home,
        onNavItemPressed = {}) {
        HomeScreen(
            windowSize = WindowSizeClass.calculateFromSize(DpSize(300.dp, 720.dp)),
            displayFeatures = listOf(),
            selectedLocation = locationForPreview(),
            locationUiState = LocationUiState(locations = locations),
            uiState =
            HomeUiState(),
            recentLocations = locations,
            connectPreVpnPermission = {},
            connectPostVpnPermission = { _, _ -> {} },
            disconnect = {},
            onLocationSelectorClick = {},
            reloadLocations = {},
            isSelectedLocation = { it == locations.first() },
            onLocationSelected = {},
            null
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(device = Devices.DEFAULT, widthDp = 300, heightDp = 600)
@Composable
fun PreviewSmallHomeScreen() {
    val locations = listOf<Location>().random(5)
    VPNLayout(windowSize = WindowSizeClass.calculateFromSize(DpSize(240.dp, 400.dp)),
        currentVPNScreen = VPNScreen.Home,
        onNavItemPressed = {}) {
        HomeScreen(
            windowSize = WindowSizeClass.calculateFromSize(DpSize(240.dp, 400.dp)),
            displayFeatures = listOf(),
            selectedLocation = locationForPreview(),
            locationUiState = LocationUiState(locations = locations),
            uiState =
            HomeUiState(),
            recentLocations = locations,
            connectPreVpnPermission = {},
            connectPostVpnPermission = { _, _ -> {} },
            disconnect = {},
            onLocationSelectorClick = {},
            reloadLocations = {},
            isSelectedLocation = { it == locations.first() },
            onLocationSelected = {},
            null
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(device = Devices.AUTOMOTIVE_1024p, widthDp = 1024, heightDp = 360)
@Composable
fun PreviewHomeScreenCompactWidthMediumHeight() {
    val locations = listOf<Location>().random(5)
    VPNLayout(windowSize = WindowSizeClass.calculateFromSize(DpSize(720.dp, 320.dp)),
        currentVPNScreen = VPNScreen.Home,
        onNavItemPressed = {}) {
        HomeScreen(
            windowSize = WindowSizeClass.calculateFromSize(DpSize(720.dp, 320.dp)),
            displayFeatures = listOf(),
            selectedLocation = locationForPreview(),
            locationUiState = LocationUiState(locations = locations),
            uiState =
            HomeUiState(),
            recentLocations = locations,
            connectPreVpnPermission = {},
            connectPostVpnPermission = { _, _ -> {} },
            disconnect = {},
            onLocationSelectorClick = {},
            reloadLocations = {},
            isSelectedLocation = { it == locations.first() },
            onLocationSelected = {},
            null
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(device = "spec:width=1280dp,height=800dp,dpi=240")
@Composable
fun PreviewHomeScreenMedium() {
    UpVPNTheme(darkTheme = false) {
        val locations = listOf<Location>().random(5)
        VPNLayout(windowSize = WindowSizeClass.calculateFromSize(DpSize(720.dp, 720.dp)),
            currentVPNScreen = VPNScreen.Home,
            onNavItemPressed = {}) {
            HomeScreen(
                windowSize = WindowSizeClass.calculateFromSize(DpSize(720.dp, 720.dp)),
                displayFeatures = listOf(),
                selectedLocation = locationForPreview(),
                locationUiState = LocationUiState(locations = locations),
                uiState =
                HomeUiState(),
                recentLocations = listOf<Location>().random(5),
                connectPreVpnPermission = {},
                connectPostVpnPermission = { _, _ -> {} },
                disconnect = {},
                onLocationSelectorClick = {},
                reloadLocations = {},
                isSelectedLocation = { it == locations.first() },
                onLocationSelected = {},
                null
            )
        }
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(showSystemUi = true, device = Devices.TV_1080p)
@Composable
fun PreviewHomeScreenLarge() {
    val locations = listOf<Location>().random(5)
    VPNLayout(windowSize = WindowSizeClass.calculateFromSize(DpSize(1080.dp, 1024.dp)),
        currentVPNScreen = VPNScreen.Home,
        onNavItemPressed = {}) {
        HomeScreen(
            windowSize = WindowSizeClass.calculateFromSize(DpSize(1080.dp, 1024.dp)),
            displayFeatures = listOf(),
            selectedLocation = locationForPreview(),
            locationUiState = LocationUiState(locations = locations),
            uiState =
            HomeUiState(
                vpnUiState = VpnUiState.Connected(
                    locations.first(),
                    SystemClock.currentThreadTimeMillis()
                )
            ),
            recentLocations = listOf<Location>().random(5),
            connectPreVpnPermission = {},
            connectPostVpnPermission = { _, _ -> {} },
            disconnect = {},
            onLocationSelectorClick = {},
            reloadLocations = {},
            isSelectedLocation = { it == locations.first() },
            onLocationSelected = {},
            null,
        )
    }
}

@Composable
fun HomeCardAndRecentRow(
    homeCardModifier: Modifier,
    recentLocationsModifier: Modifier,
    selectedLocation: Location?,
    locationUiState: LocationUiState,
    uiState: HomeUiState,
    recentLocations: List<Location>,
    connectPreVpnPermission: (Location?) -> Unit,
    connectPostVpnPermission: (Boolean, Location?) -> Unit,
    disconnect: () -> Unit,
    onLocationSelectorClick: () -> Unit,
    reloadLocations: () -> Unit,
    isSelectedLocation: (Location) -> Boolean,
    onLocationSelected: (Location) -> Unit,
    wgConfigKV: WgConfigKV?,
) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
            HomeCard(
                selectedLocation,
                locationUiState,
                uiState.vpnUiState,
                connectPreVpnPermission,
                connectPostVpnPermission,
                disconnect,
                onLocationSelectorClick,
                reloadLocations,
                homeCardModifier.weight(1f)
            )
            RecentLocationsCard(
                recentLocations,
                isSelectedLocation,
                onLocationSelected,
                wgConfigKV,
                recentLocationsModifier.weight(1f)
            )
        }
    }
}

@Composable
fun HomeCardAndRecentColumn(
    windowSize: WindowSizeClass,
    homeCardModifier: Modifier,
    recentLocationsModifier: Modifier,
    selectedLocation: Location?,
    locationUiState: LocationUiState,
    uiState: HomeUiState,
    recentLocations: List<Location>,
    connectPreVpnPermission: (Location?) -> Unit,
    connectPostVpnPermission: (Boolean, Location?) -> Unit,
    disconnect: () -> Unit,
    onLocationSelectorClick: () -> Unit,
    reloadLocations: () -> Unit,
    isSelectedLocation: (Location) -> Boolean,
    onLocationSelected: (Location) -> Unit,
    wgConfigKV: WgConfigKV?,
) {
    Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxSize()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(15.dp),
            modifier = Modifier.fillMaxHeight()
        ) {
            HomeCard(
                selectedLocation,
                locationUiState,
                uiState.vpnUiState,
                connectPreVpnPermission,
                connectPostVpnPermission,
                disconnect,
                onLocationSelectorClick,
                reloadLocations,
                homeCardModifier.weight(0.65f)
            )
            // on large/long screen if height is compact dont show recent card
            if (windowSize.heightSizeClass != WindowHeightSizeClass.Compact) {
                RecentLocationsCard(
                    recentLocations,
                    isSelectedLocation,
                    onLocationSelected,
                    wgConfigKV,
                    recentLocationsModifier.weight(0.35f)
                )
            } else {
                // still have spacedBy work for one home card from bottom of screen
                Spacer(modifier = Modifier.height(0.dp))
            }
        }
    }
}

@Composable
fun HomeScreen(
    windowSize: WindowSizeClass,
    displayFeatures: List<DisplayFeature>,
    selectedLocation: Location?,
    locationUiState: LocationUiState,
    uiState: HomeUiState,
    recentLocations: List<Location>,
    connectPreVpnPermission: (Location?) -> Unit,
    connectPostVpnPermission: (Boolean, Location?) -> Unit,
    disconnect: () -> Unit,
    onLocationSelectorClick: () -> Unit,
    reloadLocations: () -> Unit,
    isSelectedLocation: (Location) -> Boolean,
    onLocationSelected: (Location) -> Unit,
    wgConfigKV: WgConfigKV?,
) {
    val isPortrait = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT

    val homeCardModifier = when (windowSize.widthSizeClass) {
        WindowWidthSizeClass.Compact -> Modifier.padding(15.dp, 15.dp, 15.dp, 0.dp)
        WindowWidthSizeClass.Medium -> Modifier.padding(15.dp, 15.dp, 0.dp, 15.dp)
        else -> if (isPortrait.not()) {
            Modifier.padding(15.dp, 15.dp, 15.dp, 0.dp)
        } else {
            Modifier.padding(15.dp, 15.dp, 0.dp, 15.dp)
        }
    }

    val recentLocationsModifier = when (windowSize.widthSizeClass) {
        WindowWidthSizeClass.Compact -> Modifier.padding(15.dp, 0.dp, 15.dp, 15.dp)
        WindowWidthSizeClass.Medium -> Modifier.padding(0.dp, 15.dp, 15.dp, 15.dp)
        else -> if (isPortrait.not()) {
            Modifier.padding(15.dp, 0.dp, 15.dp, 15.dp)
        } else {
            Modifier.padding(0.dp, 15.dp, 15.dp, 15.dp)
        }
    }

    val allLocationsModifier =
        if (windowSize.widthSizeClass == WindowWidthSizeClass.Medium || isPortrait) {
            Modifier.padding(15.dp, 0.dp, 15.dp, 15.dp)
        } else {
            Modifier.padding(0.dp, 15.dp, 15.dp, 15.dp)
        }

    TwoPane(
        first = {
            when (windowSize.widthSizeClass) {
                WindowWidthSizeClass.Compact -> HomeCard(
                    selectedLocation,
                    locationUiState,
                    uiState.vpnUiState,
                    connectPreVpnPermission,
                    connectPostVpnPermission,
                    disconnect,
                    onLocationSelectorClick,
                    reloadLocations,
                    homeCardModifier
                )

                WindowWidthSizeClass.Medium -> HomeCardAndRecentRow(
                    homeCardModifier,
                    recentLocationsModifier,
                    selectedLocation,
                    locationUiState,
                    uiState,
                    recentLocations,
                    connectPreVpnPermission,
                    connectPostVpnPermission,
                    disconnect,
                    onLocationSelectorClick,
                    reloadLocations,
                    isSelectedLocation,
                    onLocationSelected,
                    wgConfigKV,
                )

                else ->
                    if (isPortrait.not()) {
                        HomeCardAndRecentColumn(
                            windowSize,
                            homeCardModifier,
                            recentLocationsModifier,
                            selectedLocation,
                            locationUiState,
                            uiState,
                            recentLocations,
                            connectPreVpnPermission,
                            connectPostVpnPermission,
                            disconnect,
                            onLocationSelectorClick,
                            reloadLocations,
                            isSelectedLocation,
                            onLocationSelected,
                            wgConfigKV,
                        )
                    } else {
                        // when both w & h are expanded, but table is in portrait mode
                        // avoid showing thin layer of home card + recent in column
                        HomeCardAndRecentRow(
                            homeCardModifier,
                            recentLocationsModifier,
                            selectedLocation,
                            locationUiState,
                            uiState,
                            recentLocations,
                            connectPreVpnPermission,
                            connectPostVpnPermission,
                            disconnect,
                            onLocationSelectorClick,
                            reloadLocations,
                            isSelectedLocation,
                            onLocationSelected,
                            wgConfigKV,
                        )
                    }

            }
        },
        second = {
            when (windowSize.widthSizeClass) {
                WindowWidthSizeClass.Compact -> RecentLocationsCard(
                    recentLocations,
                    isSelectedLocation,
                    onLocationSelected,
                    wgConfigKV,
                    recentLocationsModifier
                )

                WindowWidthSizeClass.Medium -> AllLocationsCard(
                    locationUiState,
                    reloadLocations,
                    isSelectedLocation,
                    onLocationSelected,
                    modifier = allLocationsModifier
                )

                else -> AllLocationsCard(
                    locationUiState,
                    reloadLocations,
                    isSelectedLocation,
                    onLocationSelected,
                    modifier = allLocationsModifier
                )
            }
        },
        strategy = when (windowSize.widthSizeClass) {
            WindowWidthSizeClass.Compact -> {
                if (windowSize.heightSizeClass == WindowHeightSizeClass.Compact) {
                    // both width and height are small do not display recent card
                    // gap for bottom of the screen
                    VerticalTwoPaneStrategy(1f, 25.dp)
                } else {
                    VerticalTwoPaneStrategy(0.65f, 15.dp)
                }
            }

            WindowWidthSizeClass.Medium -> VerticalTwoPaneStrategy(
                if (windowSize.heightSizeClass == WindowHeightSizeClass.Compact) {
                    1f
                } else if (windowSize.heightSizeClass == WindowHeightSizeClass.Medium) {
                    0.5f
                } else {
                    0.4f
                }
            )

            else -> {
                if (isPortrait.not()) {
                    HorizontalTwoPaneStrategy(0.4f)
                } else {
                    // when both w & h are expanded, but table is in portrait mode
                    // avoid showing thin layer of home card + recent in column
                    VerticalTwoPaneStrategy(0.4f)
                }
            }
        },
        displayFeatures = listOf()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeCardDivider(vpnUiState: VpnUiState) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(25.dp)
    ) {
        when (vpnUiState) {
            is VpnUiState.Checking,
            is VpnUiState.Disconnecting,
            is VpnUiState.Disconnected -> Divider(
                thickness = 2.dp,
                color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.2f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 35.dp)
            )

            is VpnUiState.Requesting,
            is VpnUiState.Accepted,
            is VpnUiState.ServerCreated,
            is VpnUiState.ServerRunning,
            is VpnUiState.ServerReady,
            is VpnUiState.Connecting -> LinearProgressIndicator(
                progress = { vpnUiState.progress() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 35.dp),
            )

            is VpnUiState.Connected -> {
                var elapsedTimeMs by remember {
                    mutableLongStateOf(SystemClock.elapsedRealtime() - vpnUiState.time)
                }
                LaunchedEffect(key1 = vpnUiState.time) {
                    while (true) {
                        delay(1000)
                        elapsedTimeMs = SystemClock.elapsedRealtime() - vpnUiState.time
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 35.dp)
                ) {

                    HorizontalDivider(thickness = 2.dp, modifier = Modifier.weight(1f))

                    SuggestionChip(
                        onClick = { },
                        label = { Text(text = elapsedTimeMs.msTimerString(), fontSize = 18.sp) },
                        shape = RoundedCornerShape(15.dp),
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )

                    HorizontalDivider(thickness = 2.dp, modifier = Modifier.weight(1f))
                }

            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeCard(
    selectedLocation: Location?,
    locationUiState: LocationUiState,
    vpnUiState: VpnUiState,
    connectPreVpnPermission: (Location?) -> Unit,
    connectPostVpnPermission: (Boolean, Location?) -> Unit,
    disconnect: () -> Unit,
    onLocationSelectorClick: () -> Unit,
    reloadLocations: () -> Unit,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current
    val vpnPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {
            connectPostVpnPermission(it.resultCode == Activity.RESULT_OK, selectedLocation)
        })

    val onCheckedChange: (Boolean) -> Unit = when (vpnUiState.switchChecked()) {
        false -> {
            {
                connectPreVpnPermission(selectedLocation)
                val intent = VpnService.prepare(context)
                if (intent == null) {
                    // already have permission
                    connectPostVpnPermission(true, selectedLocation)
                } else {
                    vpnPermissionLauncher.launch(intent)
                }
            }
        }

        else -> {
            {
                disconnect()
            }
        }
    }

    Card(
        modifier = modifier
            .heightIn(400.dp, 600.dp)
            .widthIn(400.dp, 600.dp)
            .fillMaxSize()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    painter = painterResource(id = vpnUiState.shieldResourceId()),
                    contentDescription = "Shield",
                    modifier = Modifier
                        .fillMaxHeight(0.3f)
                        .aspectRatio(1f)
                )

                when (vpnUiState) {
                    is VpnUiState.Connected -> {}
                    else ->
                        SuggestionChip(
                            onClick = { },
                            label = {
                                Text(
                                    text = vpnUiState.vpnDisplayText(),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            },
                            shape = RoundedCornerShape(15.dp),
                            modifier = Modifier
                                .fillMaxHeight(0.1f)
                        )
                }

                HomeCardDivider(vpnUiState)
                LocationSelector(
                    selectedLocation = selectedLocation,
                    vpnUiState = vpnUiState,
                    onLocationSelectorClick = onLocationSelectorClick,
                )
                Switch(
                    enabled = vpnUiState.switchEnabled(),
                    checked = vpnUiState.switchChecked(),
                    onCheckedChange = onCheckedChange
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecentLocationsCard(
    recentLocations: List<Location>,
    isSelectedLocation: (Location) -> Boolean,
    onLocationSelected: (Location) -> Unit,
    wgConfigKV: WgConfigKV? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .heightIn(400.dp, 600.dp)
            .widthIn(400.dp, 600.dp)
            .fillMaxSize()
    ) {
        if (wgConfigKV != null) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                StatsCard(wgConfigKV)
            }
        } else {
            if (recentLocations.isEmpty()) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Logo()
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.padding(10.dp, 15.dp, 10.dp, 10.dp)
                ) {
                    Text(
                        text = "Recent Locations".uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(12.dp, 0.dp, 0.dp, 0.dp)
                    )
                    LazyColumn(
//                verticalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(recentLocations.size) {
                            LocationComponent(
                                location = recentLocations[recentLocations.size - it - 1],
                                isSelectedLocation = isSelectedLocation,
                                onLocationSelected = onLocationSelected,
                                modifier = Modifier.animateItemPlacement()
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AllLocationsCard(
    locationUiState: LocationUiState,
    onRefresh: () -> Unit,
    isSelectedLocation: (Location) -> Boolean,
    onLocationSelected: (Location) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            AllLocations(
                locationUiState = locationUiState,
                verticalCountrySpacing = 0.dp,
                onRefresh = onRefresh,
                isSelectedLocation = isSelectedLocation,
                onLocationSelected = onLocationSelected
            )
        }
    }
}
