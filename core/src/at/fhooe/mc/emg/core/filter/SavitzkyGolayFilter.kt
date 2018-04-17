package at.fhooe.mc.emg.core.filter

import at.fhooe.mc.emg.core.util.filter.sg.CurveSmooth
import at.fhooe.mc.emg.designer.EmgComponent
import at.fhooe.mc.emg.designer.EmgComponentType
import java.util.*

@EmgComponent(type = EmgComponentType.FILTER)
class SavitzkyGolayFilter(private val sgFilterWidth: Int = 10) : Filter() {

    private var buffer: LinkedList<Double> = LinkedList()

    override val name = "Savitzky Golay Filter"
    override val shortName = "SG"

    override fun step(x: Double): Double {

        if (buffer.size == sgFilterWidth) {
            buffer.removeFirst()
        }
        buffer.addLast(x)

        return if (buffer.size > sgFilterWidth / 2) {
            CurveSmooth(buffer.toDoubleArray())
                    .savitzkyGolay(sgFilterWidth)?.get(sgFilterWidth / 2) ?: 0.toDouble()
        } else {
            0.0
        }
    }

    override fun reset() {
        buffer = LinkedList()
        buffer.clear()
    }

}
