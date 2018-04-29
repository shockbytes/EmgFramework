package at.fhooe.mc.emg.desktop.tool.salient

import at.fhooe.mc.emg.core.tool.salient.SalientPoint
import at.fhooe.mc.emg.core.tool.salient.SalientPointToolView
import at.fhooe.mc.emg.core.tool.salient.SalientPointToolViewCallback
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

    override fun updateSalientPointInformation(point: SalientPoint) {
        textAreaPointInfo.text = "$point"
    }

    override fun drawSalientPoint(point: SalientPoint) {

        val yData = chart.seriesMap["Data"]?.yData ?: return
        val xSeries: List<Double> = listOf(0.0, point.x.toDouble(), yData.size.toDouble() - 1)
        val ySeries: List<Double> = listOf(yData.first(), point.y, yData.last())

        synchronized(this) {
            if (chart.seriesMap.size == 1) {
                chart.addSeries("Salient Point", xSeries, ySeries)
            } else if (chart.seriesMap.size > 1) {
                chart.updateXYSeries("Salient Point", xSeries, ySeries, null)
            }
            chartWrapper.repaint()
        }
    }

    override fun clearSalientPoint() {
        synchronized(this) {
            chart.removeSeries("Salient Point")
            chartWrapper.repaint()
        }
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

        val tfAngle = JTextField("0")
        val tfConfidence = JTextField("50.0")
        val btnApply = JButton("Apply")
        btnApply.addActionListener {
            val angle = tfAngle.text.toIntOrNull()
            val confidence = tfConfidence.text.toDoubleOrNull()
            if (angle != null && (angle in 0..360) && confidence != null) {
                toolViewCallback?.updateParameter(confidence, angle)
            }
        }
        textAreaPointInfo = JTextArea()
        textAreaPointInfo.isEditable = false

        panel.add(JLabel("Angle threshold (in degree)"))
        panel.add(tfAngle)
        panel.add(JLabel("Confidence threshold"))
        panel.add(tfConfidence)
        panel.add(btnApply)
        panel.add(textAreaPointInfo)

        return panel
    }

}