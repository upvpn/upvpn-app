package app.upvpn.upvpn.service.ipc.extensions

import android.os.Build
import android.os.Message
import app.upvpn.upvpn.service.ipc.Event
import app.upvpn.upvpn.service.ipc.OrchestratorMessage
import app.upvpn.upvpn.service.ipc.Request

private fun <T> Message.convertor(key: String, classLoader: ClassLoader, clazz: Class<T>): T? {
    this.data.classLoader = classLoader
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.data.getParcelable(key, clazz)
    } else {
        this.data.getParcelable(key)
    }
}

fun Message.toRequest(): Request? =
    this.convertor(REQUEST_KEY, Request::class.java.classLoader, Request::class.java)

fun Message.toEvent(): Event? =
    this.convertor(EVENT_KEY, Event::class.java.classLoader, Event::class.java)

fun Message.toOrchestratorMessage(): OrchestratorMessage? =
    this.convertor(
        ORCHESTRATOR_KEY,
        OrchestratorMessage::class.java.classLoader,
        OrchestratorMessage::class.java
    )

