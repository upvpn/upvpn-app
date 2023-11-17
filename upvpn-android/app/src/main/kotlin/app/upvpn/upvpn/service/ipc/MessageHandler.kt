package app.upvpn.upvpn.service.ipc

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import app.upvpn.upvpn.BuildConfig
import kotlin.reflect.KClass

class MessageHandler<T : Any>(
    private val looper: Looper,
    private val fromMessage: (Message) -> T?
) :
    Handler(looper) {
    private val tag = "MessageHandler"

    private var handlers: HashMap<KClass<out T>, (T) -> Unit> = HashMap()

    fun <S : T> registerHandler(klass: KClass<S>, handler: (S) -> Unit) {
        handlers.put(klass) { handler(it as S) }
    }

    override fun handleMessage(msg: Message) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, "handleMessage $msg")
        }

        val internalMessage = fromMessage(msg)

        if (internalMessage != null) {
            val handle = handlers[internalMessage::class]
            handle?.invoke(internalMessage)
        } else {
            Log.d(tag, "cannot get internal message")
        }
    }
}
