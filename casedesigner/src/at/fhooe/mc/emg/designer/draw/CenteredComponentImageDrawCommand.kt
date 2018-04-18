package at.fhooe.mc.emg.designer.draw

class CenteredComponentImageDrawCommand(x: Int, y: Int,
                                        val width: Int, val height: Int,
                                        val className: String) : DrawCommand(x, y)