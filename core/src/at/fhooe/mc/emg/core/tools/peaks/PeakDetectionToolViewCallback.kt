package at.fhooe.mc.emg.core.tools.peaks

import at.fhooe.mc.emg.core.tools.ToolViewCallback

interface PeakDetectionToolViewCallback : ToolViewCallback {

    fun compute(width: Int, threshold: Double, decayRate: Double, isRelative: Boolean)

}