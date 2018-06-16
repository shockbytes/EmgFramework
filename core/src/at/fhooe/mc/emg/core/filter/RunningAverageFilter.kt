package at.fhooe.mc.emg.core.filter

import at.fhooe.mc.emg.designer.EmgComponentType
import at.fhooe.mc.emg.designer.annotation.EmgComponent
import at.fhooe.mc.emg.designer.annotation.EmgComponentInputPort
import at.fhooe.mc.emg.designer.annotation.EmgComponentOutputPort
import at.fhooe.mc.emg.designer.annotation.EmgComponentProperty
import io.reactivex.subjects.PublishSubject
import java.util.*

@EmgComponent(type = EmgComponentType.FILTER, displayTitle = "Running average filter")
class RunningAverageFilter : Filter() {

    private var sum: Double = 0.0
    private var rAvg: Double = 0.0
    private var buffer: LinkedList<Double> = LinkedList()

    @JvmField
    @EmgComponentProperty("750", "Window size")
    var size: Int = 400

    @JvmField
    @EmgComponentOutputPort(Double::class)
    var outputPort: PublishSubject<Double> = PublishSubject.create()

    override val name = "Running average"
    override val shortName = "rAvg"

    @EmgComponentInputPort(Double::class)
    override fun step(x: Double): Double {

        if (buffer.size == size) {
            sum -= buffer.removeFirst()
        }

        sum += x
        buffer.addLast(x)
        rAvg = sum / buffer.size
        outputPort.onNext(rAvg)
        return rAvg
    }

    override fun reset() {
        buffer = LinkedList()
        sum = 0.0
        rAvg = 0.0
        buffer.clear()
    }

}
