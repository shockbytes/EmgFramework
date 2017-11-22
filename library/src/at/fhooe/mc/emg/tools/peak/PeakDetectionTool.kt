package at.fhooe.mc.emg.tools.peak

import at.fhooe.mc.emg.core.EmgController
import at.fhooe.mc.emg.tools.Tool
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.WindowConstants

/**
 * Author:  Mescht
 * Date:    04.07.2017
 */
class PeakDetectionTool : Tool {

    override val name: String
        get() = "Peak detection"

    override fun start(controller: EmgController) {

        val frame = JFrame()
        frame.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
        frame.title = name
        frame.contentPane = JPanel()
        frame.setBounds(600, 200, 400, 300)
        frame.isVisible = true
    }
}
