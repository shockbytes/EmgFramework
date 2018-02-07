package at.fhooe.mc.emg.messaging.model

data class ServerMessage(val type: MessageType, val data: Any) {

    enum class MessageType {
        FREQUENCY, NA
    }

}

