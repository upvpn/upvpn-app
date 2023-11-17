package app.upvpn.upvpn.service.ipc.extensions

import android.os.Bundle
import android.os.Message
import app.upvpn.upvpn.service.ipc.OrchestratorMessage

const val ORCHESTRATOR_KEY = "orca"
const val ORCHESTRATOR_WHAT = 3
fun OrchestratorMessage.toMessage(): Message {
    return Message.obtain().also {
        var bundle = Bundle()
        bundle.putParcelable(ORCHESTRATOR_KEY, this)
        it.what = ORCHESTRATOR_WHAT
        it.data = bundle
    }
}
