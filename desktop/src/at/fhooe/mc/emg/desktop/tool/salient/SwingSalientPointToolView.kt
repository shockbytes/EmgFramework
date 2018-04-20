package at.fhooe.mc.emg.desktop.tool.salient

import at.fhooe.mc.emg.core.tool.salient.SalientPoint
import at.fhooe.mc.emg.core.tool.salient.SalientPointToolView
import at.fhooe.mc.emg.core.tool.salient.SalientPointToolViewCallback
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import org.knowm.xchart.XChartPanel
import org.knowm.xchart.XYChart
import org.knowm.xchart.XYChartBuilder
import org.knowm.xchart.style.Styler
import java.awt.BorderLayout
import java.awt.Color
import java.awt.GridLayout
import java.awt.Toolkit
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.util.concurrent.TimeUnit
import javax.swing.*
import javax.swing.border.EmptyBorder

class SwingSalientPointToolView : SalientPointToolView {

    private var toolViewCallback: SalientPointToolViewCallback? = null

    private lateinit var contentPanel: JPanel
    private lateinit var chart: XYChart
    private lateinit var chartWrapper: XChartPanel<XYChart>
    private lateinit var textAreaPointInfo: JTextArea


    override fun setup(toolViewCallback: SalientPointToolViewCallback, showViewImmediate: Boolean) {
        this.toolViewCallback = toolViewCallback

        if (showViewImmediate) {
            showView()
        }
    }

    override fun showView() {
        val frame = wrap()
        frame.addWindowListener(object : WindowAdapter() {
            override fun windowClosed(e: WindowEvent?) {
                super.windowClosed(e)
                toolViewCallback?.onViewClosed()
                disposable?.dispose()
            }
        })
        frame.isVisible = true
    }

    override fun updateChart(xVals: List<Double>, yVals: List<Double>) {

        if (chart.seriesMap.isEmpty()) {
            chart.addSeries("Data", xVals, yVals)
        } else {
            chart.updateXYSeries("Data", xVals, yVals, null)
        }
        chartWrapper.repaint()
    }

    override fun drawSalientPoint(point: SalientPoint) {

        textAreaPointInfo.text = "$point"

        val yData = chart.seriesMap["Data"]?.yData ?: return
        val xSeries: List<Double> = listOf(0.0, point.x.toDouble(), yData.size.toDouble() - 1)
        val ySeries: List<Double> = listOf(yData.first(), point.y, yData.last())

        if (chart.seriesMap.size == 1) {
            chart.addSeries("Salient Point", xSeries, ySeries)
        } else if (chart.seriesMap.size > 1) {
            chart.updateXYSeries("Salient Point", xSeries, ySeries, null)
        }

        chartWrapper.repaint()
    }

    override fun clearSalientPoint() {
        chart.removeSeries("Salient Point")
        chartWrapper.repaint()
    }

    private fun wrap(): JFrame {
        val frame = JFrame()
        frame.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
        frame.title = "Salient Point Detection"
        frame.iconImage = Toolkit.getDefaultToolkit()
                .getImage(System.getProperty("user.dir") + "/desktop/icons/ic_tool_salient_point.png")
        frame.setBounds(650, 100, 600, 400)

        contentPanel = JPanel(BorderLayout())
        contentPanel.add(chart(), BorderLayout.CENTER)
        contentPanel.add(parameterPanel(), BorderLayout.EAST)

        // TODO Just Testing
        val btn = JButton("Testing")
        btn.addActionListener {
            startTesting()
        }
        contentPanel.add(btn, BorderLayout.SOUTH)

        frame.contentPane = contentPanel
        return frame
    }

    private fun chart(): JComponent {

        chart = XYChartBuilder().width(150).height(200).theme(Styler.ChartTheme.GGPlot2).build()
        chart.styler.isLegendVisible = false
        chart.styler.isPlotGridLinesVisible = false
        chart.styler.plotBackgroundColor = Color.WHITE
        chart.styler.isLegendVisible = true
        chart.styler.legendPosition = Styler.LegendPosition.InsideNW
        chart.styler.seriesColors = arrayOf(Color.decode("#F44336"), Color.decode("#424242"))

        chartWrapper = XChartPanel(chart)
        return chartWrapper
    }

    private fun parameterPanel(): JPanel {

        val panel = JPanel(GridLayout(6, 1, 4, 4))
        panel.border = EmptyBorder(4, 4, 4, 4)

        val tfAngle = JTextField("15")
        val tfConfidence = JTextField("0.5")
        val btnApply = JButton("Apply")
        btnApply.addActionListener {
            val angle = tfAngle.text.toIntOrNull()
            val confidence = tfConfidence.text.toDoubleOrNull()
            if (angle != null && (angle in 5..90) && confidence != null && (confidence in 0..1)) {
                toolViewCallback?.updateParameter(confidence, angle)
            }
        }
        textAreaPointInfo = JTextArea()
        textAreaPointInfo.isEditable = false

        panel.add(JLabel("Angle threshold (5 - 90°)"))
        panel.add(tfAngle)
        panel.add(JLabel("Confidence threshold (0-1)"))
        panel.add(tfConfidence)
        panel.add(btnApply)
        panel.add(textAreaPointInfo)

        return panel
    }

    var disposable: Disposable? = null
    private val testData: List<Double> = listOf(3.0, 2.4, 3.5, 6.0, 6.2, 5.8, 6.5, 14.0, 15.0, 18.0, 17.0, 27.0, 30.0)
    private fun startTesting() {

        disposable = Observable.interval(1, TimeUnit.SECONDS).subscribe {

            val idx = it.toInt()
            if (idx < testData.size) {
                toolViewCallback?.update(testData[idx])
            } else {
                disposable?.dispose()
            }
        }
    }

}