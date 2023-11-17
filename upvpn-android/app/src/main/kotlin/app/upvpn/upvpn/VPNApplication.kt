package app.upvpn.upvpn

import android.app.Application
import android.util.Log
import app.upvpn.upvpn.data.AppContainer
import app.upvpn.upvpn.data.DefaultAppContainer

class VPNApplication : Application() {
    private val tag = "VPNApplication"
    
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
        container.init()
    }

    override fun onTerminate() {
        Log.i(tag, "onTerminate")
        super.onTerminate()
    }
}
