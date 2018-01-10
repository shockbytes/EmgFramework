package at.fhooe.mc.emg.core.tools.peaks

import at.fhooe.mc.emg.core.EmgPresenter
import at.fhooe.mc.emg.core.tools.Tool
import at.fhooe.mc.emg.core.util.PeakDetector

/**
 * Author:  Mescht
 * Date:    04.07.2017
 */
class PeakDetectionTool(override var view: PeakDetectionView? = null) : Tool {

    override val name = "Peak detection"

    override fun start(presenter: EmgPresenter, showViewImmediate: Boolean) {
        view?.setup(Unit, showViewImmediate)
        calculatePeaks(presenter.visualView.dataForFrequencyAnalysis)
    }

    private fun calculatePeaks(yValues: DoubleArray) {
        PeakDetector.detectPeakLocations(yValues).subscribe { peaks ->
            view?.showData(IntArray(yValues.size){i -> i}, yValues, peaks)
        }
    }

}
