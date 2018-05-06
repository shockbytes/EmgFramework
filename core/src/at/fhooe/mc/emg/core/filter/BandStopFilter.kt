package at.fhooe.mc.emg.core.filter

import at.fhooe.mc.emg.designer.EmgComponentType
import at.fhooe.mc.emg.designer.annotation.EmgComponent
import at.fhooe.mc.emg.designer.annotation.EmgComponentInputPort
import at.fhooe.mc.emg.designer.annotation.EmgComponentOutputPort
import io.reactivex.subjects.PublishSubject


//Band pass butterworth filter order=1 alpha1=50 alpha2=50.05
@EmgComponent(type = EmgComponentType.FILTER, displayTitle = "50 Hz Bandstop filter")
class BandStopFilter : Filter() {

    private var v: DoubleArray = DoubleArray(3)

    override val name = "50Hz Butterworth band stop"
    override val shortName = "BS"

    @JvmField
    @EmgComponentOutputPort(Double::class)
    var outputPort: PublishSubject<Double> = PublishSubject.create()

    @EmgComponentInputPort(Double::class)
    override fun step(x: Double): Double {
        v[0] = v[1]
        v[1] = v[2]
        v[2] = (9.291239228448944232e-1 * x
                + -0.72654252800537055812 * v[0]
                + 1.72654252800536989199 * v[1])
        val data = v[0] + v[2] - 2.000000 * v[1]
        outputPort.onNext(data)
        return data
    }

    override fun reset() {
        v = DoubleArray(3)
        v[0] = 0.0
        v[1] = 0.0
    }

}
