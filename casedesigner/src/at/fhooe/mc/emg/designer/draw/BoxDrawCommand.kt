package at.fhooe.mc.emg.designer.draw

class BoxDrawCommand(x: Int, y: Int, val width: Int, val height: Int,
                     val transparentBackground: Boolean) : DrawCommand(x, y)