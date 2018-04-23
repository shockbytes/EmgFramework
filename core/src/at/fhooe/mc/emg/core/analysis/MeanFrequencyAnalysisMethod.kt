package at.fhooe.mc.emg.core.analysis

import at.fhooe.mc.emg.core.analysis.model.MeanMedianFrequency
import at.fhooe.mc.emg.designer.EmgComponentType
import at.fhooe.mc.emg.designer.annotation.EmgComponent
import at.fhooe.mc.emg.designer.annotation.EmgComponentInputPort
import at.fhooe.mc.emg.designer.annotation.EmgComponentOutputPort
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

@EmgComponent(type = EmgComponentType.RELAY_SINK)
class MeanFrequencyAnalysisMethod(override var view: FrequencyAnalysisView? = null,
                                  override var fs: Double = 100.0) : FrequencyAnalysisMethod {

    override val name = "Mean/Median"

    override val hasDisplay = false

    @EmgComponentOutputPort(MeanMedianFrequency::class)
    val outputPort: PublishSubject<MeanMedianFrequency> = PublishSubject.create()

    @EmgComponentInputPort(DoubleArray::class)
    override fun calculate(input: DoubleArray) {
        Single.fromCallable {
            // TODO calculate median and mean frequency
            MeanMedianFrequency(0.0, 0.0, fs)
        }.subscribeOn(Schedulers.computation()).subscribe { mmf: MeanMedianFrequency ->
            outputPort.onNext(mmf)
        }
    }

}