package at.fhooe.mc.emg.core.filter

import at.fhooe.mc.emg.core.util.filter.sg.CurveSmooth
import java.util.*

class SavitzkyGolayFilter(private val sgFilterWidth: Int) : Filter() {

    private val buffer: LinkedList<Double> = LinkedList()

    override val name: String
        get() = "Savitzky Golay Filter"

    override val shortName: String
        get() = "SG"

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

}
