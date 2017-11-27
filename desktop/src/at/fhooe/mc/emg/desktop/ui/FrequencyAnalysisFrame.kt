package at.fhooe.mc.emg.desktop.ui

import at.fhooe.mc.emg.util.FrequencyAnalysis
import at.fhooe.mc.emg.util.FrequencyAnalysis.AnalysisType
import org.knowm.xchart.CategoryChart
import org.knowm.xchart.CategoryChartBuilder
import org.knowm.xchart.CategorySeries.CategorySeriesRenderStyle
import org.knowm.xchart.XChartPanel
import org.knowm.xchart.style.Styler.*
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Toolkit
import java.util.*
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

// TODO Decouple this with View later
internal class FrequencyAnalysisFrame private constructor(type: AnalysisType, input: DoubleArray,
                                                          sampleFrequency: Double, parent: JFrame) : JFrame() {

    private val contentPane: JPanel
    private var chart: CategoryChart? = null

    init {

        defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
        iconImage = Toolkit.getDefaultToolkit().getImage(System.getProperty("user.dir") + "/icons/ic_analysis.png")
        title = "Frequency analysis"
        setBoundsRelativeToParent(parent)

        contentPane = JPanel()
        contentPane.border = EmptyBorder(8, 8, 8, 8)
        contentPane.background = Color.WHITE
        contentPane.layout = BorderLayout(0, 0)
        setContentPane(contentPane)

        initializeChart()
        doCalculation(type, input, sampleFrequency)
    }

    private fun setBoundsRelativeToParent(parent: JFrame) {
        val b = parent.bounds
        setBounds(b.x + b.width + 10, b.y - b.height / 2 + 30, 450, 300)
    }

    private fun initializeChart() {

        chart = CategoryChartBuilder().width(800).height(600).theme(ChartTheme.GGPlot2).build()
        chart?.styler?.legendPosition = LegendPosition.InsideN
        chart?.styler?.defaultSeriesRenderStyle = CategorySeriesRenderStyle.Stick
        chart?.styler?.xAxisLabelRotation = 90
        chart?.styler?.isToolTipsEnabled = true
        chart?.styler?.toolTipType = ToolTipType.xAndYLabels
        chart?.styler?.isXAxisTicksVisible = false
        chart?.styler?.isPlotGridLinesVisible = false
        chart?.styler?.plotBackgroundColor = Color.WHITE
        chart?.styler?.chartBackgroundColor = Color.WHITE
        chart?.styler?.xAxisDecimalPattern = "#0.00"

        val chartWrapper = XChartPanel(chart)
        contentPane.add(chartWrapper)
    }

    private fun doCalculation(type: AnalysisType, input: DoubleArray, fs: Double) {

        FrequencyAnalysis.fft(input).subscribe { fft ->
            when (type) {
                FrequencyAnalysis.AnalysisType.FFT -> showFFTPlot(fft)
                FrequencyAnalysis.AnalysisType.SPECTRUM -> showPowerSpectrumPlot(fft, fs)
            }

        }
    }

    private fun showFFTPlot(fft: DoubleArray) {

        val xData = DoubleArray(fft.size)
        Arrays.setAll(xData) { i -> i.toDouble() }

        val c = Color.decode("#0091EA")
        chart?.addSeries("FFT", xData, fft)
                ?.setMarkerColor(c)?.lineColor = c
    }

    private fun showPowerSpectrumPlot(fft: DoubleArray, fs: Double) {
        FrequencyAnalysis.powerSpectrum(fft, fs).subscribe { data ->
            val c = Color.decode("#8BC34A")
            chart?.addSeries("Power spectrum", data.first, data.second)
                    ?.setMarkerColor(c)?.lineColor = c
        }
    }

    companion object {

        fun show(type: AnalysisType, input: DoubleArray,
                 sampleFrequency: Double, parent: JFrame) {
            val frame = FrequencyAnalysisFrame(type, input, sampleFrequency, parent)
            frame.isVisible = true
        }
    }

}
