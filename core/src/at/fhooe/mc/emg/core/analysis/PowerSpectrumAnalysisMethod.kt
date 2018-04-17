package at.fhooe.mc.emg.core.analysis

import at.fhooe.mc.emg.designer.EmgComponent
import at.fhooe.mc.emg.designer.EmgComponentType
import io.reactivex.Single

@EmgComponent(type = EmgComponentType.SINK)
class PowerSpectrumAnalysisMethod: FrequencyAnalysisMethod {

    override val name = "Power Spectrum"

    override val hasDisplay = true

    override fun calculate(input: DoubleArray, fs: Double, view: FrequencyAnalysisView?): Single<Double> {
        AnalysisUtils.fft(input).subscribe({ fft -> showPowerSpectrumPlot(fft, fs, view) })
        return Single.just(Double.MIN_VALUE)
    }

    private fun showPowerSpectrumPlot(fft: DoubleArray, fs: Double, view: FrequencyAnalysisView?) {
        AnalysisUtils.powerSpectrum(fft, fs).subscribe({ data ->
            view?.showEvaluation("Power Spectrum", data.first, data.second)
        }, { throwable -> view?.showError(throwable) })
    }
}