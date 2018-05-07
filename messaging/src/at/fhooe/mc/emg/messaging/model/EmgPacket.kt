package at.fhooe.mc.emg.messaging.model

/**
 * @author  Martin Macheiner
 * Date:    24.01.2018
 *
 * This class holds all the information, which is parsed and packed with the @see at.fhooe.mc.emg.messaging.EmgMessageInterpreter.
 * The class EmgPacket is the class which is used for transferring data with this message parser.
 *
 */
data class EmgPacket(var channels: List<Double>, var timestamp: Long = 0, var heartRate: Int = -1) {

    fun setTimestamp(timestamp: Long?): EmgPacket {
        this.timestamp = timestamp ?: 0
        return this
    }

    fun setHeartRate(heartRate: Int?): EmgPacket {
        this.heartRate = heartRate ?: 0
        return this
    }

    override fun toString(): String {
        return "$timestamp:${channels.joinToString(",")}:$heartRate\n"
    }

}
