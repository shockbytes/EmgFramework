package at.fhooe.mc.emg.core.computation

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
@EmgComponent(type = EmgComponentType.RELAY, displayTitle = "Mean/Median frequency")
class PeriodicMmfComponent {

    @JvmField
    @EmgComponentProperty("512", "Values for calculation")
    var capacity = 512

    @JvmField
    @EmgComponentProperty("100", "Sampling frequency")
    var samplingFrequency = 100.0

    @JvmField
    @EmgComponentProperty("true", "Exclude offset in FFT calculation")
    var excludeOffset = true

    @JvmField
    @EmgComponentOutputPort(MeanMedianFrequency::class)
    var outputPort: PublishSubject<MeanMedianFrequency> = PublishSubject.create()

    private val data: MutableList<Double> = ArrayList(capacity)

    @EmgComponentInputPort(Double::class)
    fun update(x: Double) {

        data.add(x)
        if (data.size >= capacity) {
            AnalysisUtils.meanMedianFrequency(data.toDoubleArray(), samplingFrequency, excludeOffset)
                    .subscribe(Consumer { outputPort.onNext(it) })
            data.clear()
        }
    }

    fun reset() {
        data.clear()
    }

}