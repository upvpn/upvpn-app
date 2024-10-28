package app.upvpn.upvpn.ui.components

import android.os.SystemClock
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import app.upvpn.upvpn.ui.state.AuthUiState
import kotlinx.coroutines.delay

@Composable
fun EmailCodeField(
    authUIState: AuthUiState,
    onSignUpCodeChange: (String) -> Unit,
    onRequestSignUpCode: () -> Unit,
) {
    var text by remember { mutableStateOf("Email Code") }
    var elapsedSeconds by remember { mutableLongStateOf(Long.MAX_VALUE) }
    var enabled by remember { mutableStateOf(true) }

    LaunchedEffect(key1 = authUIState.signUpCodeRequestedAt) {
        while (true) {
            authUIState.signUpCodeRequestedAt?.let { it ->
                elapsedSeconds = (SystemClock.elapsedRealtime() - it) / 1000

                if (elapsedSeconds < 3) {
                    text = "Email: code sent"
                    enabled = false
                } else if (elapsedSeconds < 60) {
                    text = "Resend in ${60 - elapsedSeconds}"
                    enabled = false
                } else {
                    text = "Email Code"
                    enabled = true
                }
            }
            delay(1000)
        }
    }

    OutlinedTextField(
        enabled = true,
        isError = false,
        singleLine = true,
        label = {
            Text(text = "Enter 6-digit code")
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            autoCorrectEnabled = false,
            imeAction = ImeAction.Done,
        ),
        value = authUIState.signUpCode,
        onValueChange = onSignUpCodeChange,
        trailingIcon = {
            TextButton(onClick = onRequestSignUpCode, enabled = enabled) {
                Text(text)
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}
