package at.fhooe.mc.emg.core.tool.peaks


import at.fhooe.mc.emg.core.Toolable
import at.fhooe.mc.emg.core.tool.Tool
import at.fhooe.mc.emg.designer.EmgComponentType
import at.fhooe.mc.emg.designer.annotation.EmgComponent
import at.fhooe.mc.emg.designer.annotation.EmgComponentInputPort

/**
 * Author:  Martin Macheiner
 * Date:    04.07.2017
 */

@EmgComponent(type = EmgComponentType.RELAY_SINK)
class PeakDetectionTool(override var toolView: PeakDetectionToolView? = null) : Tool, PeakDetectionToolViewCallback {

    override val name = "Peak Detection"

    private var toolable: Toolable? = null

    private var width: Int = PeakDetector.defaultWidth
    private var threshold: Double = PeakDetector.defaultThreshold
    private var decayRate: Double = PeakDetector.defaultDecayRate
    private var isRelative: Boolean = PeakDetector.defaultIsRelative

    override fun start(toolable: Toolable, showViewImmediate: Boolean) {
        toolView?.setup(this, showViewImmediate)
        this.toolable = toolable
    }

    override fun computeManually() {
        val yValues = toolable?.dataForFrequencyAnalysis
        if (yValues != null) {
            compute(yValues)
        } else {
            toolView?.showError("Data is null", "Peak detection")
        }
    }

    override fun onViewClosed() {
        toolable = null
        // No further cleanup necessary
    }

    override fun update(value: Double) {
        // Not needed here...
    }

    override fun updateParameter(width: Int, threshold: Double, decayRate: Double, isRelative: Boolean) {
        this.width = width
        this.threshold = threshold
        this.decayRate = decayRate
        this.isRelative = isRelative
    }

    @EmgComponentInputPort(DoubleArray::class)
    fun compute(yValues: DoubleArray) {
        // If current data pointer not available, then point to 0
        val xStart = (toolable?.currentDataPointer ?: yValues.size) - yValues.size
        PeakDetector.detectPeakLocations(yValues, width, threshold, decayRate, isRelative).subscribe({ peaks ->

            val yValuesPeaks = kotlin.DoubleArray(peaks.size) { yValues[peaks[it]] }
            val xValuesPeaks = peaks.map { it.toDouble() + xStart }.toDoubleArray()
            val xValues = kotlin.DoubleArray(yValues.size) { it.toDouble() + xStart }

            // Add xStart to the xValues the stay in sync with the received data in the main window
            toolView?.showPlotData(xValues, yValues, xValuesPeaks, yValuesPeaks)
            // Additionally show the peaks in a ListView
            toolView?.showPeaksDetail(xValuesPeaks.mapIndexed { idx, it -> Peak(it, yValuesPeaks[idx]) })
        }, {
            toolView?.showError(it.localizedMessage, it.javaClass.simpleName)
        })
    }

}
