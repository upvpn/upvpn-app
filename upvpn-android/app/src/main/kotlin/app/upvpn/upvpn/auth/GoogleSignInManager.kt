package app.upvpn.upvpn.auth

import android.app.Activity

sealed class GoogleSignInResult {
    data class Success(val idToken: String, val email: String) : GoogleSignInResult()
    data class Error(val message: String) : GoogleSignInResult()
    data object Cancelled : GoogleSignInResult()
    data object NotAvailable : GoogleSignInResult()
}

interface GoogleSignInManager {
    val isAvailable: Boolean
    suspend fun signInWithBottomSheet(activity: Activity): GoogleSignInResult
    suspend fun signInWithButton(activity: Activity): GoogleSignInResult
    suspend fun clearCredentialState()
}
