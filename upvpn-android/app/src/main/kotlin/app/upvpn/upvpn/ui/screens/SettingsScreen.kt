package app.upvpn.upvpn.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.upvpn.upvpn.BuildConfig
import app.upvpn.upvpn.ui.VPNScreen
import app.upvpn.upvpn.ui.state.SignOutState

@Preview(showSystemUi = true)
@Composable
fun PreviewSettingsScreen() {
    SettingsScreen(true, "support@upvpn.app", SignOutState.NotSignedOut, {}, {})
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SettingsScreen(
    isVpnSessionActivityInProgress: Boolean,
    signedInEmail: String,
    signOutState: SignOutState,
    onSignOutClick: () -> Unit,
    navigateTo: (VPNScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var notificationsEnabled by remember {
        mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled())
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                notificationsEnabled =
                    NotificationManagerCompat.from(context).areNotificationsEnabled()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(topBar = {
        TopAppBar(title = {
            AccountAndSettingsHeader()
        })
    }) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 15.dp)
                .padding(bottom = 20.dp)
        ) {
            // LazyColumn instead of Column so that its scrollable
            // on rotated small screen
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.weight(1f)
            ) {
                item {
                    AccountCard(
                        signedInEmail,
                        navigateTo,
                    )
                }
                item {
                    ShareCard()
                }

                item {
                    SignOut(
                        isVpnSessionActivityInProgress,
                        signOutState,
                        onSignOutClick
                    )
                }
                item {
                    AboutCard()
                }

                if (!notificationsEnabled) {
                    item {
                        NotificationsCard(
                            onPermissionResult = {
                                notificationsEnabled = NotificationManagerCompat
                                    .from(context)
                                    .areNotificationsEnabled()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AccountAndSettingsHeader() {
    Text(
        "Account",
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
fun AboutCard() {
    Text(
        text = "VERSION",
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier.padding(15.dp, 4.dp)
    )

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(15.dp, 10.dp)
                .fillMaxWidth()
        ) {

            AppVersion(BuildConfig.VERSION_CODE, BuildConfig.VERSION_NAME)
        }
    }
}

@Composable
fun AccountCard(
    signedInEmail: String,
    navigateTo: (VPNScreen) -> Unit,
) {
    Text(
        text = "PROFILE",
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier.padding(15.dp, 4.dp)
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()

    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = signedInEmail,
                modifier = Modifier.padding(vertical = 10.dp, horizontal = 15.dp)
            )
            HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.45f))
            Row(horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navigateTo(VPNScreen.Plan)
                    }
                    .padding(horizontal = 15.dp)
            ) {
                Text(text = "Plan", modifier = Modifier.padding(vertical = 10.dp))
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = "Your plan",
                    modifier = Modifier.size(15.dp)
                )
            }
            HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.45f))
            Row(horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navigateTo(VPNScreen.Help)
                    }
                    .padding(horizontal = 15.dp)
            ) {
                Text(text = "Help", modifier = Modifier.padding(vertical = 10.dp))
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = "Go to Help",
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

@Composable
fun ShareCard() {
    val context = LocalContext.current
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "Check out this cool VPN app: https://UpVPN.app\n" +
                                "Use promo code UPVPN for purchase on the web."
                        )
                    }
                    context.startActivity(Intent.createChooser(intent, null))
                }
                .padding(horizontal = 15.dp)
        ) {
            Text(text = "Share and Refer", modifier = Modifier.padding(vertical = 10.dp))
            Icon(
                Icons.Default.Share,
                contentDescription = "Share",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun SignOut(
    isVpnSessionActivityInProgress: Boolean,
    signOutState: SignOutState,
    onSignOutClick: () -> Unit
) {
    val isEnabled = signOutState is SignOutState.NotSignedOut
    var showConfirmDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()

    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            TextButton(
                enabled = isEnabled && isVpnSessionActivityInProgress.not(),
                onClick = {
                    showConfirmDialog = true
                }) {
                when (signOutState) {
                    SignOutState.SignedOut -> Text(text = "Signed Out")
                    SignOutState.SigningOut -> {
                        Text(
                            text = "Signing Out",
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }

                    else -> Text(text = "Sign Out")
                }
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    onSignOutClick()
                }) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AppVersion(
    versionCode: Int,
    versionName: String,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        Text(
            text = "$versionName / $versionCode",
        )
    }
}

@Composable
fun NotificationsCard(onPermissionResult: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
    var openSettingsInstead by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        onPermissionResult()
        if (!granted && activity != null) {
            openSettingsInstead = !ActivityCompat.shouldShowRequestPermissionRationale(
                activity, Manifest.permission.POST_NOTIFICATIONS
            )
        }
    }

    Text(
        text = "NOTIFICATION",
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier.padding(15.dp, 4.dp)
    )

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "See your VPN status",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp)
            )
            HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.45f))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val canUseSystemDialog =
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                    !openSettingsInstead
                        if (canUseSystemDialog) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            openAppNotificationSettings(context)
                        }
                    }
                    .padding(horizontal = 15.dp)
            ) {
                Text(
                    text = "Enable Notification",
                    modifier = Modifier.padding(vertical = 10.dp)
                )
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = "Enable Notifications",
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

private fun openAppNotificationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
    }
    context.startActivity(intent)
}
