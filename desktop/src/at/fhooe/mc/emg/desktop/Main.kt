package at.fhooe.mc.emg.desktop


import at.fhooe.mc.emg.core.injection.PluginReflectionsDependencyInjection
import at.fhooe.mc.emg.desktop.core.DesktopEmgPresenter
import at.fhooe.mc.emg.desktop.core.DesktopPlatformConfiguration
import at.fhooe.mc.emg.desktop.view.DesktopMainWindow
import io.reactivex.internal.functions.Functions
import io.reactivex.plugins.RxJavaPlugins
import java.awt.EventQueue
import java.io.File
import javax.swing.UIManager

object Main {

    @JvmStatic
    fun main(args: Array<String>) {
        EventQueue.invokeLater {

            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
            RxJavaPlugins.setErrorHandler(Functions.emptyConsumer())

            val injector = PluginReflectionsDependencyInjection(DesktopPlatformConfiguration(),
                    File("${System.getProperty("user.dir")}/plugin_test/"))
            DesktopEmgPresenter(injector.driver,
                    injector.tools,
                    injector.filter,
                    injector.frequencyAnalysisMethods,
                    Pair(injector.components, injector.componentPipes),
                    injector.testSubjects,
                    injector.configStorage,
                    DesktopMainWindow())
                    .start()
        }
    }

}
