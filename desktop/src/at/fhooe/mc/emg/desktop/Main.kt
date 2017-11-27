package at.fhooe.mc.emg.desktop


import at.fhooe.mc.emg.client.network.NetworkClient
import at.fhooe.mc.emg.client.simulation.SimulationClient
import at.fhooe.mc.emg.desktop.client.network.DesktopNetworkClientConfigView
import at.fhooe.mc.emg.desktop.client.serial.DesktopSerialClientConfigView
import at.fhooe.mc.emg.desktop.client.serial.SerialClient
import at.fhooe.mc.emg.desktop.client.simulation.DesktopSimulationClientConfigView
import at.fhooe.mc.emg.desktop.core.DesktopEmgController
import at.fhooe.mc.emg.desktop.tools.conconi.SwingConconiView
import at.fhooe.mc.emg.desktop.ui.DesktopMainWindow
import at.fhooe.mc.emg.tools.conconi.ConconiTool
import at.fhooe.mc.emg.tools.peak.PeakDetectionTool
import java.awt.EventQueue
import java.util.*
import javax.swing.UIManager

object Main {

    @JvmStatic
    fun main(args: Array<String>) {
        EventQueue.invokeLater {

            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
            val view = DesktopMainWindow()

            DesktopEmgController(
                    Arrays.asList(
                            SerialClient(DesktopSerialClientConfigView()),
                            SimulationClient(DesktopSimulationClientConfigView(),
                                    System.getProperty("user.dir") + "/data/simulation"),
                            NetworkClient(DesktopNetworkClientConfigView())),
                    Arrays.asList(
                            ConconiTool(SwingConconiView()),
                            PeakDetectionTool()),
                    view)
        }
    }

}
