package at.fhooe.mc.emg.core.misc

import at.fhooe.mc.emg.core.analysis.model.MeanMedianFrequency
import at.fhooe.mc.emg.core.util.rms
import at.fhooe.mc.emg.designer.EmgComponentType
import at.fhooe.mc.emg.designer.annotation.EmgComponent
import at.fhooe.mc.emg.designer.annotation.EmgComponentInputPort
import at.fhooe.mc.emg.designer.annotation.EmgComponentOutputPort
import at.fhooe.mc.emg.designer.annotation.EmgComponentProperty
import io.reactivex.subjects.PublishSubject

/**
 * Periodic Mean Median Frequency computation component
 */
@EmgComponent(type = EmgComponentType.RELAY)
class PeriodicMmfComponent {

    @EmgComponentProperty
    var capacity = 2000

    @JvmField
    @EmgComponentOutputPort(MeanMedianFrequency::class)
    var outputPort: PublishSubject<MeanMedianFrequency> = PublishSubject.create()

    private val data: MutableList<Double> = ArrayList(capacity)

    @EmgComponentInputPort(Double::class)
    fun update(x: Double) {

        data.add(x)
        if (data.size >= capacity) {
            // TODO Calculate mean and mediean frequency
            val rms = data.toDoubleArray().rms()
            //outputPort.onNext(rms)
            data.clear()
        }
    }

}