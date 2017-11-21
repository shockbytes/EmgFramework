package at.fhooe.mc.emg.filter

import java.util.*

class SavitzkyGolayFilter(private val sgFilterWidth: Int) : Filter() {
    private val buffer: LinkedList<Double>

    override val name: String
        get() = "Savitzky Golay Filter"

    override val shortName: String
        get() = "SG"

    init {
        buffer = LinkedList()
    }

    override fun step(x: Double): Double {

        if (buffer.size == sgFilterWidth) {
            buffer.removeFirst()
        }
        buffer.addLast(x)

        return if (buffer.size > sgFilterWidth / 2) {
            0.0
            // TODO
            /*CurveSmooth(buffer.stream().mapToDouble { d1 -> d1 }.toArray())
                    .savitzkyGolay(sgFilterWidth)[sgFilterWidth / 2]
                    */
        } else {
            0.0
        }
    }

}
