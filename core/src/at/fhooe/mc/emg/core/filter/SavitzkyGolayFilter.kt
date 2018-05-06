package at.fhooe.mc.emg.core.filter

import at.fhooe.mc.emg.core.util.filter.sg.CurveSmooth
import at.fhooe.mc.emg.designer.EmgComponentType
import at.fhooe.mc.emg.designer.annotation.EmgComponent
import at.fhooe.mc.emg.designer.annotation.EmgComponentInputPort
import at.fhooe.mc.emg.designer.annotation.EmgComponentOutputPort
import at.fhooe.mc.emg.designer.annotation.EmgComponentProperty
import io.reactivex.subjects.PublishSubject
import java.util.*

@EmgComponent(type = EmgComponentType.FILTER, displayTitle = "Savitzky-Golay filter")
class SavitzkyGolayFilter : Filter() {

    private var buffer: LinkedList<Double> = LinkedList()

    override val name = "Savitzky Golay Filter"
    override val shortName = "SG"


    @JvmField
    @EmgComponentProperty("10", "Savitzky-Golay filter width")
    var sgFilterWidth: Int = 10

    @JvmField
    @EmgComponentOutputPort(Double::class)
    var outputPort: PublishSubject<Double> = PublishSubject.create()

    @EmgComponentInputPort(Double::class)
    override fun step(x: Double): Double {

        if (buffer.size == sgFilterWidth) {
            buffer.removeFirst()
        }
        buffer.addLast(x)

        val data =  if (buffer.size > sgFilterWidth / 2) {
            CurveSmooth(buffer.toDoubleArray())
                    .savitzkyGolay(sgFilterWidth)?.get(sgFilterWidth / 2) ?: 0.toDouble()
        } else {
            0.0
        }
        outputPort.onNext(data)
        return data
    }

    override fun reset() {
        buffer = LinkedList()
        buffer.clear()
    }

}
