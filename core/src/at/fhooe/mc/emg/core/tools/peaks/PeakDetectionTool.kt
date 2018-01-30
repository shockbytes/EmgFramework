package at.fhooe.mc.emg.core.tools.peaks

import at.fhooe.mc.emg.core.EmgPresenter
import at.fhooe.mc.emg.core.tools.Tool
import java.awt.geom.Point2D

/**
 * Author:  Mescht
 * Date:    04.07.2017
 */
class PeakDetectionTool(override var view: PeakDetectionView? = null) : Tool, PeakDetectionViewCallback {

    override val name = "Peak detection"

    private var presenter: EmgPresenter? = null

    override fun start(presenter: EmgPresenter, showViewImmediate: Boolean) {
        view?.setup(this, showViewImmediate)
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
                view?.showPlotData(xValues, yValues, xValuesPeaks, yValuesPeaks)

                // Additionally show the peaks in a ListView
                view?.showPeaksDetail(xValuesPeaks.mapIndexed { idx, it -> Point2D.Double(it, yValuesPeaks[idx]) })

            }, {
                view?.showError(it.localizedMessage, it.javaClass.simpleName)
            })
        } else {
            view?.showError("There is no data provided! yValues = null", "Empty data")
        }
    }

    override fun onViewClosed() {
        presenter = null
        // No further cleanup necessary
    }

}
