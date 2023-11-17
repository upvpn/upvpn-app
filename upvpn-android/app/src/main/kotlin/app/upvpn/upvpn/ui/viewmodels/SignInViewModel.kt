package app.upvpn.upvpn.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.upvpn.upvpn.data.VPNRepository
import app.upvpn.upvpn.model.UserCredentials
import app.upvpn.upvpn.service.client.VPNServiceConnectionManager
import app.upvpn.upvpn.ui.state.SignInState
import app.upvpn.upvpn.ui.state.SignInUiState
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

class SignInViewModel(
    private val serviceConnectionManager: VPNServiceConnectionManager,
    private val vpnRepository: VPNRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val tag = "SignInViewModel"
    private val _uiState = MutableStateFlow(SignInUiState())
    val uiState: StateFlow<SignInUiState> = _uiState.asStateFlow()

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

    fun onSignInClick() {
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
                    _uiState.update { value -> value.copy(signInState = SignInState.SignedIn(email)) }
                    _signOutUiState.update { value -> value.copy(signOutState = SignOutState.NotSignedOut) }
                },
                failure = { error ->
                    _uiState.update { it.copy(signInState = SignInState.Error(error)) }
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
                            signOutState = SignOutState.SignOutError(
                                it
                            )
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
