package at.fhooe.mc.emg.core.analysis

import at.fhooe.mc.emg.designer.EmgComponentType
import at.fhooe.mc.emg.designer.annotation.EmgComponent
import at.fhooe.mc.emg.designer.annotation.EmgComponentInputPort

@EmgComponent(type = EmgComponentType.SINK)
class FftFrequencyAnalysisMethod(override var view: FrequencyAnalysisView? = null,
                                 override var fs: Double = 100.0) : FrequencyAnalysisMethod {

    override val name = "FFT"

    override val hasDisplay = true

    @EmgComponentInputPort(DoubleArray::class)
    override fun calculate(input: DoubleArray) {
        AnalysisUtils.fft(input).subscribe({ fft -> showFFTPlot(fft, view) })
    }

    private fun showFFTPlot(fft: DoubleArray, view: FrequencyAnalysisView?) {
        val xData = DoubleArray(fft.size) { i -> i.toDouble() }
        view?.showEvaluation("FFT", xData, fft)
    }

}