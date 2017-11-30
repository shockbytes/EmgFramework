package at.fhooe.mc.emg.desktop


import at.fhooe.mc.emg.core.client.network.NetworkClientDriver
import at.fhooe.mc.emg.core.client.simulation.SimulationClientDriver
import at.fhooe.mc.emg.core.tools.conconi.ConconiTool
import at.fhooe.mc.emg.core.tools.peak.PeakDetectionTool
import at.fhooe.mc.emg.desktop.client.network.DesktopNetworkClientDriverConfigView
import at.fhooe.mc.emg.desktop.client.serial.DesktopSerialClientDriverConfigView
import at.fhooe.mc.emg.desktop.client.serial.SerialClientDriver
import at.fhooe.mc.emg.desktop.client.simulation.DesktopSimulationClientDriverConfigView
import at.fhooe.mc.emg.desktop.core.DesktopEmgController
import at.fhooe.mc.emg.desktop.tools.conconi.SwingConconiView
import at.fhooe.mc.emg.desktop.ui.DesktopMainWindow
import java.awt.EventQueue
import java.util.*
import javax.swing.UIManager

object Main {

    @JvmStatic
    fun main(args: Array<String>) {
        EventQueue.invokeLater {

            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

            DesktopEmgController(
                    Arrays.asList(
                            SerialClientDriver(DesktopSerialClientDriverConfigView()),
                            SimulationClientDriver(DesktopSimulationClientDriverConfigView(),
                                    System.getProperty("user.dir") + "/data/simulation"),
                            NetworkClientDriver(DesktopNetworkClientDriverConfigView())),
                    Arrays.asList(
                            ConconiTool(SwingConconiView()),
                            PeakDetectionTool()),
                    DesktopMainWindow())
                    .start()
        }
    }

}
