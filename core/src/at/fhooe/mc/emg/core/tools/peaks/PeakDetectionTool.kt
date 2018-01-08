package at.fhooe.mc.emg.core.tools.peaks

import at.fhooe.mc.emg.core.EmgController
import at.fhooe.mc.emg.core.tools.Tool
import at.fhooe.mc.emg.core.util.PeakDetector

/**
 * Author:  Mescht
 * Date:    04.07.2017
 */
class PeakDetectionTool(override var view: PeakDetectionView? = null) : Tool {

    override val name = "Peak detection"

    override fun start(controller: EmgController, showViewImmediate: Boolean) {
        view?.setup(Unit, showViewImmediate)
        calculatePeaks(controller.visualView.dataForFrequencyAnalysis)
    }

    private fun calculatePeaks(yValues: DoubleArray) {
        PeakDetector.detectPeakLocations(yValues).subscribe { peaks ->
            view?.showData(IntArray(yValues.size){i -> i}, yValues, peaks)
        }
    }

}
