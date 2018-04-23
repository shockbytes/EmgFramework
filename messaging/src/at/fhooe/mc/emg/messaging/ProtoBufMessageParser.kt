package at.fhooe.mc.emg.messaging

import at.fhooe.mc.emg.messaging.model.EmgPacket
import at.fhooe.mc.emg.messaging.model.ServerMessage

/**
 * Author:  Martin Macheiner
 * Date:    23.04.2018
 *
 * TODO Implement ProtoBuf protocol
 */
class ProtoBufMessageParser(override val protocolVersion: MessageParser.ProtocolVersion) : MessageParser<EmgPacket> {

    override fun parseClientMessage(msg: String): EmgPacket? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun buildFrequencyMessage(fs: Double): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun buildClientMessage(packet: EmgPacket): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun parseServerMessage(msg: String): ServerMessage? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun parseFrequencyMessage(msg: String): Long {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}