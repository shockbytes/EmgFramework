package at.fhooe.mc.emg.desktop.designer.view

import at.fhooe.mc.emg.designer.DesignerViewCallback
import at.fhooe.mc.emg.designer.component.EmgBaseComponent
import at.fhooe.mc.emg.designer.component.model.Origin
import at.fhooe.mc.emg.designer.component.model.Point
import at.fhooe.mc.emg.designer.view.ComponentInteractionView
import at.fhooe.mc.emg.desktop.designer.DesktopDesignerHelper
import at.fhooe.mc.emg.desktop.designer.util.DragDropTargetTransferHandler
import at.fhooe.mc.emg.desktop.designer.util.DragDropTransferHandler
import java.awt.Color
import java.awt.Graphics
import java.awt.dnd.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JPanel


class DesktopComponentInteractionView : JPanel(), ComponentInteractionView, DropTargetListener {

    override var interactionComponents: List<EmgBaseComponent> = listOf()
        set(value) {
            field = value
            repaint()
        }

    private var viewCallback: DesignerViewCallback? = null
    private var dragComponent: EmgBaseComponent? = null

    // Connection values
    private var connectionComponent: EmgBaseComponent? = null


    override fun setup(viewCallback: DesignerViewCallback?) {
        this.viewCallback = viewCallback

        // DependencyInjection drag and drop target
        dropTarget = DropTarget(this, DnDConstants.ACTION_COPY, this, true)
        transferHandler = DragDropTargetTransferHandler()

        // DependencyInjection component move functionality
        addMouseListener(object : MouseAdapter() {

            override fun mousePressed(e: MouseEvent?) {
                super.mousePressed(e)

                // Move component
                when {
                    e?.button == MouseEvent.BUTTON1 -> {
                        dragComponent = interactionComponents.firstOrNull { it.box.intersects(Point(e.point.x, e.point.y)) }

                        // New action, erase connection action
                        connectionComponent = null
                    }
                    e?.button == MouseEvent.BUTTON3 -> {

                        // Connect components
                        connectionComponent = if (connectionComponent == null) {
                            interactionComponents.firstOrNull { it.box.intersects(Point(e.point.x, e.point.y)) }
                        } else {
                            val target = interactionComponents.firstOrNull { it.box.intersects(Point(e.point.x, e.point.y)) }
                            if (target != null && connectionComponent != null) {
                                viewCallback?.connectComponents(connectionComponent!!, target)
                            }
                            null
                        }

                        // New action, erase drag action
                        dragComponent = null
                    }
                    e?.button == MouseEvent.BUTTON2 -> {
                        val c = interactionComponents.firstOrNull { it.box.intersects(Point(e.point.x, e.point.y)) }
                        if (c != null) {
                            viewCallback?.showProperties(c)
                        }
                    }
                }
            }

            override fun mouseReleased(e: MouseEvent?) {
                super.mouseReleased(e)

                if (e?.button == MouseEvent.BUTTON1 && dragComponent != null) {
                    viewCallback?.moveComponent(dragComponent!!, e.point.x, e.point.y)
                    dragComponent = null
                }
            }
        })
        addMouseMotionListener(object : MouseAdapter() {

            override fun mouseDragged(e: MouseEvent?) {
                super.mouseDragged(e)

                val p = e?.point ?: return
                dragComponent?.origin = Origin(p.x, p.y)
                repaint()
            }
        })
    }

    override fun paintComponent(g: Graphics?) {
        super.paintComponent(g)

        g?.color = Color.WHITE
        interactionComponents.forEach { c ->
            c.draw().forEach { command -> DesktopDesignerHelper.drawCommand(command, g) }
        }
    }

    override fun dragEnter(dtde: DropTargetDragEvent) {}

    override fun dragOver(dtde: DropTargetDragEvent) {}

    override fun dropActionChanged(dtde: DropTargetDragEvent) {}

    override fun dragExit(dte: DropTargetEvent) {}

    override fun drop(e: DropTargetDropEvent) {
        val src = e.transferable
                .getTransferData(DragDropTransferHandler.COMPONENT_DATA_FLAVOR) as? EmgBaseComponent ?: return
        viewCallback?.addComponent(src, e.location.x, e.location.y)
    }

}