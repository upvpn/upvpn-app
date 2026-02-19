package app.upvpn.upvpn

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.upvpn.upvpn.service.client.VPNServiceConnectionManager
import app.upvpn.upvpn.ui.VPNApp
import app.upvpn.upvpn.ui.theme.UpVPNTheme
import com.google.accompanist.adaptive.calculateDisplayFeatures
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val tag = "MainActivity"
    private lateinit var serviceConnectionManager: VPNServiceConnectionManager

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            // android 15+ enforces edge-to-edge
            enableEdgeToEdge()
        }
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            // android 15+ enforces edge-to-edge
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(0, systemBars.top * 9 / 10, 0, 0)
                insets
            }
        }

        // initialize connection manager
        val app = application as VPNApplication
        serviceConnectionManager = app.container.serviceConnectionManager

        setContent {
            UpVPNTheme {
                Surface(modifier = Modifier.fillMaxSize()) {

                    // A surface container using the 'background' color from the theme
                    val snackbarHostState = remember { SnackbarHostState() }
                    val scope = rememberCoroutineScope()
                    val displayFeatures = calculateDisplayFeatures(activity = this)

                    val showSnackBar: (msg: String) -> Unit = { msg ->
                        scope.launch {
                            snackbarHostState.showSnackbar(msg)
                        }
                    }

                    val windowSize = calculateWindowSizeClass(this)

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        snackbarHost = {
                            SnackbarHost(
                                hostState = snackbarHostState,
                                modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 80.dp)
                            )
                        }) {
                        VPNApp(
                            windowSize = windowSize,
                            displayFeatures = displayFeatures,
                            showSnackBar = showSnackBar,
                            modifier = Modifier.padding(it)
                        )
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        serviceConnectionManager.bind()
    }

    override fun onStop() {
        super.onStop()
        serviceConnectionManager.unbind()
    }

    override fun onDestroy() {
        Log.d(tag, "onDestroy")
        super.onDestroy()
    }
}
