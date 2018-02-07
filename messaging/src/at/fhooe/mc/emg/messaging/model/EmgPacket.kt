package at.fhooe.mc.emg.messaging.model

data class EmgPacket(var channels: List<Double>, var timestamp: Long = 0, var heartRate: Int = 0) {

    fun setTimestamp(timestamp: Long?): EmgPacket {
        this.timestamp = timestamp ?: 0
        return this
    }

    fun setHeartRate(heartRate: Int?): EmgPacket {
        this.heartRate = heartRate ?: 0
        return this
    }

}
