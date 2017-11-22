package at.fhooe.mc.emg.filter

import at.fhooe.mc.emg.filter.sg.SgFilterImplementation
import java.util.*

// TODO Find working implementation
class SavitzkyGolayFilter(private val sgFilterWidth: Int, private val degree: Int) : Filter() {

    private val buffer: LinkedList<Double> = LinkedList()

    override val name: String
        get() = "Savitzky Golay Filter"

    override val shortName: String
        get() = "SG"


    private val coeffs: DoubleArray
    private val filter: SgFilterImplementation

    init {
        filter = SgFilterImplementation(sgFilterWidth/2, sgFilterWidth/2)
        coeffs = SgFilterImplementation.computeSGCoefficients(sgFilterWidth/2, sgFilterWidth, degree)
    }

    override fun step(x: Double): Double {

        if (buffer.size == sgFilterWidth) {
            buffer.removeFirst()
        }
        buffer.addLast(x)

        return if (buffer.size > sgFilterWidth / 2) {
            filter.smooth(buffer.toDoubleArray(), coeffs)[sgFilterWidth/2]
        } else {
            0.0
        }
    }

}
