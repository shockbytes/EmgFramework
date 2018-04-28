package at.fhooe.mc.emg.desktop.view

import at.fhooe.mc.emg.designer.WorkflowViewCallback
import at.fhooe.mc.emg.designer.view.WorkflowView
import java.awt.GridLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.event.WindowEvent.WINDOW_CLOSING
import javax.swing.*
import javax.swing.border.EmptyBorder


class DesktopWorkflowView : WorkflowView {

    private var frame: JFrame? = null
    private var labelStatus: JLabel? = null

    override fun startView(callback: WorkflowViewCallback) {

        frame = JFrame("Workflow controls")
        frame?.addWindowListener(object: WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                super.windowClosing(e)
                callback.tearDown()
            }
        })

        frame?.setBounds(400, 400, 300, 300)
        frame?.defaultCloseOperation = WindowConstants.HIDE_ON_CLOSE

        val panel = JPanel(GridLayout(4, 1, 4, 4))
        panel.border = EmptyBorder(4, 4, 4, 4)

        val btnStart = JButton("Start")
        btnStart.addActionListener { callback.start() }
        panel.add(btnStart)

        val btnStop = JButton("Stop")
        btnStop.addActionListener { callback.stop() }
        panel.add(btnStop)

        val btnClose = JButton("Close")
        btnClose.addActionListener { callback.close() }
        panel.add(btnClose)

        labelStatus = JLabel("")
        panel.add(labelStatus)

        frame?.contentPane = panel
        frame?.isVisible = true
    }

    override fun close() {
        frame?.dispatchEvent(WindowEvent(frame, WINDOW_CLOSING))
    }

    override fun onStart() {
        labelStatus?.text = "Workflow started"
    }

    override fun onStop() {
        labelStatus?.text = "Workflow stopped"
    }
}