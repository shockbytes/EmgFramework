package at.fhooe.mc.emg.core.tool.salient

import at.fhooe.mc.emg.core.tool.ToolViewCallback

/**
 * Author:  Martin Macheiner
 * Date:    17.04.2018
 */
interface SalientPointToolViewCallback : ToolViewCallback {

    fun updateParameter(confidence: Double, angle: Int)

}