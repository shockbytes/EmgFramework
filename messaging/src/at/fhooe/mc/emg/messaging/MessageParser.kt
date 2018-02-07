package at.fhooe.mc.emg.messaging

import at.fhooe.mc.emg.messaging.model.ServerMessage

interface MessageParser<T> {

    // --------------- Methods called from the driver side ---------------

    fun parseClientMessage(msg: String): T?

    fun buildFrequencyMessage(fs: Double): String

    // --------------- Methods called from the client side ---------------

    fun buildClientMessage(packet: T): String

    fun parseServerMessage(msg: String): ServerMessage?

    fun parseFrequencyMessage(msg: String): Long
}