package at.fhooe.mc.emg.core.filter

import at.fhooe.mc.emg.designer.EmgComponent
import at.fhooe.mc.emg.designer.EmgComponentType
import java.util.*

@EmgComponent(type = EmgComponentType.FILTER)
class RunningAverageFilter(private val size: Int = 30) : Filter() {

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
