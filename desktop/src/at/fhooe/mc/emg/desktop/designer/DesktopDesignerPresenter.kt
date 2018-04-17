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
        view.show(components)
    }

    override fun stop() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}