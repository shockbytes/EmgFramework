package at.fhooe.mc.emg.core.tool.peaks


import at.fhooe.mc.emg.core.Toolable
import at.fhooe.mc.emg.core.tool.Tool
import at.fhooe.mc.emg.designer.EmgComponentType
import at.fhooe.mc.emg.designer.annotation.*
import io.reactivex.subjects.PublishSubject

/**
 * Author:  Martin Macheiner
 * Date:    04.07.2017
 */

@EmgComponent(type = EmgComponentType.RELAY_SINK, displayTitle = "Peak detector")
class PeakDetectionTool(override var toolView: PeakDetectionToolView? = null) : Tool, PeakDetectionToolViewCallback {

    override val name = "Peak Detection"

    private var toolable: Toolable? = null

    @JvmField
    @EmgComponentProperty(PeakDetector.defaultWidth.toString(), "Width")
    var width: Int = PeakDetector.defaultWidth

    @JvmField
    @EmgComponentProperty(PeakDetector.defaultThreshold.toString(), "Threshold")
    var threshold: Double = PeakDetector.defaultThreshold

    @JvmField
    @EmgComponentProperty(PeakDetector.defaultDecayRate.toString(), "Decay rate [0, 1]")
    var decayRate: Double = PeakDetector.defaultDecayRate

    @JvmField
    @EmgComponentProperty(PeakDetector.defaultIsRelative.toString(), "Use relative peaks")
    var isRelative: Boolean = PeakDetector.defaultIsRelative

    @JvmField
    @EmgComponentOutputPort(DoubleArray::class)
    var outputPort: PublishSubject<DoubleArray> = PublishSubject.create()

    override fun start(toolable: Toolable?, showViewImmediate: Boolean) {
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

    @EmgComponentStartablePoint("toolView", PeakDetectionToolView::class)
    override fun externalStart() {
        start(null, true)
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

            outputPort.onNext(xValuesPeaks)

        }, {
            toolView?.showError(it.localizedMessage, it.javaClass.simpleName)
        })
    }

}
