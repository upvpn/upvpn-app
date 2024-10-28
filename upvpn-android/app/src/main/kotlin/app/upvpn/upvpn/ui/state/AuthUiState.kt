package app.upvpn.upvpn.ui.state

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val signUpCode: String = "",
    val signUpCodeRequestedAt: Long? = null,
    val signInState: SignInState = SignInState.CheckingLocal,
    val authAction: AuthAction = AuthAction.SignIn,
    val isSigningUp: Boolean = false,

    val signInError: String? = null,
    val signUpError: String? = null,
)

sealed class SignInState {
    data object CheckingLocal : SignInState()

    data class SignedIn(val email: String) : SignInState()

    data object NotSignedIn : SignInState()

    data object Submitting : SignInState()
}

sealed class AuthAction {
    data object SignIn : AuthAction()
    data object SignUp : AuthAction()
}
