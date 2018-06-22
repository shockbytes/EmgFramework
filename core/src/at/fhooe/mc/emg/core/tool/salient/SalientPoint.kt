package at.fhooe.mc.emg.core.tool.salient

import at.fhooe.mc.emg.core.util.CoreUtils

data class SalientPoint(val x: Int, val y: Double, val confidence: Double, val angle: Int) {

    override fun toString() = "Index: $x ($y)\nConfidence: ${CoreUtils.roundDouble(confidence, 2)}\nAngle: $angle°"

}