package at.fhooe.mc.emg.messaging.codec

/**
 * Author:  Martin Macheiner
 * Date:    11.05.2018
 *
 * Some messages can be additionally encoded and decoded, due to
 * various reasons. A binary protocol cannot be transferred as a
 * string representation out of the box. Therefore codecs can
 * transform them into a transportable string representation.
 *
 */
interface MessageCodec {

    fun encode(out: ByteArray): String

    fun decode(msg: String): ByteArray
}