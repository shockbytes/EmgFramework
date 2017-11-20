package at.fhooe.mc.emg.filter

import java.util.*

class RunningAverageFilter(private val size: Int) : Filter() {

    private var sum: Double = 0.0
    private var rAvg: Double = 0.0
    private val buffer: LinkedList<Double>

    override val name: String
        get() = "Running average"

    override val shortName: String
        get() = "rAvg"

    init {
        sum = 0.0
        rAvg = 0.0
        buffer = LinkedList()
    }

    override fun step(x: Double): Double {

        if (buffer.size == size) {
            sum -= buffer.removeFirst()
        }

        sum += x
        buffer.addLast(x)
        rAvg = sum / buffer.size

        return rAvg
    }

}
