package app.upvpn.upvpn.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.upvpn.upvpn.R
import app.upvpn.upvpn.ui.VPNScreen


val NAVIGATION_SCREENS = listOf(
    VPNScreen.Home,
    VPNScreen.Location,
    VPNScreen.Settings,
)

private data class VPNNavigationItem(
    val screen: VPNScreen,
    val icon: ImageVector,
    val text: String,
)

private fun allVpnNavigationItems(): List<VPNNavigationItem> = listOf(
    VPNNavigationItem(VPNScreen.Location, Icons.Default.LocationOn, VPNScreen.Location.name),
    VPNNavigationItem(VPNScreen.Home, Icons.Default.Home, VPNScreen.Home.name),
    VPNNavigationItem(VPNScreen.Settings, Icons.Default.ManageAccounts, VPNScreen.Settings.name)
)

@Composable
fun VPNLayout(
    windowSize: WindowSizeClass,
    currentVPNScreen: VPNScreen,
    onNavItemPressed: (VPNScreen) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable() () -> Unit
) {

    val allContents = when (windowSize.widthSizeClass == WindowWidthSizeClass.Expanded) {
        true -> VPNNavigationDrawerWithContent(
            currentVPNScreen = currentVPNScreen,
            vpnNavigationItems = allVpnNavigationItems(),
            onNavItemPressed = onNavItemPressed,
            modifier = modifier,
        ) {
            VPNContentWithoutDrawer(
                windowSize,
                currentVPNScreen,
                onNavItemPressed,
                modifier,
                content
            )
        }

        else -> VPNContentWithoutDrawer(
            windowSize,
            currentVPNScreen,
            onNavItemPressed,
            modifier,
            content
        )
    }

    Box(modifier = modifier) {
        allContents
    }
}

@Composable
private fun VPNContentWithoutDrawer(
    windowSize: WindowSizeClass,
    currentVPNScreen: VPNScreen,
    onNavItemPressed: (VPNScreen) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable() () -> Unit
) {

    val showRail = windowSize.widthSizeClass == WindowWidthSizeClass.Medium ||
            (windowSize.heightSizeClass == WindowHeightSizeClass.Compact && windowSize.widthSizeClass ==
                    WindowWidthSizeClass.Compact)

    Row(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(visible = showRail) {
            VPNNavigationRail(currentVPNScreen, allVpnNavigationItems(), onNavItemPressed)
        }
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.weight(1f)) {
                content()
            }
            AnimatedVisibility(visible = showRail.not() && windowSize.widthSizeClass != WindowWidthSizeClass.Expanded) {
                VPNBottomNavigationBar(
                    currentVPNScreen,
                    allVpnNavigationItems(),
                    onNavItemPressed,
                    Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun VPNBottomNavigationBar(
    currentVPNScreen: VPNScreen,
    vpnNavigationItems: List<VPNNavigationItem>,
    onNavItemPressed: (VPNScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        for (item in vpnNavigationItems) {
            NavigationBarItem(
                selected = currentVPNScreen == item.screen,
                onClick = { onNavItemPressed(item.screen) },
                icon = {
                    Icon(imageVector = item.icon, contentDescription = item.text)
                })
        }
    }
}

@Composable
private fun VPNNavigationRail(
    currentVPNScreen: VPNScreen,
    vpnNavigationItems: List<VPNNavigationItem>,
    onNavItemPressed: (VPNScreen) -> Unit,
    modifier: Modifier = Modifier
) {

    NavigationRail(modifier = modifier) {
        Spacer(Modifier.weight(1f))
        Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
            for (item in vpnNavigationItems) {
                NavigationRailItem(
                    selected = currentVPNScreen == item.screen,
                    onClick = { onNavItemPressed(item.screen) },
                    icon = {
                        Icon(imageVector = item.icon, contentDescription = item.text)
                    })
            }
        }
        Spacer(Modifier.weight(1f))
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VPNNavigationDrawerWithContent(
    currentVPNScreen: VPNScreen,
    vpnNavigationItems: List<VPNNavigationItem>,
    onNavItemPressed: (VPNScreen) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {

    PermanentNavigationDrawer(
        modifier = modifier,
        drawerContent = {
            PermanentDrawerSheet(modifier = Modifier.width(160.dp)) {
                VPNNavigationDrawerContent(
                    currentVPNScreen = currentVPNScreen,
                    vpnNavigationItems = vpnNavigationItems,
                    onNavItemPressed = onNavItemPressed
                )
            }

        },
        content = content
    )

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VPNNavigationDrawerContent(
    currentVPNScreen: VPNScreen,
    vpnNavigationItems: List<VPNNavigationItem>,
    onNavItemPressed: (VPNScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(15.dp, 20.dp, 0.dp, 20.dp)
            .fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxHeight()) {
            NavigationDrawerItem(
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.upvpn),
                        contentDescription = "upvpn logo",
                        modifier = Modifier.size(26.dp)
                    )
                },
                label = { Text(text = "UpVPN", fontWeight = FontWeight.Bold) },
                selected = false,
                onClick = { })

        }
        Box(contentAlignment = Alignment.Center, modifier = modifier.fillMaxSize()) {
            Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
                for (item in vpnNavigationItems) {
                    NavigationDrawerItem(
                        icon = {
                            Icon(imageVector = item.icon, contentDescription = item.text)
                        },
                        label = { Text(text = item.text) },
                        selected = currentVPNScreen == item.screen,
                        onClick = { onNavItemPressed(item.screen) })
                }
            }
        }
    }
}

