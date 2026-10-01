package app.upvpn.upvpn.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
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
import androidx.compose.material.icons.filled.Notifications
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
                        isVpnSessionActivityInProgress,
                        signOutState,
                        onSignOutClick,
                        navigateTo,
                    )
                }
                item {
                    ShareCard()
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
    isVpnSessionActivityInProgress: Boolean,
    signOutState: SignOutState,
    onSignOutClick: () -> Unit,
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
            HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.45f))
            SignOutRow(
                isVpnSessionActivityInProgress,
                signOutState,
                onSignOutClick
            )
        }
    }
}

@Composable
fun ShareCard() {
    val context = LocalContext.current
    Text(
        text = "REFERRALS",
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier.padding(15.dp, 4.dp)
    )
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
                                "Use promo code UPVPN when you purchase on the web."
                        )
                    }
                    context.startActivity(Intent.createChooser(intent, null))
                }
                .padding(horizontal = 15.dp)
        ) {
            Text(text = "Refer a friend", modifier = Modifier.padding(vertical = 10.dp))
            Icon(
                Icons.Default.Share,
                contentDescription = "Share",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun SignOutRow(
    isVpnSessionActivityInProgress: Boolean,
    signOutState: SignOutState,
    onSignOutClick: () -> Unit
) {
    val isEnabled =
        signOutState is SignOutState.NotSignedOut && isVpnSessionActivityInProgress.not()
    var showConfirmDialog by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isEnabled) {
                showConfirmDialog = true
            }
            .padding(horizontal = 15.dp)
    ) {
        Text(
            text = when (signOutState) {
                SignOutState.SignedOut -> "Signed Out"
                SignOutState.SigningOut -> "Signing Out"
                SignOutState.NotSignedOut -> "Sign Out"
            },
            color = if (isEnabled) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            },
            modifier = Modifier.padding(vertical = 10.dp)
        )
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
    // SystemClock.elapsedRealtime() when permission was last requested
    var permissionRequestedAt by remember { mutableLongStateOf(0L) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        onPermissionResult()
        // once user has denied permission for good Android does not show its dialog
        // and denies right away, then only system settings can enable notifications
        val elapsedMs = SystemClock.elapsedRealtime() - permissionRequestedAt
        if (!granted && elapsedMs < DENIED_WITHOUT_PERMISSION_DIALOG_MS) {
            openAppNotificationSettings(context)
        }
    }

    Text(
        text = "NOTIFICATION",
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier.padding(15.dp, 4.dp)
    )

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionRequestedAt = SystemClock.elapsedRealtime()
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            // before Android 13 there is no permission to ask for,
                            // notifications can be turned back on only in system settings
                            openAppNotificationSettings(context)
                        }
                    }
                    .padding(horizontal = 15.dp)
            ) {
                Text(
                    text = "Enable Notification",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = "Enable Notification",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    Text(
        text = "See your VPN status and location in a notification.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(15.dp, 6.dp)
    )
}

// a denial quicker than this could not have come from user answering the permission dialog
private const val DENIED_WITHOUT_PERMISSION_DIALOG_MS = 500L

private fun openAppNotificationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
    }
    context.startActivity(intent)
}
