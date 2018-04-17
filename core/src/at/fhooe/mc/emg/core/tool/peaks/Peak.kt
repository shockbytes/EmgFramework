package at.fhooe.mc.emg.core.tool.peaks

data class Peak(val x: Double, val y: Double) {

    fun toPrettyString(): String = "${x.toInt()}, $y"
}