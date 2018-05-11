package at.fhooe.mc.emg.messaging.codec

import java.util.*

class StandardBase64Codec: MessageCodec {

    override fun encode(out: ByteArray): String {
        return String(Base64.getEncoder().encode(out))
    }

    override fun decode(msg: String): ByteArray {
        return Base64.getDecoder().decode(msg.trim())
    }

}