package app.upvpn.upvpn.auth

import android.app.Activity
import android.content.Context

class GoogleSignInManagerImpl(@Suppress("UNUSED_PARAMETER") context: Context) : GoogleSignInManager {
    override val isAvailable: Boolean = false

    override suspend fun signInWithBottomSheet(activity: Activity) =
        GoogleSignInResult.NotAvailable

    override suspend fun signInWithButton(activity: Activity) =
        GoogleSignInResult.NotAvailable

    override suspend fun clearCredentialState() {}
}
