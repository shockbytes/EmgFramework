package at.fhooe.mc.emg.desktop.designer

import at.fhooe.mc.emg.designer.component.*
import at.fhooe.mc.emg.designer.draw.*
import sun.swing.SwingUtilities2
import java.awt.Color
import java.awt.Graphics
import javax.swing.ImageIcon


object DesktopDesignerHelper {

    val blueprintColor: Color = Color.decode("#0017a5")

    fun compactComponentColor(c: EmgBaseComponent): Color {
        return when (c) {
            is EmgDeviceComponent -> Color.ORANGE
            is EmgFilterComponent -> Color.GREEN
            is EmgToolComponent -> Color.BLUE
            is EmgSinkComponent -> Color.BLACK
            is EmgRelaySinkComponent -> Color.LIGHT_GRAY
            is EmgRelayComponent -> Color.RED
            else -> Color.decode("#424242")
        }
    }

    fun componentIcon(c: EmgBaseComponent): ImageIcon? {
        val filename = when (c) {
            is EmgDeviceComponent -> "ic_component_device.png"
            is EmgFilterComponent -> "ic_component_filter.png"
            is EmgToolComponent -> "ic_component_tool.png"
            is EmgSinkComponent -> "ic_component_sink.png"
            is EmgRelaySinkComponent -> "ic_component_relay_sink.png"
            is EmgRelayComponent -> "ic_component_relay.png"
            else -> null
        }
        return if (filename != null) {
            ImageIcon("${System.getProperty("user.dir")}/desktop/icons/components/$filename")
        } else {
            null
        }
    }

    fun drawCommand(command: DrawCommand, g: Graphics?) {
        when (command) {
            is BoxDrawCommand -> drawBoxCommand(command, g)
            is LineDrawCommand -> drawLineCommand(command, g)
            is StringDrawCommand -> drawStringCommand(command, g)
            is CenteredStringDrawCommand -> drawCenteredString(command, g)
            is CenteredComponentImageDrawCommand -> drawCenteredComponentImage(command, g)
            is FilledBoxDrawCommand -> drawFilledBoxCommand(command, g)
        }
    }

    private fun drawStringCommand(c: StringDrawCommand, g: Graphics?) {
        g?.drawString(c.text, c.x, c.y)
    }

    private fun drawBoxCommand(c: BoxDrawCommand, g: Graphics?) {
        // Draw a background for the standard view
        if (!c.transparentBackground) {
            g?.color = blueprintColor
            g?.fillRect(c.x, c.y, c.width, c.height)
            g?.color = Color.WHITE
        }
        g?.drawRect(c.x, c.y, c.width, c.height)
    }

    private fun drawFilledBoxCommand(c: FilledBoxDrawCommand, g: Graphics?) {
        g?.fillRect(c.x, c.y, c.width, c.height)
    }

    private fun drawLineCommand(c: LineDrawCommand, g: Graphics?) {
        g?.drawLine(c.x, c.y, c.xEnd, c.yEnd)
    }

    private fun drawCenteredString(c: CenteredStringDrawCommand, g: Graphics?) {
        val width = g?.fontMetrics?.stringWidth(c.text) ?: c.maxWidth
        val overflowPadding = 8
        var text = c.text
        val xOffset = if (width < c.maxWidth) {
            val padding = c.maxWidth - width
            padding / 2
        } else {
            text = SwingUtilities2.clipStringIfNecessary(null, g?.fontMetrics, c.text, c.maxWidth - overflowPadding)
            overflowPadding / 2
        }
        g?.drawString(text, c.x + xOffset, c.y)
    }

    private fun drawCenteredComponentImage(c: CenteredComponentImageDrawCommand, g: Graphics?) {
        val image = componentIconForName(c.className)?.image ?: return
        val x = c.x + c.width / 2 - image.getWidth(null) / 2
        val y = c.y + c.height / 2 - image.getHeight(null) / 2
        g?.drawImage(image, x, y, null)
    }

    private fun componentIconForName(className: String): ImageIcon? {
        val filename = when (className) {
            "EmgDeviceComponent" -> "ic_component_device.png"
            "EmgSourceComponent" -> "ic_component_device.png"
            "EmgFilterComponent" -> "ic_component_filter.png"
            "EmgToolComponent" -> "ic_component_tool.png"
            "EmgSinkComponent" -> "ic_component_sink.png"
            "EmgRelaySinkComponent" -> "ic_component_relay_sink.png"
            "EmgRelayComponent" -> "ic_component_relay.png"
            else -> null
        }
        return if (filename != null) {
            ImageIcon("${System.getProperty("user.dir")}/desktop/icons/components/$filename")
        } else {
            null
        }
    }

}