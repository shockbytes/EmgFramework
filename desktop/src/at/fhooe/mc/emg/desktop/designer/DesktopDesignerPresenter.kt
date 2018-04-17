package at.fhooe.mc.emg.desktop.designer

import at.fhooe.mc.emg.designer.DesignerPresenter
import at.fhooe.mc.emg.designer.component.EmgBaseComponent
import at.fhooe.mc.emg.designer.view.DesignerView
import java.io.File

/**
 * Author:  Martin Macheiner
 * Date:    16.04.2018
 */
class DesktopDesignerPresenter(override val view: DesignerView,
                               override val components: List<EmgBaseComponent>) : DesignerPresenter {

    override fun start(file: File?) {
        view.show(this, components)
    }

    override fun stop() {
        println("Stop")
        // TODO
    }

    override fun open(file: File) {
        println("Open ${file.absolutePath}")
        // TODO
    }

    override fun save(file: File): Boolean {
        println("Save ${file.absolutePath}")
        // TODO
        return false
    }

    override fun reset() {
        println("Reset")
        // TODO
    }

    override fun run() {
        println("Run")
        // TODO
    }

    override fun addComponentByDoubleClick(component: EmgBaseComponent) {
        println("Add component: ${component.name}")
        // TODO
    }

}