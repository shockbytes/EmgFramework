package at.fhooe.mc.emg.messaging

import at.fhooe.mc.emg.messaging.model.EmgPacket
import at.fhooe.mc.emg.messaging.model.ServerMessage
import at.fhooe.mc.emg.messaging.model.protobuf.ProtocolBuffers
import java.io.ByteArrayOutputStream

/**
 * Author:  Martin Macheiner
 * Date:    23.04.2018
 *
 * A binary protocol implementation, which should replace the standard #EmgMessageParser due to its performance
 * drawbacks. Protobuf was used as the binary protocol.
 *
 */
class ProtoBufMessageParser(override val protocolVersion: MessageParser.ProtocolVersion) : MessageParser<EmgPacket> {

    override fun parseClientMessage(msg: String): EmgPacket? {
        val protoPacket = ProtocolBuffers.EmgPacket.parseFrom(msg.toByteArray())
        return EmgPacket(protoPacket.channelsList, protoPacket.timestamp, protoPacket.heartRate)
    }

    override fun buildFrequencyMessage(fs: Double): String {
        val millis = (1.0 / fs * 1000).toInt()

        val outStream = ByteArrayOutputStream()
        ProtocolBuffers.FrequencyMessage.newBuilder()
                .setDelay(millis)
                .build()
                .writeTo(outStream)
        return outStream.toString()
    }

    override fun buildClientMessage(packet: EmgPacket): String {
        val outStream = ByteArrayOutputStream()
        ProtocolBuffers.EmgPacket.newBuilder()
                .addAllChannels(packet.channels)
                .setTimestamp(packet.timestamp)
                .setHeartRate(packet.heartRate)
                .build()
                .writeTo(outStream)
        return outStream.toString()
    }

    override fun parseServerMessage(msg: String): ServerMessage? {
        val protoMsg = ProtocolBuffers.ServerMessage.parseFrom(msg.toByteArray())
        return ServerMessage(ServerMessage.MessageType.values()[protoMsg.type], protoMsg.data)
    }

    override fun parseFrequencyMessage(msg: String): Long {
        return ProtocolBuffers.FrequencyMessage.parseFrom(msg.toByteArray()).delay.toLong()
    }
}