package app.upvpn.upvpn.ui.state

data class SignOutUiState(
    val signOutState: SignOutState = SignOutState.NotSignedOut,
    val signOutError: String? = null
)

sealed class SignOutState {
    data object NotSignedOut : SignOutState()
    data object SigningOut : SignOutState()
    data object SignedOut : SignOutState()
}
