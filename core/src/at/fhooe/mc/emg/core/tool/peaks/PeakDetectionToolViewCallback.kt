package at.fhooe.mc.emg.core.tool.peaks

import at.fhooe.mc.emg.core.tool.ToolViewCallback

interface PeakDetectionToolViewCallback : ToolViewCallback {

    fun compute(width: Int, threshold: Double, decayRate: Double, isRelative: Boolean)

}