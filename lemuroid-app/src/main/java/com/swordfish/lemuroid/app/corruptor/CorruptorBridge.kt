package com.swordfish.lemuroid.corruptor

/**
 * Single integration point between the ROM corruptor UI and the running emulator.
 * Kept deliberately thin so the game plays normally even if nothing is registered.
 */
object CorruptorBridge {

    @Volatile
    private var reloadHandler: ((ByteArray) -> Unit)? = null

    /** Called by the game screen once the GLRetroView is ready. */
    fun register(handler: (ByteArray) -> Unit) {
        reloadHandler = handler
    }

    fun unregister() {
        reloadHandler = null
    }

    /** Called by the corruptor screen when RE-CORRUPT is pressed. */
    fun reload(bytes: ByteArray) {
        try {
            reloadHandler?.invoke(bytes)
        } catch (t: Throwable) {
            android.util.Log.e("RomCorruptor", "reload failed", t)
        }
    }
}
