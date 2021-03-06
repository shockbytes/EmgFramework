package at.fhooe.mc.emg.messaging

import at.fhooe.mc.emg.messaging.model.EmgPacket


/**
 * @author  Martin Macheiner
 * Date:    24.01.2018
 * <p>
 * Concrete implementation of {@link MessageInterpreter} for the data class {@link EmgPacket}, which adhere to a really
 * lightweight protocol scheme.
 *
 */

/**
 * Primary constructor of EmgMessageInterpreter
 *
 * @param protocolVersion abstract member implementation of super class, indicating the highest supported protocol version
 */
class EmgMessageInterpreter(override val protocolVersion: MessageInterpreter.ProtocolVersion) : MessageInterpreter<EmgPacket> {

    // Delimiters of supported protocol
    private val paramDelimiter = ":"
    private val channelDelimiter = ","
    private val serverMessageDelimiter = "="


    override fun buildClientMessage(packet: EmgPacket): String {
        return when (protocolVersion) {
            MessageInterpreter.ProtocolVersion.V1 -> buildV1(packet)
            MessageInterpreter.ProtocolVersion.V2 -> buildV2(packet)
            MessageInterpreter.ProtocolVersion.V3 -> buildV3(packet)
        }
    }

    private fun buildV1(packet: EmgPacket): String {
        return packet.channels.joinToString(channelDelimiter)
    }

    private fun buildV2(packet: EmgPacket): String {
        return "${packet.timestamp}$paramDelimiter${buildV1(packet)}"
    }

    private fun buildV3(packet: EmgPacket): String {
        return "${buildV2(packet)}$paramDelimiter${packet.heartRate}"
    }

    // ----------------------------------------------------------------------------------------------------

    override fun buildFrequencyMessage(fs: Double): String {
        val millis = (1.0 / fs * 1000).toInt()
        return "delay=$millis\r\n"
    }

    // ----------------------------------------------------------------------------------------------------

    override fun parseClientMessage(msg: String): EmgPacket? {
        val params = msg.split(paramDelimiter).dropWhile { it.isBlank() }
        return when (protocolVersion) {
            MessageInterpreter.ProtocolVersion.V1 -> parseV1(params[0])
            MessageInterpreter.ProtocolVersion.V2 -> parseV2(params)
            MessageInterpreter.ProtocolVersion.V3 -> parseV3(params)
        }
    }

    private fun parseV1(msg: String): EmgPacket? {
        // > than 1 channel
        return if (msg.contains(",")) {
            val values = msg.split(channelDelimiter)
                    .dropLastWhile { it.isBlank() }
                    .map { it.toDouble() }
            // Do not process damaged packages
            return if (values.isNotEmpty()) EmgPacket(values) else null
        } else {
            // Ensure that single channel has a valid format
            val singleChannel = msg.toDoubleOrNull()
            if (singleChannel != null) {
                EmgPacket(listOf(singleChannel))
            } else null
        }
    }

    /**
     * Assume a minimum version of 2, as V1 packages are discouraged. Therefore return null if V2 has invalid packet
     */
    private fun parseV2(params: List<String>): EmgPacket? {
        return if (params.size >= 2) {
            val timestamp = params[0].toLongOrNull() ?: System.currentTimeMillis()
            parseV1(params[1])?.setTimestamp(timestamp)
        } else null
    }

    /**
     * Just because a MessageInterpreter is capable of parsing V3 messages does not necessarily mean, that
     * devices will send with at least this version. Therefore make heart rate parsing optional
     */
    private fun parseV3(params: List<String>): EmgPacket? {
        // Try get heart rate
        val hr =  if (params.size >= 3) {
            params[2].toIntOrNull() ?: -1
        } else -1
        // Fallback anyway to V2 message parsing
        return parseV2(params)?.setHeartRate(hr)
    }

    // ----------------------------------------------------------------------------------------------------

    override fun parseServerMessage(msg: String): MessageInterpreter.ServerMessage? {
        val params = msg.split(serverMessageDelimiter)
        return if (params.size == 2) {
            getServerMessageForTypeType(params[0], params[1])
        } else null
    }

    override fun parseFrequencyMessage(msg: String): Long = msg.trim().split(serverMessageDelimiter)[1].toLong()

    private fun getServerMessageForTypeType(type: String, data: String): MessageInterpreter.ServerMessage {
        return when (type) {
            "delay" -> MessageInterpreter.ServerMessage(MessageInterpreter.ServerMessage.MessageType.FREQUENCY, data)
            else -> MessageInterpreter.ServerMessage(MessageInterpreter.ServerMessage.MessageType.NA, data)
        }
    }

}