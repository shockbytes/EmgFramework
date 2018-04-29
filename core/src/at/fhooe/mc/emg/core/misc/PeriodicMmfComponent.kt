package at.fhooe.mc.emg.core.misc

import at.fhooe.mc.emg.core.analysis.AnalysisUtils
import at.fhooe.mc.emg.core.analysis.model.MeanMedianFrequency
import at.fhooe.mc.emg.designer.EmgComponentType
import at.fhooe.mc.emg.designer.annotation.EmgComponent
import at.fhooe.mc.emg.designer.annotation.EmgComponentInputPort
import at.fhooe.mc.emg.designer.annotation.EmgComponentOutputPort
import at.fhooe.mc.emg.designer.annotation.EmgComponentProperty
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject

/**
 * Periodic Mean Median Frequency computation component
 */
@EmgComponent(type = EmgComponentType.RELAY)
class PeriodicMmfComponent {

    @JvmField
    @EmgComponentProperty("512")
    var capacity = 512

    @JvmField
    @EmgComponentProperty("100")
    var samplingFrequency = 100.0

    @JvmField
    @EmgComponentOutputPort(MeanMedianFrequency::class)
    var outputPort: PublishSubject<MeanMedianFrequency> = PublishSubject.create()

    private val data: MutableList<Double> = ArrayList(capacity)

    @EmgComponentInputPort(Double::class)
    fun update(x: Double) {

        data.add(x)
        if (data.size >= capacity) {
            AnalysisUtils.meanMedianFrequency(data.toDoubleArray(), samplingFrequency)
                    .subscribe(Consumer { outputPort.onNext(it) })
            data.clear()
        }
    }

}