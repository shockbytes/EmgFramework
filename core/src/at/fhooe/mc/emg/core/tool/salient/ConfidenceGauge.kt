package at.fhooe.mc.emg.core.tool.salient

interface ConfidenceGauge {

    fun calculateConfidence(idx: Int, errors: Pair<Double, Double>): Double
}