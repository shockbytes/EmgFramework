package at.fhooe.mc.emg.designer.component.internal

import at.fhooe.mc.emg.designer.component.EmgBaseComponent
import at.fhooe.mc.emg.designer.draw.DrawCommand
import at.fhooe.mc.emg.designer.draw.LineDrawCommand

class RelayConnectorComponent(start: EmgBaseComponent,
                              end: EmgBaseComponent,
                              val relays: List<EmgBaseComponent>) : ConnectorComponent(start, end) {

    override val portConfiguration: Pair<Boolean, Boolean> = Pair(false, false)

    override fun draw(): List<DrawCommand> {
        return listOf(LineDrawCommand(start.origin.x + start.box.width, start.origin.y + start.box.height / 2,
                end.origin.x, end.origin.y + end.box.height / 2))
    }

    override fun drawCompact(scale: Int): List<DrawCommand> {
        return listOf(LineDrawCommand((start.origin.x + start.box.width) / scale, (start.origin.y + start.box.height / 2) / scale,
                (end.origin.x) / scale, (end.origin.y + end.box.height / 2) / scale))
    }

    override fun copyWithOrigin(x: Int, y: Int): EmgBaseComponent {
        throw UnsupportedOperationException("Cannot copy internal component classes!")
    }

}