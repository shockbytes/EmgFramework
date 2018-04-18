package at.fhooe.mc.emg.designer.component

import at.fhooe.mc.emg.designer.draw.*
import at.fhooe.mc.emg.designer.draw.model.Box
import at.fhooe.mc.emg.designer.draw.model.Origin



/**
 * Author:  Martin Macheiner
 * Date:    16.04.2018
 */
abstract class EmgBaseComponent(val name: String = "", val qualifiedName: String = "", origin: Origin = Origin(0, 0)) {

    var box: Box = Box(origin, DEFAULT_WIDTH, DEFAULT_HEIGHT)

    var origin: Origin
        get() = box.origin
        set(value) {
            box.origin = value
        }

    val type: String = javaClass.simpleName

    fun draw(): List<DrawCommand> {
        return listOf(
                BoxDrawCommand(
                        origin.x,
                        origin.y,
                        box.width,
                        box.height),
                CenteredStringDrawCommand(
                        origin.x,
                        origin.y + 12,
                        box.width,
                        name),
                CenteredComponentImageDrawCommand(
                        origin.x,
                        origin.y + NAME_COMPARTMENT_HEIGHT,
                        box.width,
                        box.height,
                        javaClass.simpleName
                ),
                LineDrawCommand(
                        origin.x,
                        origin.y + NAME_COMPARTMENT_HEIGHT,
                        origin.x + box.width,
                        origin.y + NAME_COMPARTMENT_HEIGHT))
    }

    fun drawCompact(scale: Int): List<DrawCommand> {
        return listOf(
                BoxDrawCommand(
                        origin.x / scale,
                        origin.y / scale,
                        box.width / scale,
                        box.height / scale))
    }

    abstract fun copyWithOrigin(x: Int, y: Int): EmgBaseComponent

    override fun equals(other: Any?): Boolean {
        return if (other is EmgBaseComponent) {
            other.name == name && other.origin == origin
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + qualifiedName.hashCode()
        result = 31 * result + box.hashCode()
        return result
    }

    companion object {
        const val DEFAULT_HEIGHT = 90
        const val DEFAULT_WIDTH = DEFAULT_HEIGHT
        const val NAME_COMPARTMENT_HEIGHT = 15
    }

}