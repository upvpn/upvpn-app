package app.upvpn.upvpn.network

import kotlinx.serialization.Serializable

@Serializable
data class ApiError(val errorType: String, val message: String)
