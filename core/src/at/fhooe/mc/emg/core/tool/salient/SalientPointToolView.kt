package at.fhooe.mc.emg.core.tool.salient

import at.fhooe.mc.emg.core.tool.ToolView

/**
 * Author:  Martin Macheiner
 * Date:    17.04.2018
 */
interface SalientPointToolView: ToolView<SalientPointToolViewCallback> {

    fun updateChart(xVals: List<Double>, yVals: List<Double>)

    fun drawSalientPoint(point: SalientPoint)

    fun clearSalientPoint()

}