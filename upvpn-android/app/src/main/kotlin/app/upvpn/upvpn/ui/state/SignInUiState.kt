package app.upvpn.upvpn.ui.state

data class SignInUiState(
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val signInState: SignInState = SignInState.CheckingLocal
)

sealed class SignInState {
    data object CheckingLocal : SignInState()

    data class SignedIn(val email: String) : SignInState()

    data object NotSignedIn : SignInState()

    data object Submitting : SignInState()

    data class Error(val message: String) : SignInState()
}
