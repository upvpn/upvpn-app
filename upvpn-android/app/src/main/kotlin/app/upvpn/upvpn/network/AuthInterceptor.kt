package app.upvpn.upvpn.network

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val token: () -> String?) : Interceptor {
    private val tag = "AuthInterceptor"

    override fun intercept(chain: Interceptor.Chain): Response {

        val newRequest = token()?.let {
            chain.request()
                .newBuilder()
                .addHeader("Authorization", "Bearer $it")
                .build()
        } ?: chain.request()

        return chain.proceed(newRequest)
    }

}
