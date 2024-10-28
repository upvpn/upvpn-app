package app.upvpn.upvpn.ui.viewmodels

import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.upvpn.upvpn.data.VPNRepository
import app.upvpn.upvpn.model.OnlyEmail
import app.upvpn.upvpn.model.UserCredentials
import app.upvpn.upvpn.model.UserCredentialsWithCode
import app.upvpn.upvpn.service.client.VPNServiceConnectionManager
import app.upvpn.upvpn.ui.state.AuthAction
import app.upvpn.upvpn.ui.state.AuthUiState
import app.upvpn.upvpn.ui.state.SignInState
import app.upvpn.upvpn.ui.state.SignOutState
import app.upvpn.upvpn.ui.state.SignOutUiState
import com.github.michaelbull.result.fold
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val serviceConnectionManager: VPNServiceConnectionManager,
    private val vpnRepository: VPNRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val tag = "SignInViewModel"
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _signOutUiState = MutableStateFlow(SignOutUiState())
    val signOutUiState: StateFlow<SignOutUiState> = _signOutUiState.asStateFlow()

    init {
        viewModelScope.launch(dispatcher) {
            val userEmail = vpnRepository.isAuthenticated()
            if (userEmail != null) {
                _uiState.update { value -> value.copy(signInState = SignInState.SignedIn(userEmail)) }
            } else {
                _uiState.update { value -> value.copy(signInState = SignInState.NotSignedIn) }
            }
        }
    }

    fun setAuthAction(authAction: AuthAction) {
        _uiState.update { value -> value.copy(authAction = authAction) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { value -> value.copy(passwordVisible = !value.passwordVisible) }
    }

    fun onEmailChange(text: String) {
        _uiState.update { value ->
            value.copy(
                email = text.lowercase(),
                signInState = SignInState.NotSignedIn
            )
        }
    }

    fun onPasswordChange(text: String) {
        _uiState.update { value ->
            value.copy(
                password = text,
                signInState = SignInState.NotSignedIn
            )
        }
    }

    fun onSignUpCodeChange(text: String) {
        _uiState.update { value ->
            value.copy(
                signUpCode = text,
            )
        }
    }

    fun clearSignInError() {
        _uiState.update { value -> value.copy(signInError = null) }
    }

    fun clearSignUpError() {
        _uiState.update { value -> value.copy(signUpError = null) }
    }

    fun clearSignOutError() {
        _signOutUiState.update { value -> value.copy(signOutError = null) }
    }

    private fun onSignInClick() {
        _uiState.update { value ->
            value.copy(
                signInState = SignInState.Submitting,
                passwordVisible = false
            )
        }
        viewModelScope.launch(dispatcher) {
            val email = uiState.value.email
            val result = vpnRepository.addDevice(
                UserCredentials(uiState.value.email, uiState.value.password)
            )

            result.fold(
                success = {
                    _uiState.update { value ->
                        value.copy(
                            signInState = SignInState.SignedIn(email),
                            password = ""
                        )
                    }
                    _signOutUiState.update { value -> value.copy(signOutState = SignOutState.NotSignedOut) }
                },
                failure = { error ->
                    _uiState.update {
                        it.copy(
                            signInState = SignInState.NotSignedIn,
                            signInError = error
                        )
                    }
                }
            )
        }
    }

    private fun onSignUpClick() {
        _uiState.update { value ->
            value.copy(isSigningUp = true)
        }

        viewModelScope.launch(dispatcher) {
            val result = vpnRepository.signUp(
                UserCredentialsWithCode(
                    email = uiState.value.email,
                    password = uiState.value.password,
                    code = uiState.value.signUpCode
                )
            )

            result.fold(
                success = {
                    onSignInClick()
                    _uiState.update { value ->
                        value.copy(
                            isSigningUp = false,
                            signUpCode = "",
                            authAction = AuthAction.SignIn
                        )
                    }
                },
                failure = {
                    _uiState.update { value -> value.copy(isSigningUp = false, signUpError = it) }
                }
            )
        }
    }

    fun onSubmit() {
        when (_uiState.value.authAction) {
            is AuthAction.SignIn -> {
                onSignInClick()
            }

            is AuthAction.SignUp -> {
                onSignUpClick()
            }
        }
    }

    fun onRequestSignUpCode() {
        viewModelScope.launch(dispatcher) {
            val result = vpnRepository.requestCode(OnlyEmail(email = uiState.value.email))

            result.fold(
                success = {
                    _uiState.update { value -> value.copy(signUpCodeRequestedAt = SystemClock.elapsedRealtime()) }
                },
                failure = {
                    _uiState.update { value -> value.copy(signUpError = it) }
                }
            )
        }
    }

    fun onSignOutClick() {
        _signOutUiState.update { value -> value.copy(signOutState = SignOutState.SigningOut) }

        viewModelScope.launch {
            // TODO: this could be more robust by waiting for disconnect and then signing out
            serviceConnectionManager.vpnManager()?.disconnect()
            delay(300)
            val result = vpnRepository.signOut()

            result.fold(
                success = {
                    _uiState.update { value -> value.copy(signInState = SignInState.NotSignedIn) }
                    _signOutUiState.update { value -> value.copy(signOutState = SignOutState.SignedOut) }
                },
                failure = {
                    _signOutUiState.update { value ->
                        value.copy(
                            signOutState = SignOutState.NotSignedOut,
                            signOutError = it
                        )
                    }
                    Log.d(tag, "Failed to log out $it")
                }
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.i("SignInViewModel", "onCleared YO")
    }
}
