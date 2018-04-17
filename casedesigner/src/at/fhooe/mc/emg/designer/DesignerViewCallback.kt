package at.fhooe.mc.emg.designer

import at.fhooe.mc.emg.designer.component.EmgBaseComponent
import java.io.File

interface DesignerViewCallback {

    fun stop()

    fun open(file: File)

    fun save(file: File): Boolean

    fun reset()

    fun run()

    fun addComponentByDoubleClick(component: EmgBaseComponent)

}