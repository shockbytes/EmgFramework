package at.fhooe.mc.emg.core.util


fun DoubleArray.rms(): Double {
    val sum = sumByDouble { it * it }
    return Math.sqrt(sum / this.size)
}

