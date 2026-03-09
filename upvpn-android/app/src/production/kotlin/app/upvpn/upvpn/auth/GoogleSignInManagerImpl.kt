package app.upvpn.upvpn.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import app.upvpn.upvpn.BuildConfig
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

class GoogleSignInManagerImpl(context: Context) : GoogleSignInManager {

    private val tag = "GoogleSignInManager"
    private val credentialManager = CredentialManager.create(context)
    private val clientId = BuildConfig.GOOGLE_CLIENT_ID

    override val isAvailable: Boolean = clientId.isNotEmpty()

    override suspend fun signInWithBottomSheet(activity: Activity): GoogleSignInResult {
        if (!isAvailable) return GoogleSignInResult.NotAvailable

        // First try with authorized accounts only (returning users)
        val authorizedResult = tryGetGoogleId(activity, filterByAuthorizedAccounts = true)
        if (authorizedResult is GoogleSignInResult.Success) return authorizedResult

        // Only fall back to all accounts if no credentials were found,
        // not if the user explicitly dismissed the sheet
        if (authorizedResult is GoogleSignInResult.NotAvailable) {
            return tryGetGoogleId(activity, filterByAuthorizedAccounts = false)
        }

        return authorizedResult
    }

    override suspend fun signInWithButton(activity: Activity): GoogleSignInResult {
        if (!isAvailable) return GoogleSignInResult.NotAvailable

        return try {
            val signInOption = GetSignInWithGoogleOption.Builder(clientId).build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInOption)
                .build()

            val response = credentialManager.getCredential(activity, request)
            parseResponse(response)
        } catch (e: GetCredentialCancellationException) {
            Log.d(tag, "User cancelled sign-in with button")
            GoogleSignInResult.Cancelled
        } catch (e: Exception) {
            Log.e(tag, "Sign-in with button failed", e)
            GoogleSignInResult.Error(e.message ?: "Sign-in failed. Please try again.")
        }
    }

    override suspend fun clearCredentialState() {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.w(tag, "Failed to clear credential state", e)
        }
    }

    private suspend fun tryGetGoogleId(
        activity: Activity,
        filterByAuthorizedAccounts: Boolean
    ): GoogleSignInResult {
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(clientId)
                .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response = credentialManager.getCredential(activity, request)
            parseResponse(response)
        } catch (e: NoCredentialException) {
            Log.d(tag, "No credentials available (filterAuthorized=$filterByAuthorizedAccounts)")
            GoogleSignInResult.NotAvailable
        } catch (e: GetCredentialCancellationException) {
            Log.d(tag, "User cancelled (filterAuthorized=$filterByAuthorizedAccounts)")
            GoogleSignInResult.Cancelled
        } catch (e: Exception) {
            Log.e(tag, "Google ID sign-in failed (filterAuthorized=$filterByAuthorizedAccounts)", e)
            GoogleSignInResult.Error(e.message ?: "Sign-in failed. Please try again.")
        }
    }

    private fun parseResponse(response: GetCredentialResponse): GoogleSignInResult {
        val credential = response.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            return GoogleSignInResult.Success(
                idToken = googleIdTokenCredential.idToken,
                email = googleIdTokenCredential.id
            )
        }
        return GoogleSignInResult.Error("Unexpected credential type")
    }
}
