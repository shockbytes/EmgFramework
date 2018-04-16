package at.fhooe.mc.emg.desktop


import at.fhooe.mc.emg.core.setup.BasicReflectionsSetup
import at.fhooe.mc.emg.desktop.core.DesktopEmgPresenter
import at.fhooe.mc.emg.desktop.ui.DesktopMainWindow
import java.awt.EventQueue
import javax.swing.UIManager

object Main {

    @JvmStatic
    fun main(args: Array<String>) {
        EventQueue.invokeLater {

            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

            val setup = BasicReflectionsSetup()
            DesktopEmgPresenter(setup.driver, setup.tools, setup.filter, setup.frequencyAnalysisMethods,
                    setup.components, setup.configStorage, DesktopMainWindow())
                    .start()
        }
    }

}
