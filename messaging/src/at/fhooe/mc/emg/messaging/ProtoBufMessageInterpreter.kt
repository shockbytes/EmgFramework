package at.fhooe.mc.emg.messaging

import at.fhooe.mc.emg.messaging.codec.MessageCodec
import at.fhooe.mc.emg.messaging.model.EmgPacket
import at.fhooe.mc.emg.messaging.model.protobuf.ProtocolBuffers
import java.io.ByteArrayOutputStream
import java.util.*

/**
 * Author:  Martin Macheiner
 * Date:    23.04.2018
 *
 * A binary protocol implementation, which should replace the standard #EmgMessageInterpreter due to its performance
 * drawbacks. Protobuf was used as the binary protocol.
 *
 */
class ProtoBufMessageInterpreter(override val protocolVersion: MessageInterpreter.ProtocolVersion,
                                 private val codec: MessageCodec) : MessageInterpreter<EmgPacket> {

    override fun parseClientMessage(msg: String): EmgPacket? {
        val decoded = Base64.getDecoder().decode(msg)
        val protoPacket = ProtocolBuffers.EmgPacket.parseFrom(decoded)
        return EmgPacket(protoPacket.channelsList, protoPacket.timestamp, protoPacket.heartRate)
    }

    override fun buildFrequencyMessage(fs: Double): String {
        val millis = (1.0 / fs * 1000).toInt()

        val outStream = ByteArrayOutputStream()
        ProtocolBuffers.FrequencyMessage.newBuilder()
                .setDelay(millis)
                .build()
                .writeTo(outStream)
        return codec.encode(outStream.toByteArray())
    }

    override fun buildClientMessage(packet: EmgPacket): String {
        val outStream = ByteArrayOutputStream()
        ProtocolBuffers.EmgPacket.newBuilder()
                .addAllChannels(packet.channels)
                .setTimestamp(packet.timestamp)
                .setHeartRate(packet.heartRate)
                .build()
                .writeTo(outStream)
        return codec.encode(outStream.toByteArray())
    }

    override fun parseServerMessage(msg: String): MessageInterpreter.ServerMessage? {
        val decoded = codec.decode(msg)
        val protoMsg = ProtocolBuffers.ServerMessage.parseFrom(decoded)
        return MessageInterpreter.ServerMessage(MessageInterpreter.ServerMessage.MessageType.values()[protoMsg.type], protoMsg.data)
    }

    override fun parseFrequencyMessage(msg: String): Long {
        val decoded = codec.decode(msg)
        return ProtocolBuffers.FrequencyMessage.parseFrom(decoded).delay.toLong()
    }
}