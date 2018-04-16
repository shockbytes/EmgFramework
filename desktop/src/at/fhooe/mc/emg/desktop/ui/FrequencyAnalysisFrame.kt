package at.fhooe.mc.emg.desktop.ui

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

    private val contentPanel: JPanel
    private var chart: CategoryChart

    init {

        defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
        iconImage = Toolkit.getDefaultToolkit()
                .getImage(System.getProperty("user.dir") + "/desktop/icons/ic_analysis.png")
        title = "Frequency analysis"
        bounds = Rectangle(400, 200, 450, 300)

        contentPanel = JPanel()
        contentPanel.border = EmptyBorder(8, 8, 8, 8)
        contentPanel.background = Color.WHITE
        contentPanel.layout = BorderLayout(0, 0)

        chart = CategoryChartBuilder().width(800).height(600).theme(ChartTheme.GGPlot2).build()
        initializeChart()

        contentPane = contentPanel
        isVisible = true
    }

    private fun initializeChart() {

        chart.styler.legendPosition = LegendPosition.InsideN
        chart.styler.defaultSeriesRenderStyle = CategorySeriesRenderStyle.Stick
        chart.styler.xAxisLabelRotation = 90
        chart.styler.isToolTipsEnabled = true
        chart.styler.toolTipType = ToolTipType.xAndYLabels
        chart.styler.isXAxisTicksVisible = false
        chart.styler.isPlotGridLinesVisible = false
        chart.styler.plotBackgroundColor = Color.WHITE
        chart.styler.chartBackgroundColor = Color.WHITE
        chart.styler.xAxisDecimalPattern = "#0.00"
    }

    override fun showEvaluation(method: String, xData: DoubleArray, yData: DoubleArray) {

        title = method

        val color: Color = when (method) {
            "FFT" -> Color.decode("#0091EA")
            "Power Spectrum" -> Color.decode("#8BC34A")
            else -> Color.decode("#AADDEE")
        }

        chart.addSeries(method, xData, yData).setMarkerColor(color)?.lineColor = color
        contentPane.add(XChartPanel(chart))
        contentPane.revalidate()
    }

    override fun showError(error: Throwable) {
        val msg = "${error.javaClass.simpleName}: ${error.localizedMessage}"
        UiUtils.showErrorMessage(this, msg, "Frequency Analysis error")

        dispose()
    }


}
