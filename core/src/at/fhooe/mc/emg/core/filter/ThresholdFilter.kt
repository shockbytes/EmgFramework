package at.fhooe.mc.emg.core.filter

import at.fhooe.mc.emg.designer.EmgComponentType
import at.fhooe.mc.emg.designer.annotation.EmgComponent
import at.fhooe.mc.emg.designer.annotation.EmgComponentInputPort
import at.fhooe.mc.emg.designer.annotation.EmgComponentOutputPort
import at.fhooe.mc.emg.designer.annotation.EmgComponentProperty
import io.reactivex.subjects.PublishSubject

@EmgComponent(type = EmgComponentType.FILTER)
class ThresholdFilter: Filter() {

    override val name = "Threshold filter"

    override val shortName = "TH"

    @JvmField
    @EmgComponentProperty("128")
    var threshold: Int = 128

    @JvmField
    @EmgComponentOutputPort(Double::class)
    var outputPort: PublishSubject<Double> = PublishSubject.create()

    @EmgComponentInputPort(Double::class)
    override fun step(x: Double): Double {
        val data =  if (x > threshold) threshold.toDouble() else x
        outputPort.onNext(data)
        return data
    }

    override fun reset() {
        // Do nothing...
    }

}