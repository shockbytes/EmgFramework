package at.fhooe.mc.emg.core.tools.peaks

import at.fhooe.mc.emg.core.tools.ToolView

/**
 * Author:  Mescht
 * Date:    20.11.2017
 */
interface PeakDetectionView: ToolView<Unit> {

    fun showData(xValues: IntArray, yValues: DoubleArray, peaks: IntArray)

}