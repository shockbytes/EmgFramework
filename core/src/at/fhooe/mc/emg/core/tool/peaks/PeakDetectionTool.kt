package at.fhooe.mc.emg.core.tool.peaks


import at.fhooe.mc.emg.core.EmgPresenter
import at.fhooe.mc.emg.core.tool.Tool
import at.fhooe.mc.emg.designer.EmgComponent
import at.fhooe.mc.emg.designer.EmgComponentType

/**
 * Author:  Martin Macheiner
 * Date:    04.07.2017
 */

@EmgComponent(type = EmgComponentType.RELAY_SINK)
class PeakDetectionTool(override var toolView: PeakDetectionToolView? = null) : Tool, PeakDetectionToolViewCallback {

    override val name = "Peak detection"

    private var presenter: EmgPresenter? = null

    override fun start(presenter: EmgPresenter, showViewImmediate: Boolean) {
        toolView?.setup(this, showViewImmediate)
        this.presenter = presenter
    }

    override fun compute(width: Int, threshold: Double, decayRate: Double, isRelative: Boolean) {

        val yValues = presenter?.visualView?.dataForFrequencyAnalysis
        if (yValues != null) {
            // If current data pointer not available, then point to 0
            val xStart = (presenter?.currentDataPointer ?: yValues.size) - yValues.size
            PeakDetector.detectPeakLocations(yValues, width, threshold, decayRate, isRelative).subscribe({ peaks ->

                val yValuesPeaks = DoubleArray(peaks.size) { yValues[peaks[it]] }
                val xValuesPeaks = peaks.map { it.toDouble() + xStart }.toDoubleArray()
                val xValues = DoubleArray(yValues.size) { it.toDouble() + xStart }

                // Add xStart to the xValues the stay in sync with the received data in the main window
                toolView?.showPlotData(xValues, yValues, xValuesPeaks, yValuesPeaks)

                // Additionally show the peaks in a ListView
                toolView?.showPeaksDetail(xValuesPeaks.mapIndexed { idx, it -> Peak(it, yValuesPeaks[idx]) })

            }, {
                toolView?.showError(it.localizedMessage, it.javaClass.simpleName)
            })
        } else {
            toolView?.showError("There is no data provided! yValues = null", "Empty data")
        }
    }

    override fun onViewClosed() {
        presenter = null
        // No further cleanup necessary
    }

}
