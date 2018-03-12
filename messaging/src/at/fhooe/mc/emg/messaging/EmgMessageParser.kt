package at.fhooe.mc.emg.messaging

import at.fhooe.mc.emg.messaging.model.EmgPacket
import at.fhooe.mc.emg.messaging.model.ServerMessage

class EmgMessageParser(override val protocolVersion: MessageParser.ProtocolVersion) : MessageParser<EmgPacket> {

    private val paramDelimiter = ":"
    private val channelDelimiter = ","
    private val serverMessageDelimiter = "="

    /**
     * @param packet Packet which contains all the information which should be transferred and at least the channels
     * with the EMG data
     *
     * @return String representation of the different channels which are transferred
     */
    override fun buildClientMessage(packet: EmgPacket): String {

        return when (protocolVersion) {
            MessageParser.ProtocolVersion.V1 -> buildV1(packet)
            MessageParser.ProtocolVersion.V2 -> buildV2(packet)
            MessageParser.ProtocolVersion.V3 -> buildV3(packet)
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

    /**
     * @param msg The message which contains the EMG data, this message packet SHOULD be built
     * with the {@link #buildClientMessage(List<Double>, Long, ProtocolVersion)} method, to ensure,
     * that the data is consistent.
     * @return List of all available channels.
     */
    override fun parseClientMessage(msg: String): EmgPacket? {

        val params = msg.split(paramDelimiter).dropLastWhile { it.isBlank() }
        return when (protocolVersion) {
            MessageParser.ProtocolVersion.V1 -> parseV1(params[0])
            MessageParser.ProtocolVersion.V2 -> parseV2(params)
            MessageParser.ProtocolVersion.V3 -> parseV3(params)
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
            if (Character.isDigit(msg[0]) && msg.count { it == '.' } == 1) {
                EmgPacket(listOf(msg.toDouble()))
            } else null
        }
    }

    private fun parseV2(params: List<String>): EmgPacket? {
        return if (params.size == 2) {
            val timestamp = params[0].toLongOrNull() ?: System.currentTimeMillis()
            parseV1(params[1])?.setTimestamp(timestamp)
        } else null
    }

    private fun parseV3(params: List<String>): EmgPacket? {
        return if (params.size == 3) {
            val heartRate = params[2].toIntOrNull() ?: -1
            parseV2(params)?.setHeartRate(heartRate)
        } else null
    }

    // ----------------------------------------------------------------------------------------------------


    override fun parseServerMessage(msg: String): ServerMessage? {
        val params = msg.split(serverMessageDelimiter)
        return if (params.size == 2) {
            getServerMessageForTypeType(params[0], params[1])
        } else null
    }

    private fun getServerMessageForTypeType(type: String, data: String): ServerMessage {
        return when (type) {
            "delay" -> ServerMessage(ServerMessage.MessageType.FREQUENCY, data.toLong())
            else -> ServerMessage(ServerMessage.MessageType.NA, data)
        }
    }

    override fun parseFrequencyMessage(msg: String): Long = msg.trim().split(serverMessageDelimiter)[1].toLong()

}