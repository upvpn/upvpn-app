package app.upvpn.upvpn.ui.state

data class SignOutUiState(val signOutState: SignOutState = SignOutState.NotSignedOut)

sealed class SignOutState {
    data object NotSignedOut : SignOutState()
    data object SigningOut : SignOutState()

    data class SignOutError(val error: String) : SignOutState()

    data object SignedOut : SignOutState()
}
