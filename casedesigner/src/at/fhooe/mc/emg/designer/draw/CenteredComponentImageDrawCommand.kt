package at.fhooe.mc.emg.designer.draw

class CenteredComponentImageDrawCommand(x: Int, y: Int,
                                        val width: Int, val height: Int,
                                        val className: String,
                                        val scale: Int = 1) : DrawCommand(x, y)