package at.fhooe.mc.emg.core.tools.peaks

import at.fhooe.mc.emg.core.tools.ToolView

/**
 * Author:  Mescht
 * Date:    20.11.2017
 */
interface PeakDetectionView : ToolView<PeakDetectionViewCallback> {

    /**
     * @param xValues x values of data from 0..n
     * @param yValues original y values of recorded data
     * @param xValuesPeaks array which contains all the peaks, basically the x values for peak series
     * @param yValuesPeaks already populated array with length of xValues, where the contained data is zero if no
     * peak, and the original y value where a peak is detected
     */
    fun showPlotData(xValues: DoubleArray, yValues: DoubleArray, xValuesPeaks: DoubleArray, yValuesPeaks: DoubleArray)

    fun showPeaksDetail(peaks: List<Peak>)

    fun showError(cause: String, title: String)

}