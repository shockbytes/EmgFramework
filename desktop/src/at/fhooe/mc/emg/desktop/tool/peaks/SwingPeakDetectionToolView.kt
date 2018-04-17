package at.fhooe.mc.emg.desktop.tool.peaks

import at.fhooe.mc.emg.core.tool.peaks.Peak
import at.fhooe.mc.emg.core.tool.peaks.PeakDetectionToolView
import at.fhooe.mc.emg.core.tool.peaks.PeakDetectionToolViewCallback
import at.fhooe.mc.emg.core.tool.peaks.PeakDetector
import at.fhooe.mc.emg.desktop.util.chart.PeakChartMarker
import org.knowm.xchart.XChartPanel
import org.knowm.xchart.XYChart
import org.knowm.xchart.XYChartBuilder
import org.knowm.xchart.XYSeries
import org.knowm.xchart.style.Styler
import java.awt.*
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*
import javax.swing.border.EmptyBorder

class SwingPeakDetectionToolView : PeakDetectionToolView {

    private var toolViewCallback: PeakDetectionToolViewCallback? = null

    private lateinit var contentPanel: JPanel

    private lateinit var chartPeaks: XYChart
    private lateinit var chartPeaksWrapper: XChartPanel<XYChart>

    // For peaks detail toolView
    private lateinit var list: JList<String>
    private lateinit var labelPeaksFound: JLabel

    private fun wrap(): JFrame {
        val frame = JFrame()
        frame.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
        frame.title = "Peak detection"
        frame.iconImage = Toolkit.getDefaultToolkit()
                .getImage(System.getProperty("user.dir") + "/desktop/icons/ic_tool_peak_detection.png")
        frame.setBounds(650, 100, 600, 400)

        contentPanel = JPanel()
        contentPanel.layout = BorderLayout()

        val configPanel = JPanel()
        configPanel.border = EmptyBorder(8, 8, 8, 8)
        configPanel.layout = GridLayout(10, 1, 4, 4)
        configPanel.add(JLabel("Width between peaks"))
        val tfWidth = JTextField(PeakDetector.defaultWidth.toString())
        configPanel.add(tfWidth)
        configPanel.add(JLabel("Min threshold for peak"))
        val tfThreshold = JTextField(PeakDetector.defaultThreshold.toString())
        configPanel.add(tfThreshold)
        configPanel.add(JLabel("Decay rate"))
        val tfDecayRate = JTextField(PeakDetector.defaultDecayRate.toString())
        configPanel.add(tfDecayRate)
        configPanel.add(JLabel("Is relative to local avg"))
        val cbIsRelative = JCheckBox("Is relative", PeakDetector.defaultIsRelative)
        configPanel.add(cbIsRelative)
        labelPeaksFound = JLabel("")
        labelPeaksFound.horizontalAlignment = JLabel.CENTER
        configPanel.add(labelPeaksFound)
        val btnCompute = JButton("Compute peaks")
        btnCompute.addActionListener { _ -> validate(tfWidth.text, tfThreshold.text, tfDecayRate.text, cbIsRelative.isSelected) }
        configPanel.add(btnCompute)
        contentPanel.add(configPanel, BorderLayout.EAST)

        list = JList()
        list.preferredSize = Dimension(100, 100)
        list.selectionMode = ListSelectionModel.SINGLE_SELECTION
        list.fixedCellWidth = 100

        val scrollPaneList = JScrollPane(list)
        scrollPaneList.border = BorderFactory.createLoweredSoftBevelBorder()
        contentPanel.add(scrollPaneList, BorderLayout.WEST)

        chartPeaks = XYChartBuilder().width(150).height(200).theme(Styler.ChartTheme.GGPlot2).build()
        chartPeaks.styler.isLegendVisible = false
        chartPeaks.styler.isPlotGridLinesVisible = false
        chartPeaks.styler.plotBackgroundColor = Color.WHITE
        chartPeaks.styler.seriesColors = arrayOf(Color.decode("#8BC34A"), Color.decode("#FF9800"))

        chartPeaksWrapper = XChartPanel(chartPeaks)
        contentPanel.add(chartPeaksWrapper, BorderLayout.CENTER)

        chartPeaks.addSeries("Data", doubleArrayOf(0.0), doubleArrayOf(0.0))
        chartPeaks.addSeries("Peaks", doubleArrayOf(0.0), doubleArrayOf(0.0))
                .setXYSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Scatter).marker = PeakChartMarker(12)

        frame.contentPane = contentPanel
        return frame
    }

    private fun validate(strWidth: String, strThreshold: String, strDecayRate: String, isRelative: Boolean) {

        try {
            val width = Integer.parseInt(strWidth)
            val threshold = java.lang.Double.parseDouble(strThreshold)
            val decayRate = java.lang.Double.parseDouble(strDecayRate)
            if (decayRate < 0.0 || decayRate > 1.0) {
                throw NumberFormatException("Decay Rate must be in range [0, 1]")
            }
            toolViewCallback?.compute(width, threshold, decayRate, isRelative)
        } catch (nfe: NumberFormatException) {
            showError(nfe.localizedMessage, nfe.javaClass.simpleName)
        }
    }

    override fun showPlotData(xValues: DoubleArray, yValues: DoubleArray,
                              xValuesPeaks: DoubleArray, yValuesPeaks: DoubleArray) {

        if (xValues.isNotEmpty()) {
            chartPeaks.updateXYSeries("Data", xValues, yValues, null)
        }
        if (xValuesPeaks.isNotEmpty()) {
            chartPeaks.updateXYSeries("Peaks", xValuesPeaks, yValuesPeaks, null)
        }
        chartPeaksWrapper.repaint()
    }

    override fun setup(toolViewCallback: PeakDetectionToolViewCallback, showViewImmediate: Boolean) {
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

    override fun showError(cause: String, title: String) {
        JOptionPane.showMessageDialog(contentPanel, cause, title, JOptionPane.ERROR_MESSAGE)
    }

    override fun showPeaksDetail(peaks: List<Peak>) {
        val data = peaks.map { it.toPrettyString() }.toTypedArray()
        list.setListData(data)

        labelPeaksFound.text = "${peaks.size} peaks detected"
    }
}
