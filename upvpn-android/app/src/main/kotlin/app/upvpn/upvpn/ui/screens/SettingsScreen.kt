package app.upvpn.upvpn.ui.screens

import android.annotation.SuppressLint
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
                    AboutCard()
                }

                item {
                    SignOut(
                        isVpnSessionActivityInProgress,
                        signOutState,
                        onSignOutClick
                    )
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
        text = "ACCOUNT",
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
fun SignOut(
    isVpnSessionActivityInProgress: Boolean,
    signOutState: SignOutState,
    onSignOutClick: () -> Unit
) {
    val isEnabled = signOutState is SignOutState.NotSignedOut
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
                    onSignOutClick()
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

}

@Composable
fun AppVersion(
    versionCode: Int,
    versionName: String,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        Text(
            text = "$versionCode / $versionName",
        )
    }
}
