package at.fhooe.mc.emg.desktop.ui

import at.fhooe.mc.emg.core.analysis.FrequencyAnalysisMethod
import at.fhooe.mc.emg.core.analysis.FrequencyAnalysisView
import org.knowm.xchart.CategoryChart
import org.knowm.xchart.CategoryChartBuilder
import org.knowm.xchart.CategorySeries.CategorySeriesRenderStyle
import org.knowm.xchart.XChartPanel
import org.knowm.xchart.style.Styler.*
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Rectangle
import java.awt.Toolkit
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class FrequencyAnalysisFrame : JFrame(), FrequencyAnalysisView {

    private val contentPane: JPanel
    private var chart: CategoryChart? = null

    init {

        defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
        iconImage = Toolkit.getDefaultToolkit()
                .getImage(System.getProperty("user.dir") + "/desktop/icons/ic_analysis.png")
        title = "Frequency analysis"
        bounds = Rectangle(400, 200, 450, 300)

        contentPane = JPanel()
        contentPane.border = EmptyBorder(8, 8, 8, 8)
        contentPane.background = Color.WHITE
        contentPane.layout = BorderLayout(0, 0)
        setContentPane(contentPane)

        initializeChart()
        isVisible = true
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

    override fun showEvaluation(method: FrequencyAnalysisMethod.Method, xData: DoubleArray, yData: DoubleArray) {

        val name: String
        val color: Color
        when(method) {

            FrequencyAnalysisMethod.Method.FFT -> {
                name = "FFT"
                color = Color.decode("#0091EA")
            }
            FrequencyAnalysisMethod.Method.SPECTRUM -> {
                name = "Power spectrum"
                color = Color.decode("#8BC34A")
            }
        }

        chart?.addSeries(name, xData, yData)
                ?.setMarkerColor(color)?.lineColor = color
    }

}
