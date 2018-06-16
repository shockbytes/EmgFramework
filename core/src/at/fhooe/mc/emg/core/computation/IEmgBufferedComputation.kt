package at.fhooe.mc.emg.core.computation

import io.reactivex.Single

class IEmgBufferedComputation: BufferedComputation<Double, Double>() {

    override fun computeValue(data: List<Double>): Single<Double> {
        return Single.fromCallable {
            data.sumByDouble { Math.abs(it) }
        }
    }

}