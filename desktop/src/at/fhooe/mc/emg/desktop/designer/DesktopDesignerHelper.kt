package at.fhooe.mc.emg.desktop.designer

import at.fhooe.mc.emg.designer.component.*
import at.fhooe.mc.emg.designer.draw.BoxDrawCommand
import at.fhooe.mc.emg.designer.draw.DrawCommand
import at.fhooe.mc.emg.designer.draw.LineDrawCommand
import at.fhooe.mc.emg.designer.draw.StringDrawCommand
import java.awt.Color
import java.awt.Graphics
import javax.swing.ImageIcon

object DesktopDesignerHelper {

    fun compactComponentColor(c: EmgBaseComponent): Color {
        return when(c) {
            is EmgDeviceComponent -> Color.ORANGE
            is EmgSourceComponent -> Color.ORANGE
            is EmgFilterComponent -> Color.GREEN
            is EmgToolComponent -> Color.BLUE
            is EmgSinkComponent -> Color.BLACK
            is EmgRelaySinkComponent -> Color.LIGHT_GRAY
            else -> Color.decode("#424242")
        }
    }

    fun componentIcon(c: EmgBaseComponent): ImageIcon? {
        val filename = when(c) {
            is EmgDeviceComponent -> "ic_component_device.png"
            is EmgSourceComponent -> "ic_component_device.png"
            is EmgFilterComponent -> "ic_component_filter.png"
            is EmgToolComponent -> "ic_component_tool.png"
            is EmgSinkComponent -> "ic_component_sink.png"
            is EmgRelaySinkComponent -> "ic_component_relay_sink.png"
            else -> null
        }
        return if (filename != null) {
            ImageIcon("${System.getProperty("user.dir")}/desktop/icons/components/$filename")
        } else { null }
    }

    fun drawCommand(command: DrawCommand, g: Graphics?) {

        when (command) {

            is BoxDrawCommand -> drawBoxCommand(command, g)
            is LineDrawCommand -> drawLineCommand(command, g)
            is StringDrawCommand -> drawStringCommand(command, g)
        }
    }

    fun drawStringCommand(c: StringDrawCommand, g: Graphics?) {
        g?.drawString(c.text, c.x, c.y)
    }

    fun drawBoxCommand(c: BoxDrawCommand, g: Graphics?) {
        g?.drawRect(c.x, c.y, c.width, c.height)
    }

    fun drawLineCommand(c: LineDrawCommand, g: Graphics?) {
        g?.drawLine(c.x, c.y, c.xEnd, c.yEnd)
    }

}