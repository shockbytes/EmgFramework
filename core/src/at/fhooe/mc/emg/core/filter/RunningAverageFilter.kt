package at.fhooe.mc.emg.core.filter

import java.util.*

class RunningAverageFilter(private val size: Int) : Filter() {

    private var sum: Double = 0.0
    private var rAvg: Double = 0.0
    private var buffer: LinkedList<Double> = LinkedList()

    override val name = "Running average"
    override val shortName = "rAvg"

    override fun step(x: Double): Double {

        if (buffer.size == size) {
            sum -= buffer.removeFirst()
        }

        sum += x
        buffer.addLast(x)
        rAvg = sum / buffer.size
        return rAvg
    }

    override fun reset() {
        buffer = LinkedList()
        sum = 0.0
        rAvg = 0.0
        buffer.clear()
    }

}
