package at.fhooe.mc.emg.messaging

import at.fhooe.mc.emg.messaging.model.EmgPacket
import com.google.gson.Gson
import com.google.gson.JsonObject

class JsonMessageInterpreter : MessageInterpreter<EmgPacket> {

    private val gson = Gson()

    override val protocolVersion: MessageInterpreter.ProtocolVersion = MessageInterpreter.ProtocolVersion.V3

    override fun parseClientMessage(msg: String): EmgPacket? {
        return try {
            gson.fromJson(msg, EmgPacket::class.java)
        } catch (e: Exception) {
            null
        }
    }

    override fun buildFrequencyMessage(fs: Double): String {
        val millis = (1.0 / fs * 1000).toInt()
        return JsonObject().addProperty("delay", millis).toString()
    }

    override fun buildClientMessage(packet: EmgPacket): String {
        return gson.toJson(packet)
    }

    override fun parseServerMessage(msg: String): MessageInterpreter.ServerMessage? {
        return gson.fromJson(msg, MessageInterpreter.ServerMessage::class.java)
    }

    override fun parseFrequencyMessage(msg: String): Long {
        return gson.fromJson(msg, Map::class.java)["delay"] as Long
    }
}