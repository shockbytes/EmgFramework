package at.fhooe.mc.emg.designer.component

import at.fhooe.mc.emg.designer.draw.BoxDrawCommand
import at.fhooe.mc.emg.designer.draw.DrawCommand
import at.fhooe.mc.emg.designer.draw.LineDrawCommand
import at.fhooe.mc.emg.designer.draw.StringDrawCommand
import at.fhooe.mc.emg.designer.draw.model.Box
import at.fhooe.mc.emg.designer.draw.model.Origin

/**
 * Author:  Martin Macheiner
 * Date:    16.04.2018
 */
abstract class EmgBaseComponent(val name: String, val qualifiedName: String) {

    var width = DEFAULT_WIDTH
    var box: Box = Box(Origin(0, 0), width, width)

    var origin: Origin
        get() = box.origin
        set(value) {
            box.origin = value
        }

    fun draw(): List<DrawCommand> {
        return listOf(
                BoxDrawCommand(
                        origin.x,
                        origin.y,
                        box.width,
                        box.height),
                StringDrawCommand(
                        origin.x + (box.width / 2),
                        origin.y + 10,
                        name),
                LineDrawCommand(
                        origin.x,
                        origin.y + 15,
                        origin.x + box.width,
                        origin.y + 15))
    }

    fun drawCompact(scale: Int): List<DrawCommand> {
        return listOf(
                BoxDrawCommand(
                        origin.x / scale,
                        origin.y / scale,
                        box.width / scale,
                        box.height / scale))
    }

    companion object {
        const val DEFAULT_WIDTH = 70
    }

}