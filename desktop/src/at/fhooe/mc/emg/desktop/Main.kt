package at.fhooe.mc.emg.desktop


import at.fhooe.mc.emg.client.network.NetworkClient
import at.fhooe.mc.emg.client.simulation.SimulationClient
import at.fhooe.mc.emg.desktop.client.serial.SerialClient
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
            try {

                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
                val view = DesktopMainWindow()

                DesktopEmgController(
                        Arrays.asList(
                                SerialClient(),
                                SimulationClient(System.getProperty("user.dir") + "/data/simulation"),
                                NetworkClient()),
                        Arrays.asList(
                                ConconiTool(SwingConconiView()),
                                PeakDetectionTool()),
                        view)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}
