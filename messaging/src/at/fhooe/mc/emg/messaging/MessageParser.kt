package at.fhooe.mc.emg.messaging

import at.fhooe.mc.emg.messaging.model.ServerMessage

interface MessageParser<T> {

    enum class ProtocolVersion {
        // Easiest protocol, just multiple channel values divided by the channel delimiter.
        // Example: 1,2,3
        V1,
        // Incorporates timestamp. This version should tackle the problem of latencies. Now every packet has is stamped.
        // Example: 687801928:1,2,3
        V2,
        // Adds the possibility to measure and send heart rate as well. This is especially useful when a Conconi test
        // should be conducted. Because of the data overhead it is not recommended to use this, when it is not necessary.
        // Example: 687801928:1,2,3:65
        V3
    }

    abstract val protocolVersion: ProtocolVersion

    // --------------- Methods called from the driver side ---------------

    fun parseClientMessage(msg: String): T?

    fun buildFrequencyMessage(fs: Double): String

    // --------------- Methods called from the client side ---------------

    fun buildClientMessage(packet: T): String

    fun parseServerMessage(msg: String): ServerMessage?

    fun parseFrequencyMessage(msg: String): Long
}