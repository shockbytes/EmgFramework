package at.fhooe.mc.emg.messaging.model.experimental

data class EmgBurst(val timestamp: Long, val heartRate: Short, val emg: List<List<Float>>)