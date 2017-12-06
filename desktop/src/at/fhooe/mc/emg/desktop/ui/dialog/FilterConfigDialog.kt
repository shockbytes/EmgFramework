package at.fhooe.mc.emg.desktop.ui.dialog

import at.fhooe.mc.emg.core.util.config.EmgConfig

import javax.swing.*
import javax.swing.border.CompoundBorder
import javax.swing.border.EmptyBorder
import javax.swing.border.TitledBorder
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import java.awt.*

class FilterConfigDialog(private val config: EmgConfig, mainWindow: JFrame) : JDialog(), ChangeListener {

    private val sliderRunningAverage: JSlider
    private val labelRunningAverage: JLabel
    private val sliderSavitzkyGolay: JSlider
    private val labelSavitzkyGolay: JLabel

    init {
        isResizable = false
        title = "Filter configuration"
        setIconImage(Toolkit.getDefaultToolkit()
                .getImage(System.getProperty("user.dir") + "/desktop/icons/ic_dialog_filter_config.png"))

        val width = 345
        val height = 245
        setBounds(mainWindow.x + mainWindow.width / 2 - width / 2,
                mainWindow.y + mainWindow.height / 2 - height / 2,
                width,
                height)
        val contentPanel = JPanel()
        contentPanel.layout = BoxLayout(contentPanel, BoxLayout.PAGE_AXIS)
        contentPanel.border = EmptyBorder(16, 16, 16, 16)
        contentPane = contentPanel

        val panelRunningAverage = JPanel()
        panelRunningAverage.border = CompoundBorder(TitledBorder(UIManager.getBorder("TitledBorder.border"),
                "Running average window size", TitledBorder.LEADING, TitledBorder.TOP,
                null, Color(0, 0, 0)),
                EmptyBorder(16, 16, 16, 16))
        contentPane.add(panelRunningAverage)
        panelRunningAverage.layout = FlowLayout(FlowLayout.CENTER, 5, 5)

        sliderRunningAverage = JSlider()
        sliderRunningAverage.minimum = 10
        sliderRunningAverage.maximum = 70
        sliderRunningAverage.value = config.runningAverageWindowSize
        sliderRunningAverage.addChangeListener(this)
        panelRunningAverage.add(sliderRunningAverage)

        labelRunningAverage = JLabel()
        labelRunningAverage.text = config.runningAverageWindowSize.toString()
        panelRunningAverage.add(labelRunningAverage)

        val panelSavitzkyGolay = JPanel()
        panelSavitzkyGolay.border = CompoundBorder(TitledBorder(UIManager.getBorder("TitledBorder.border"),
                "Savitzky Golay Filter width", TitledBorder.LEADING, TitledBorder.TOP,
                null, Color(0, 0, 0)), EmptyBorder(16, 16, 16, 16))
        contentPanel.add(panelSavitzkyGolay)
        panelSavitzkyGolay.layout = FlowLayout(FlowLayout.CENTER, 5, 5)

        sliderSavitzkyGolay = JSlider()
        sliderSavitzkyGolay.minimum = 1
        sliderSavitzkyGolay.maximum = 40
        sliderSavitzkyGolay.value = config.savitzkyGolayFilterWidth
        sliderSavitzkyGolay.addChangeListener(this)
        panelSavitzkyGolay.add(sliderSavitzkyGolay)

        labelSavitzkyGolay = JLabel()
        labelSavitzkyGolay.text = config.savitzkyGolayFilterWidth.toString()
        panelSavitzkyGolay.add(labelSavitzkyGolay)

        val labelInfo = JLabel("Changing filter parameters will need a restart to become active")
        labelInfo.alignmentX = Component.CENTER_ALIGNMENT
        labelInfo.horizontalAlignment = SwingConstants.CENTER
        contentPanel.add(labelInfo)
    }

    override fun stateChanged(event: ChangeEvent) {

        if (event.source === sliderRunningAverage) {
            labelRunningAverage.text = sliderRunningAverage.value.toString()
            config.runningAverageWindowSize = sliderRunningAverage.value
        } else if (event.source === sliderSavitzkyGolay) {
            labelSavitzkyGolay.text = sliderSavitzkyGolay.value.toString()
            config.savitzkyGolayFilterWidth = sliderSavitzkyGolay.value
        }
    }

}
