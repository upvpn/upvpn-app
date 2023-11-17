package app.upvpn.upvpn.service.ipc.extensions

import android.os.Bundle
import android.os.Message
import app.upvpn.upvpn.service.ipc.Request

const val REQUEST_KEY = "request"
const val REQUEST_WHAT = 2

fun Request.toMessage(): Message {
    return Message.obtain().also {
        var bundle = Bundle()
        bundle.putParcelable(REQUEST_KEY, this)
        it.what = REQUEST_WHAT
        it.data = bundle
    }
}
