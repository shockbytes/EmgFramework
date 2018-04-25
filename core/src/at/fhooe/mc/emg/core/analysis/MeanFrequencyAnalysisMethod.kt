package at.fhooe.mc.emg.core.analysis

import at.fhooe.mc.emg.core.analysis.model.MeanMedianFrequency
import at.fhooe.mc.emg.designer.EmgComponentType
import at.fhooe.mc.emg.designer.annotation.EmgComponent
import at.fhooe.mc.emg.designer.annotation.EmgComponentInputPort
import at.fhooe.mc.emg.designer.annotation.EmgComponentOutputPort
import io.reactivex.subjects.PublishSubject

@EmgComponent(type = EmgComponentType.RELAY_SINK)
class MeanFrequencyAnalysisMethod(override var view: FrequencyAnalysisView? = null,
                                  override var fs: Double = 100.0) : FrequencyAnalysisMethod {

    override val name = "Mean/Median"

    override val hasDisplay = false

    @JvmField
    @EmgComponentOutputPort(MeanMedianFrequency::class)
    val outputPort: PublishSubject<MeanMedianFrequency> = PublishSubject.create()

    @EmgComponentInputPort(DoubleArray::class)
    override fun calculate(input: DoubleArray) {
        AnalysisUtils.meanMedianFrequency(input, fs).subscribe { mmf: MeanMedianFrequency ->
            outputPort.onNext(mmf)
        }
    }

}