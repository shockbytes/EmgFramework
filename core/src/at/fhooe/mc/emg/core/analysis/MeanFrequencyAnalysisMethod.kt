package at.fhooe.mc.emg.core.analysis

import at.fhooe.mc.emg.designer.EmgComponent
import at.fhooe.mc.emg.designer.EmgComponentType
import io.reactivex.Single

@EmgComponent(type = EmgComponentType.RELAY_SINK)
class MeanFrequencyAnalysisMethod: FrequencyAnalysisMethod {

    override val name = "Mean/Median"

    override val hasDisplay = false

    override fun calculate(input: DoubleArray, fs: Double, view: FrequencyAnalysisView?): Single<Double> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}