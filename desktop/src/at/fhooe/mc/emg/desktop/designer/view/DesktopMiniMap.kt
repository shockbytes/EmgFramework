package at.fhooe.mc.emg.desktop.designer.view

import at.fhooe.mc.emg.designer.component.EmgBaseComponent
import at.fhooe.mc.emg.designer.view.MiniMap
import at.fhooe.mc.emg.desktop.designer.DesktopDesignerHelper.compactComponentColor
import at.fhooe.mc.emg.desktop.designer.DesktopDesignerHelper.drawCommand
import java.awt.Color
import java.awt.Graphics
import javax.swing.JComponent

class DesktopMiniMap(defaultScale: Int) : JComponent(), MiniMap {

    private val drawColor = Color.decode("#424242")

    override var scale: Int = defaultScale

    override var miniMapComponents : List<EmgBaseComponent> = listOf()
        set(value) {
            field = value
            invalidateMiniMap()
        }

    override fun invalidateMiniMap() {
        repaint()
    }

    override fun paintComponent(g: Graphics?) {
        super.paintComponent(g)

        // Draw background
        g?.color = Color.WHITE
        g?.fillRect(0, 0, width, height)

        // Draw dark components
        g?.color = drawColor
		miniMapComponents.forEach { c ->
            g?.color = compactComponentColor(c)
            c.drawCompact(scale).forEach { command -> drawCommand(command, g)}
        }
    }

}
