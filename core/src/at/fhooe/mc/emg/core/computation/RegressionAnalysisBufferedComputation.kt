package at.fhooe.mc.emg.core.computation

import io.reactivex.Single
import org.apache.commons.math3.stat.regression.SimpleRegression

class RegressionAnalysisBufferedComputation(initialCapacity: Int = 6) : BufferedComputation<Pair<Double, Double>, Double>() {

    init {
        capacity = initialCapacity
    }

    override fun computeValue(data: List<Pair<Double, Double>>): Single<Double> {
        return Single.fromCallable {
            val regression = SimpleRegression(false)
            regression.addData(prepareData(data))
            regression.slope
        }
    }

    private fun prepareData(data: List<Pair<Double, Double>>): Array<DoubleArray> {
        return data.map { doubleArrayOf(it.first, it.second) }.toTypedArray()
    }

}