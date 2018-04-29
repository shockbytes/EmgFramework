package at.fhooe.mc.emg.core.util


fun DoubleArray.rms(): Double {
    val sum = sumByDouble { it * it }
    return Math.sqrt(sum / this.size)
}

fun List<Double>.rmse(approx: List<Double>): Double {
    var sum = 0.0
    for (i in indices) {
        val diff = get(i) - approx[i]
        val pow = Math.pow(diff, 2.0)
        sum += pow
    }
    return Math.sqrt(sum / this.size)
}

fun DoubleArray.rmse(approx: List<Double>): Double {
    return toList().rmse(approx)
}

fun Double.round(): Int {
    return Math.round(this).toInt()
}

fun List<Double>.meanFrequency(): Double {
    return toDoubleArray().meanFrequency()
}

fun List<Double>.medianFrequency(): Double {
    return toDoubleArray().medianFrequency()
}

fun DoubleArray.meanFrequency(): Double {
    TODO("Calculate frequency")
}

fun DoubleArray.medianFrequency(): Double {
    TODO("Calculate frequency")
}