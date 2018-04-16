package at.fhooe.mc.emg.core.analysis

import at.fhooe.mc.emg.core.EmgComponent
import at.fhooe.mc.emg.core.EmgComponentType
import io.reactivex.Single

@EmgComponent(type = EmgComponentType.SINK)
class FftFrequencyAnalysisMethod: FrequencyAnalysisMethod {

    override val name = "FFT"

    override val hasDisplay = true


    override fun calculate(input: DoubleArray, fs: Double, view: FrequencyAnalysisView?): Single<Double> {
        AnalysisUtils.fft(input).subscribe({ fft -> showFFTPlot(fft, view) })
        return Single.just(Double.MIN_VALUE)
    }

    private fun showFFTPlot(fft: DoubleArray, view: FrequencyAnalysisView?) {
        val xData = DoubleArray(fft.size) { i -> i.toDouble() }
        view?.showEvaluation("FFT", xData, fft)
    }

}