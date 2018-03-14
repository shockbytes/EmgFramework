package at.fhooe.mc.emg.messaging.model

/**
 * @author  Martin Macheiner
 * Date:    24.01.2018
 *
 * ServerMessage is a simple data class for wrapping the incoming message payload and the
 * incoming message type. It is mostly used in the class @see at.fhooe.mc.emg.messaging.EmgMessageParser
 * and in the client implementation at.fhooe.mc.emg.client.EmgClient to distinguish between the types and chosse the right
 * action accordingly.
 *
 */
data class ServerMessage(val type: MessageType, val data: Any) {

    enum class MessageType {
        FREQUENCY, NA
    }

}

