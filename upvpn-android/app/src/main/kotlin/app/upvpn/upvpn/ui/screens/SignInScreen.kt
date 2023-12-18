package app.upvpn.upvpn.ui.screens

import android.content.ActivityNotFoundException
import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.upvpn.upvpn.BuildConfig
import app.upvpn.upvpn.R
import app.upvpn.upvpn.ui.state.SignInState
import app.upvpn.upvpn.ui.state.SignInUiState
import app.upvpn.upvpn.ui.theme.UpVPNTheme
import com.google.accompanist.adaptive.HorizontalTwoPaneStrategy
import com.google.accompanist.adaptive.TwoPane
import com.google.accompanist.adaptive.VerticalTwoPaneStrategy


@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(showSystemUi = true)
@Composable
fun SignInScreenNotSignedIn() {
    UpVPNTheme {
        SignInScreen(
            WindowSizeClass.calculateFromSize(DpSize.Unspecified),
            signInUIState = SignInUiState(signInState = SignInState.NotSignedIn),
            onEmailValueChange = {},
            onPasswordValueChange = {},
            togglePasswordVisibility = {},
            onSignInClick = {},
            showSnackBar = {})
    }
}

@Composable
fun SignInScreen(
    windowSize: WindowSizeClass,
    signInUIState: SignInUiState,
    onEmailValueChange: (String) -> Unit,
    onPasswordValueChange: (String) -> Unit,
    togglePasswordVisibility: () -> Unit,
    onSignInClick: () -> Unit,
    showSnackBar: (String) -> Unit,
) {
    when (signInUIState.signInState) {
        is SignInState.SignedIn -> Brand()
        is SignInState.CheckingLocal -> Brand()
        else -> {
            if (windowSize.widthSizeClass == WindowWidthSizeClass.Compact && windowSize.heightSizeClass == WindowHeightSizeClass.Compact) {
                SignInCard(
                    signInUIState = signInUIState,
                    onEmailValueChange = onEmailValueChange,
                    onPasswordValueChange = onPasswordValueChange,
                    togglePasswordVisibility = togglePasswordVisibility,
                    onSignInClick = onSignInClick,
                    showSnackBar = showSnackBar
                )
            } else {
                TwoPane(
                    first = { SignInHeader() },
                    second = {
                        SignInCard(
                            signInUIState,
                            onEmailValueChange,
                            onPasswordValueChange,
                            togglePasswordVisibility,
                            onSignInClick,
                            showSnackBar,
                        )
                    },
                    strategy = when (windowSize.widthSizeClass) {
                        WindowWidthSizeClass.Compact -> VerticalTwoPaneStrategy(0.4f)
                        WindowWidthSizeClass.Medium -> VerticalTwoPaneStrategy(0.4f)
                        else -> HorizontalTwoPaneStrategy(0.4f)
                    },
                    displayFeatures = listOf(),
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                )
            }
        }
    }
}

@Composable
fun SignInHeader() {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painterResource(R.drawable.upvpn),
                contentDescription = "UpVPN Logo",
                tint = MaterialTheme.colorScheme.inverseSurface,
                modifier = Modifier
                    .fillMaxHeight(0.2f)
                    .aspectRatio(1f)
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(text = "UpVPN", fontSize = 40.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "A Modern Serverless VPN",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            )
        }
    }
}

private fun isValidEmail(email: String): Boolean {
    val pattern = Patterns.EMAIL_ADDRESS
    return pattern.matcher(email).matches()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInCard(
    signInUIState: SignInUiState,
    onEmailValueChange: (String) -> Unit,
    onPasswordValueChange: (String) -> Unit,
    togglePasswordVisibility: () -> Unit,
    onSignInClick: () -> Unit,
    showSnackBar: (String) -> Unit,
) {
    val uriHandler = LocalUriHandler.current

    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(20.dp)
        ) {
            item {
                OutlinedTextField(
                    enabled = signInUIState.signInState != SignInState.Submitting,
                    isError = false,
                    singleLine = true,
                    label = {
                        Text(text = "Email")
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        capitalization = KeyboardCapitalization.None,
                        autoCorrect = false,
                        imeAction = ImeAction.Next
                    ),
                    value = signInUIState.email,
                    onValueChange = onEmailValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
            item {
                Spacer(modifier = Modifier.height(5.dp))
            }
            item {
                OutlinedTextField(
                    enabled = signInUIState.signInState != SignInState.Submitting,
                    isError = false,
                    singleLine = true,
                    label = {
                        Text(text = "Password")
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        capitalization = KeyboardCapitalization.None,
                        autoCorrect = false,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        onSignInClick()
                    }),
                    value = signInUIState.password,
                    onValueChange = onPasswordValueChange,
                    visualTransformation = if (signInUIState.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val (icon, description) = when (signInUIState.passwordVisible) {
                            true -> Icons.Filled.Visibility to "Password visible"
                            false -> Icons.Filled.VisibilityOff to "Password invisible"
                        }
                        IconButton(onClick = togglePasswordVisibility) {
                            Icon(icon, contentDescription = description)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
            item {
                Box {
                    Spacer(modifier = Modifier.height(35.dp))
                    when (signInUIState.signInState) {
                        is SignInState.Error -> Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(5.dp)
                        ) {
                            Icon(
                                Icons.Default.WarningAmber, contentDescription = "Warning",
                                modifier = Modifier.size(10.dp)
                            )
                            Text(
                                fontSize = 10.sp,
                                text = signInUIState.signInState.message
                            )
                        }

                        else -> Text(text = "")
                    }
                }
            }
            item {
                Button(
                    onClick = onSignInClick,
                    enabled = (
                            (signInUIState.signInState != SignInState.Submitting) &&
                                    signInUIState.email.isNotEmpty() &&
                                    signInUIState.password.isNotEmpty() &&
                                    isValidEmail(signInUIState.email)
                            ),
                    shape = RoundedCornerShape(5.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    if (signInUIState.signInState == SignInState.Submitting) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(20.dp)
                                .align(Alignment.CenterVertically)
                        )
                    } else {
                        Text(text = "SIGN IN")
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(25.dp))
            }

            item {
                if (BuildConfig.IS_AMAZON) {
                    // Amazon: Remove Sign-Up or implement IAP
                    Text(
                        text = "upvpn.app",
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                } else {
                    Text(
                        text = "Need an account?",
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val uri = "${BuildConfig.UPVPN_BASE_URL}/sign-up"
                                try {
                                    uriHandler.openUri(uri)
                                } catch (e: ActivityNotFoundException) {
                                    showSnackBar("Please visit $uri")
                                }
                            }
                    )
                }
            }

        }
    }
}

@Composable
fun Brand(
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)

    ) {
        SignInHeader()
    }
}
