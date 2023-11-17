package app.upvpn.upvpn.network

import android.util.Log
import app.upvpn.upvpn.BuildConfig
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.skydoves.sandwich.ApiResponse
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.IOException
import java.net.ConnectException
import javax.net.ssl.SSLException

private fun clientException(msg: String): ApiError = ApiError(
    errorType = "client_exception",
    message = msg
)

fun <T> ApiResponse<T>.toResult(): Result<T, ApiError> {
    val result = when (this) {
        is ApiResponse.Success -> {
            Ok(data)
        }

        is ApiResponse.Failure.Error -> {
            val apiError = try {
                val apiError: ApiError? = Json.decodeFromString(this.errorBody?.string()!!)
                apiError!!
            } catch (e: SerializationException) {
                e.printStackTrace()
                clientException("Cannot process response. Please try again.")
            } catch (e: NullPointerException) {
                e.printStackTrace()
                clientException("No response. Please try again.")
            }

            Err(apiError)
        }

        is ApiResponse.Failure.Exception -> {
            this.exception.printStackTrace()

            val apiError = when (this.exception) {
                is ConnectException -> clientException("Connection Error. Please try again.")
                is SSLException -> clientException("SSL Connection Error. Please try again.")
                is IOException -> clientException("Network issue. Please try again.")
                else -> clientException(exception.message ?: "Failed. Please try again.")
            }

            Err(apiError)
        }
    }

    if (BuildConfig.DEBUG) {
        Log.d("ApiResponse.toResult", "ApiResponse: $this, \nResult: $result")
    }

    return result
}
