package app.upvpn.upvpn.data

import android.content.Context
import app.upvpn.upvpn.BuildConfig
import app.upvpn.upvpn.data.db.VPNDatabase
import app.upvpn.upvpn.network.AuthInterceptor
import app.upvpn.upvpn.network.VPNApiService
import app.upvpn.upvpn.notification.VPNNotificationManager
import app.upvpn.upvpn.service.client.VPNServiceConnectionManager
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.skydoves.sandwich.adapters.ApiResponseCallAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit


interface AppContainer {
    val appScope: CoroutineScope
    val vpnRepository: VPNRepository
    val vpnSessionRepository: VPNSessionRepository
    val serviceConnectionManager: VPNServiceConnectionManager
    val vpnNotificationManager: VPNNotificationManager
    fun init()
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    private val baseUrl = "${BuildConfig.UPVPN_BASE_URL}/api/v1/"

    override val appScope by lazy { CoroutineScope(SupervisorJob()) }

    override fun init() {
        synchronized(this) {
            runBlocking {
                // init device
                vpnRepository.initDevice()
                // mark old vpn sessions to be reclaimed
                vpnDatabase.vpnSessionDao().markAllForDeletion()
                // create VPN session status notification channel
                vpnNotificationManager.createChannels()
            }
        }
    }

    private val vpnDatabase: VPNDatabase by lazy {
        VPNDatabase.getDatabase(context)
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor {
            vpnDatabase.userDao().getUserSync()?.token
        })
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .client(okHttpClient)
        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
        .addCallAdapterFactory(ApiResponseCallAdapterFactory.create(appScope))
        .baseUrl(baseUrl)
        .build()

    private val retrofitService: VPNApiService by lazy {
        retrofit.create(VPNApiService::class.java)
    }

    override val vpnRepository: VPNRepository by lazy {
        DefaultVPNRepository(retrofitService, vpnDatabase)
    }

    override val vpnSessionRepository: VPNSessionRepository by lazy {
        DefaultVPNSessionRepository(retrofitService, vpnDatabase)
    }

    override val serviceConnectionManager: VPNServiceConnectionManager by lazy {
        VPNServiceConnectionManager(context)
    }

    override val vpnNotificationManager: VPNNotificationManager by lazy {
        VPNNotificationManager(context)
    }
}
