package at.fhooe.mc.emg.desktop.tool.salient

import at.fhooe.mc.emg.core.tool.salient.SalientPointToolView
import at.fhooe.mc.emg.core.tool.salient.SalientPointToolViewCallback
import java.awt.BorderLayout
import java.awt.Toolkit
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.WindowConstants

class SwingSalientPointToolView: SalientPointToolView {

    private var toolViewCallback: SalientPointToolViewCallback? = null

    private lateinit var contentPanel: JPanel


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

    private fun wrap(): JFrame {
        val frame = JFrame()
        frame.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
        frame.title = "Salient Point Detection"
        frame.iconImage = Toolkit.getDefaultToolkit()
                .getImage(System.getProperty("user.dir") + "/desktop/icons/ic_tool_salient_point.png")
        frame.setBounds(650, 100, 600, 400)

        contentPanel = JPanel(BorderLayout())

        frame.contentPane = contentPanel
        return frame
    }

}