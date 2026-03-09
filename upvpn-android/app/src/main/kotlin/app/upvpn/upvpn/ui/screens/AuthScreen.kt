package app.upvpn.upvpn.ui.screens

import android.app.Activity
import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.upvpn.upvpn.R
import app.upvpn.upvpn.ui.components.AuthFooter
import app.upvpn.upvpn.ui.components.EmailCodeField
import app.upvpn.upvpn.ui.components.Logo
import app.upvpn.upvpn.ui.state.AuthAction
import app.upvpn.upvpn.ui.state.AuthUiState
import app.upvpn.upvpn.ui.state.SignInState
import app.upvpn.upvpn.ui.theme.UpVPNTheme


@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(showSystemUi = true)
@Composable
fun SignInScreenNotSignedIn() {
    UpVPNTheme {
        SignInScreen(
            WindowSizeClass.calculateFromSize(DpSize.Unspecified),
            authUIState = AuthUiState(signInState = SignInState.NotSignedIn),
            onEmailValueChange = {},
            onPasswordValueChange = {},
            togglePasswordVisibility = {},
            onSubmit = {},
            showSnackBar = {},
            setAuthAction = {},
            onSignUpCodeChange = {},
            onRequestSignUpCode = {},
            onGoogleSignInBottomSheet = {},
            onGoogleSignInButton = {},
        )
    }
}

@Composable
fun SignInScreen(
    windowSize: WindowSizeClass,
    authUIState: AuthUiState,
    onEmailValueChange: (String) -> Unit,
    onPasswordValueChange: (String) -> Unit,
    togglePasswordVisibility: () -> Unit,
    onSubmit: () -> Unit,
    showSnackBar: (String) -> Unit,
    setAuthAction: (AuthAction) -> Unit,
    onSignUpCodeChange: (String) -> Unit,
    onRequestSignUpCode: () -> Unit,
    onGoogleSignInBottomSheet: (Activity) -> Unit,
    onGoogleSignInButton: (Activity) -> Unit,
) {
    when (authUIState.signInState) {
        is SignInState.SignedIn -> Brand()
        is SignInState.CheckingLocal -> Brand()
        else -> {
            // Auto-trigger bottom sheet for returning users
            if (authUIState.isGoogleSignInAvailable) {
                val context = LocalContext.current
                LaunchedEffect(Unit) {
                    (context as? Activity)?.let { onGoogleSignInBottomSheet(it) }
                }
            }

            if (windowSize.widthSizeClass == WindowWidthSizeClass.Compact && windowSize.heightSizeClass == WindowHeightSizeClass.Compact) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 20.dp)
                ) {
                    SignInCard(
                        authUIState = authUIState,
                        onEmailValueChange = onEmailValueChange,
                        onPasswordValueChange = onPasswordValueChange,
                        togglePasswordVisibility = togglePasswordVisibility,
                        onSubmit = onSubmit,
                        showSnackBar = showSnackBar,
                        setAuthAction = setAuthAction,
                        onSignUpCodeChange = onSignUpCodeChange,
                        onRequestSignUpCode = onRequestSignUpCode,
                        onGoogleSignInButton = onGoogleSignInButton,
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 20.dp)
                ) {
                    SignInHeader()
                    SignInCard(
                        authUIState,
                        onEmailValueChange,
                        onPasswordValueChange,
                        togglePasswordVisibility,
                        onSubmit,
                        showSnackBar,
                        setAuthAction,
                        onSignUpCodeChange,
                        onRequestSignUpCode,
                        onGoogleSignInButton,
                    )
                }
            }
        }
    }
}

@Composable
fun SignInHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Logo()
        Spacer(modifier = Modifier.height(5.dp))
        Text(text = "UpVPN", fontSize = 40.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "A Modern Serverless VPN",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Light
        )
    }
}

private fun isValidEmail(email: String): Boolean {
    val pattern = Patterns.EMAIL_ADDRESS
    return pattern.matcher(email).matches()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInCard(
    authUIState: AuthUiState,
    onEmailValueChange: (String) -> Unit,
    onPasswordValueChange: (String) -> Unit,
    togglePasswordVisibility: () -> Unit,
    onSubmit: () -> Unit,
    showSnackBar: (String) -> Unit,
    setAuthAction: (AuthAction) -> Unit,
    onSignUpCodeChange: (String) -> Unit,
    onRequestSignUpCode: () -> Unit,
    onGoogleSignInButton: (Activity) -> Unit,
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(20.dp)
    ) {

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                onClick = { setAuthAction(AuthAction.SignUp) },
                selected = authUIState.authAction == AuthAction.SignUp,
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            ) {
                Text("Sign Up")
            }

            SegmentedButton(
                onClick = { setAuthAction(AuthAction.SignIn) },
                selected = authUIState.authAction == AuthAction.SignIn,
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            ) {
                Text("Sign In")
            }
        }

        Spacer(modifier = Modifier.height(5.dp))

        OutlinedTextField(
            enabled = authUIState.signInState != SignInState.Submitting,
            isError = false,
            singleLine = true,
            label = {
                Text(text = "Email")
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false,
                imeAction = ImeAction.Next
            ),
            value = authUIState.email,
            onValueChange = onEmailValueChange,
            modifier = Modifier
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(5.dp))

        OutlinedTextField(
            enabled = authUIState.signInState != SignInState.Submitting,
            isError = false,
            singleLine = true,
            label = {
                Text(text = "Password")
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false,
                imeAction = if (authUIState.authAction == AuthAction.SignUp) ImeAction.Next else ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = {
                onSubmit()
            }),
            value = authUIState.password,
            onValueChange = onPasswordValueChange,
            visualTransformation = if (authUIState.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val (icon, description) = when (authUIState.passwordVisible) {
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


        if (authUIState.authAction == AuthAction.SignUp) {

            Spacer(modifier = Modifier.height(5.dp))
            EmailCodeField(authUIState, onSignUpCodeChange, onRequestSignUpCode)

        }


        Spacer(modifier = Modifier.height(15.dp))

        Button(
            onClick = onSubmit,
            enabled = (
                    (authUIState.signInState != SignInState.Submitting) &&
                            authUIState.email.isNotEmpty() &&
                            authUIState.password.isNotEmpty() &&
                            isValidEmail(authUIState.email) &&
                            !authUIState.isSigningUp &&
                            (authUIState.authAction == AuthAction.SignIn || (
                                    authUIState.authAction == AuthAction.SignUp && authUIState.signUpCode.isNotEmpty()
                                    ))
                    ),
            shape = RoundedCornerShape(5.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (authUIState.signInState == SignInState.Submitting || authUIState.isSigningUp) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.CenterVertically)
                )
            } else {
                Text(text = (if (authUIState.authAction == AuthAction.SignUp) "SIGN UP" else "SIGN IN"))
            }
        }

        if (authUIState.isGoogleSignInAvailable) {
            Spacer(modifier = Modifier.height(15.dp))

            // "or" divider
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                HorizontalDivider()
                Text(
                    text = "or",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(15.dp))

            // Google sign-in button
            val context = LocalContext.current
            if (authUIState.isGoogleSignInSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.btn_google_signin),
                    contentDescription = "Continue with Google",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .height(40.dp)
                        .clickable {
                            (context as? Activity)?.let { onGoogleSignInButton(it) }
                        }
                )
            }
        }

        Spacer(modifier = Modifier.height(25.dp))

        AuthFooter()

    }
}

@Composable
fun Brand(
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        SignInHeader()
    }
}
