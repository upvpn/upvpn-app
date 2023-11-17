package app.upvpn.upvpn.service.ipc.extensions

import android.os.Bundle
import android.os.Message
import app.upvpn.upvpn.service.ipc.Event

const val EVENT_KEY = "event"
const val EVENT_WHAT = 1
fun Event.toMessage(): Message {
    return Message.obtain().also {
        var bundle = Bundle()
        bundle.putParcelable(EVENT_KEY, this)
        it.what = EVENT_WHAT
        it.data = bundle
    }
}
