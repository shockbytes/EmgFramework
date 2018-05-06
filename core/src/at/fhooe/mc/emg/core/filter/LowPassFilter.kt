package at.fhooe.mc.emg.core.filter

import at.fhooe.mc.emg.designer.EmgComponentType
import at.fhooe.mc.emg.designer.annotation.EmgComponent
import at.fhooe.mc.emg.designer.annotation.EmgComponentInputPort
import at.fhooe.mc.emg.designer.annotation.EmgComponentOutputPort
import io.reactivex.subjects.PublishSubject

@EmgComponent(type = EmgComponentType.FILTER, displayTitle = "10 Hz low pass filter")
class LowPassFilter : Filter() {

    private var v: DoubleArray = DoubleArray(2)

    override val name = "10Hz Chebyshev low pass"
    override val shortName = "LP"

    @JvmField
    @EmgComponentOutputPort(Double::class)
    var outputPort: PublishSubject<Double> = PublishSubject.create()

    @EmgComponentInputPort(Double::class)
    override fun step(x: Double): Double {
        v[0] = v[1]
        v[1] = 2.456770461833230612e-1 * x + 0.50864590763335382206 * v[0]
        val data =  v[0] + v[1]
        outputPort.onNext(data)
        return data
    }

    override fun reset() {
        v = DoubleArray(2)
        v[0] = 0.0
    }

}
