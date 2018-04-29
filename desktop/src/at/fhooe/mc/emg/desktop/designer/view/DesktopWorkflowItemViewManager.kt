package at.fhooe.mc.emg.desktop.designer.view

import at.fhooe.mc.emg.designer.model.Workflow
import at.fhooe.mc.emg.designer.view.WorkflowItemViewManager
import java.awt.event.WindowEvent
import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.WindowConstants

class DesktopWorkflowItemViewManager : WorkflowItemViewManager {

    private val frameList = mutableListOf<JFrame>()

    private var xOffset = 100
    private val defaultWidth = 300
    private val defaultHeight = 300

    override fun startProducer(item: Workflow.StartableProducer) {
        // TODO
        println("Start producer view ${item.instance.javaClass.simpleName}")
    }

    override fun showPlatformView(item: Workflow.ViewableConsumer) {
        val (view, requestedWidth) = item.view
        val platformView = view.get(item.instance) as? JComponent
        if (platformView != null) {

            val width = if (requestedWidth > 0) requestedWidth else defaultWidth
            val frame = wrapPlatformViewInJFrame(item.instance.javaClass.simpleName, platformView, width)
            frame.isVisible = true

            frameList.add(frame)
            xOffset += width
        }
    }

    override fun releaseViews() {
        frameList.forEach { frame -> frame.dispatchEvent(WindowEvent(frame, WindowEvent.WINDOW_CLOSING)) }
        frameList.clear()
        xOffset = 100
    }


    private fun wrapPlatformViewInJFrame(title: String, platformView: JComponent, width: Int): JFrame {
        val frame = JFrame()
        frame.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
        frame.title = title
        frame.setBounds(xOffset, 75, width, defaultHeight)
        frame.add(platformView)
        return frame
    }
}