package at.fhooe.mc.emg.desktop.tool.fatigue

import at.fhooe.mc.emg.core.tool.fatigue.MuscleFatigueToolView
import at.fhooe.mc.emg.core.tool.fatigue.MuscleFatigueToolViewCallback
import at.fhooe.mc.emg.core.util.round
import at.fhooe.mc.emg.desktop.misc.DesktopDataLog
import at.fhooe.mc.emg.desktop.util.UiUtils
import org.knowm.xchart.XChartPanel
import org.knowm.xchart.XYChart
import org.knowm.xchart.XYChartBuilder
import org.knowm.xchart.style.Styler
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Toolkit
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*
import javax.swing.border.EmptyBorder

class SwingMuscleFatigueToolView : MuscleFatigueToolView {

    private var viewCallback: MuscleFatigueToolViewCallback? = null

    private lateinit var mainPanel: JPanel
    private lateinit var chart: XYChart
    private lateinit var chartWrapper: XChartPanel<XYChart>
    private lateinit var labelStatus: JLabel

    private val dataLog: DesktopDataLog = DesktopDataLog()

    private fun wrap(): JFrame {
        val frame = JFrame()
        frame.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
        frame.title = "Muscle Fatigue detection (Joint Analysis of EMG Spectrum and Amplitude)"
        frame.setBounds(650, 100, 600, 400)
        frame.iconImage = Toolkit.getDefaultToolkit()
                .getImage(System.getProperty("user.dir") + "/desktop/icons/ic_tool_muscle_fatigue.png")

        mainPanel = JPanel(BorderLayout())
        mainPanel.background = Color.WHITE
        mainPanel.add(chart(), BorderLayout.CENTER)
        val logView = dataLog.view
        logView.preferredSize = Dimension(200,300)
        mainPanel.add(logView, BorderLayout.EAST)
        labelStatus = JLabel("Status: Working")
        labelStatus.border = EmptyBorder(8, 8, 8, 8)
        mainPanel.add(labelStatus, BorderLayout.SOUTH)
        frame.contentPane = mainPanel

        val menuBar = JMenuBar()
        val mnFile = JMenu("File")
        val mnItemSave = JMenuItem("Save")
        mnItemSave.addActionListener {
            val fileName = UiUtils.showCsvSaveDialog()
            if (fileName != null) {
                viewCallback?.save(fileName)
            }
        }
        mnFile.add(mnItemSave)

        val mnItemOpen = JMenuItem("Open")
        mnItemOpen.addActionListener {
            val fileName = UiUtils.showCsvOpenDialog()
            if (fileName != null) {
                viewCallback?.save(fileName)
            }
        }
        mnFile.add(mnItemOpen)

        menuBar.add(mnFile)
        frame.jMenuBar = menuBar

        return frame
    }

    override fun setup(toolViewCallback: MuscleFatigueToolViewCallback, showViewImmediate: Boolean) {
        this.viewCallback = toolViewCallback

        if (showViewImmediate) {
            showView()
        }
    }

    override fun showView() {
        val frame = wrap()
        frame.addWindowListener(object : WindowAdapter() {
            override fun windowClosed(e: WindowEvent?) {
                super.windowClosed(e)
                viewCallback?.onViewClosed()
            }
        })
        frame.isVisible = true
    }

    override fun update(values: List<Pair<Double, Double>>) {

        // RMS values as x values, median frequency as y values
        updateChart(values.map { it.second.round().toDouble() }, values.map { it.first })

        // Always update full data log
        dataLog.update(values.last().let { "MF: ${it.first}\nRMS: ${it.second}" })
        dataLog.view.invalidate()
    }

    override fun showError(s: String) {
        labelStatus.text = s
    }

    override fun clear() {
        dataLog.clear()

        if (chart.seriesMap.isNotEmpty()) {
            chart.removeSeries("Data")
        }
    }

    private fun chart(): JComponent {

        chart = XYChartBuilder().width(150).height(200).theme(Styler.ChartTheme.GGPlot2).build()
        chart.styler.isLegendVisible = false
        chart.styler.isPlotGridLinesVisible = false
        chart.styler.plotBackgroundColor = Color.WHITE
        chart.styler.isLegendVisible = false
        chart.styler.seriesColors = arrayOf(Color.decode("#795548"))

        chartWrapper = XChartPanel(chart)
        chartWrapper.border = EmptyBorder(8, 8, 8, 8)
        return chartWrapper
    }

    private fun updateChart(xVals: List<Double>, yVals: List<Double>) {
        if (chart.seriesMap.isEmpty()) {
            chart.addSeries("Data", xVals, yVals)
        } else {
            chart.updateXYSeries("Data", xVals, yVals, null)
        }
        chartWrapper.repaint()
    }
}

