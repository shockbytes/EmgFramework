package at.fhooe.mc.emg.desktop.ui.dialog

import at.fhooe.mc.emg.core.util.EmgConfig
import java.awt.Color
import java.awt.Component
import java.awt.FlowLayout
import java.awt.Toolkit
import javax.swing.*
import javax.swing.border.CompoundBorder
import javax.swing.border.EmptyBorder
import javax.swing.border.TitledBorder

class FilterConfigDialog(private val config: EmgConfig, mainWindow: JFrame) : JDialog() {


    init {
        isResizable = false
        title = "Filter configuration"
        setIconImage(Toolkit.getDefaultToolkit()
                .getImage(System.getProperty("user.dir") + "/desktop/icons/ic_dialog_filter_config.png"))

        val width = 345
        val height = 390
        setBounds(mainWindow.x + mainWindow.width / 2 - width / 2,
                mainWindow.y + mainWindow.height / 2 - height / 2,
                width, height)
        val contentPanel = JPanel()
        contentPanel.layout = BoxLayout(contentPanel, BoxLayout.PAGE_AXIS)
        contentPanel.border = EmptyBorder(16, 16, 16, 16)
        contentPane = contentPanel

        contentPanel.add(addConfigurableFilterComponent("RAVG", "Running average window size",
                10, 70, config.runningAverageWindowSize))
        contentPanel.add(addConfigurableFilterComponent("SG", "Savitzky Golay filter width",
                1, 40, config.savitzkyGolayFilterWidth))
        contentPanel.add(addConfigurableFilterComponent("TH", "Threshold filter value",
                1, 2048, config.thresholdFilterValue))

        val labelInfo = JLabel("Changing filter parameters will need a restart to become active")
        labelInfo.alignmentX = Component.CENTER_ALIGNMENT
        labelInfo.horizontalAlignment = SwingConstants.CENTER
        contentPanel.add(labelInfo)
    }

    private fun addConfigurableFilterComponent(name: String, title: String,
                                               min: Int, max: Int, initValue: Int): JComponent {

        val panel = JPanel()
        panel.border = CompoundBorder(TitledBorder(UIManager.getBorder("TitledBorder.border"),
                title, TitledBorder.LEADING, TitledBorder.TOP,
                null, Color(0, 0, 0)), EmptyBorder(16, 16, 16, 16))
        panel.layout = FlowLayout(FlowLayout.CENTER, 5, 5)

        val slider = JSlider()
        val label = JLabel()

        slider.minimum = min
        slider.maximum = max
        slider.value = initValue
        slider.addChangeListener {
            label.text = slider.value.toString()

            when(name) {
                "SG" -> config.savitzkyGolayFilterWidth = slider.value
                "RAVG" -> config.runningAverageWindowSize = slider.value
                "TH" -> config.thresholdFilterValue = slider.value
            }
        }
        panel.add(slider)

        label.text = initValue.toString()
        panel.add(label)

        return panel
    }

}
