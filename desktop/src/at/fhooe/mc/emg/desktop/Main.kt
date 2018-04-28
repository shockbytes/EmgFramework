package at.fhooe.mc.emg.desktop


import at.fhooe.mc.emg.core.injection.BasicReflectionsDependencyInjection
import at.fhooe.mc.emg.desktop.core.DesktopEmgPresenter
import at.fhooe.mc.emg.desktop.core.DesktopPlatformConfiguration
import at.fhooe.mc.emg.desktop.view.DesktopMainWindow
import java.awt.EventQueue
import javax.swing.UIManager

object Main {

    @JvmStatic
    fun main(args: Array<String>) {
        EventQueue.invokeLater {

            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

            val injector = BasicReflectionsDependencyInjection(DesktopPlatformConfiguration())
            DesktopEmgPresenter(injector.driver,
                    injector.tools,
                    injector.filter,
                    injector.frequencyAnalysisMethods,
                    Pair(injector.components, injector.componentPipes),
                    injector.configStorage,
                    DesktopMainWindow())
                    .start()
        }
    }

}
