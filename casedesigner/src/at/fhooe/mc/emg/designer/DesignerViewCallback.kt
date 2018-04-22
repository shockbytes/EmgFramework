package at.fhooe.mc.emg.designer

import at.fhooe.mc.emg.designer.component.EmgBaseComponent
import java.io.File

interface DesignerViewCallback {

    fun stop()

    fun open(file: File)

    fun save(file: File)

    fun reset()

    fun validate()

    fun run()

    fun addComponent(component: EmgBaseComponent, x: Int, y: Int)

    fun addComponentByDoubleClick(component: EmgBaseComponent)

    fun removeComponent(component: EmgBaseComponent)

    fun moveComponent(component: EmgBaseComponent, xNew: Int, yNew: Int)

    fun connectComponents(component1: EmgBaseComponent, component2: EmgBaseComponent)

    fun showProperties(component: EmgBaseComponent)

}