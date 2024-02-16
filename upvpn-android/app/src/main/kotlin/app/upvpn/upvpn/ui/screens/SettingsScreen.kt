package app.upvpn.upvpn.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.upvpn.upvpn.BuildConfig
import app.upvpn.upvpn.ui.state.SignOutState

@Preview(showSystemUi = true)
@Composable
fun PreviewSettingsScreen() {
    SettingsScreen(true, "support@upvpn.app", SignOutState.NotSignedOut, {})
}

@Composable
fun SettingsScreen(
    isVpnSessionActivityInProgress: Boolean,
    signedInEmail: String,
    signOutState: SignOutState,
    onSignOutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(15.dp)
    ) {
        AccountAndSettingsHeader()
        // LazyColumn instead of Column so that its scrollable
        // on rotated small screen
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.weight(1f)
        ) {
            item {
                AccountCard(
                    isVpnSessionActivityInProgress,
                    signedInEmail = signedInEmail, signOutState, onSignOutClick
                )
            }
            item {
                AboutCard()
            }
        }
    }
}

@Composable
fun AccountAndSettingsHeader() {
    Text(
        "Account and Settings",
        fontSize = 20.sp,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )
}

@Composable
fun AboutCard() {
    val context = LocalContext.current
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .padding(15.dp, 10.dp)
                .fillMaxWidth()
        ) {
            Text(text = "ABOUT", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            AppVersion(BuildConfig.VERSION_CODE, BuildConfig.VERSION_NAME)
            Divider()
            Row(horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(BuildConfig.UPVPN_BASE_URL + "/oss/android/${BuildConfig.VERSION_NAME}")
                        )
                        context.startActivity(intent)
                    }) {
                Text(text = "Open Source Licenses")
                Icon(imageVector = Icons.Default.OpenInNew, contentDescription = "Open externally")
            }
        }
    }
}

@Composable
fun AccountCard(
    isVpnSessionActivityInProgress: Boolean,
    signedInEmail: String, signOutState: SignOutState, onSignOutClick: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()

    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .padding(15.dp, 10.dp)
                .fillMaxWidth()
        ) {
            Text(text = "ACCOUNT", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            Text(text = signedInEmail)
            Divider()
            Row(horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(BuildConfig.UPVPN_BASE_URL + "/dashboard")
                        )
                        context.startActivity(intent)
                    }) {
                Text(text = "Dashboard")
                Icon(imageVector = Icons.Default.OpenInNew, contentDescription = "Open externally")
            }
            Divider()
            Column(modifier = Modifier.padding(top = 20.dp)) {
                SignOut(
                    isVpnSessionActivityInProgress,
                    signOutState = signOutState, onSignOutClick
                )
            }
        }
    }
}

@Composable
fun SignOut(
    isVpnSessionActivityInProgress: Boolean,
    signOutState: SignOutState, onSignOutClick: () -> Unit
) {

    val isEnabled =
        signOutState is SignOutState.NotSignedOut || signOutState is SignOutState.SignOutError
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Button(
            enabled = isEnabled && isVpnSessionActivityInProgress.not(),
            onClick = {
                onSignOutClick()
            },
            shape = RoundedCornerShape(5.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            when (signOutState) {
                SignOutState.SignedOut -> Text(text = "Signed Out")
                SignOutState.SigningOut -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Signing Out", modifier = Modifier.padding(10.dp, 0.dp, 0.dp, 0.dp)
                    )
                }

                else -> Text(text = "Sign Out")
            }

        }
        if (signOutState is SignOutState.SignOutError) {
            Text(text = signOutState.error, fontSize = 10.sp)
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
            text = "App version $versionCode / $versionName",
        )
    }
}
