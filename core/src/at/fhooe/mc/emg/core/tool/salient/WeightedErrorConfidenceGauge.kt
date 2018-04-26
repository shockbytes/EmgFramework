package at.fhooe.mc.emg.core.tool.salient

class WeightedErrorConfidenceGauge(private val weightFirstLine: Float,
                                   private val weightSecondLine: Float): ConfidenceGauge {

    override fun calculateConfidence(idx: Int, errors: Pair<Double, Double>): Double {
        return (errors.first * weightFirstLine) + (errors.second * weightSecondLine)
    }

}