package at.fhooe.mc.emg.core.tool.salient

data class SalientPoint(val x: Int, val y: Double, val confidence: Double, val angle: Int) {

    override fun toString() = "Index: $x ($y)\nConfidence: $confidence\nAngle: $angle°"

}