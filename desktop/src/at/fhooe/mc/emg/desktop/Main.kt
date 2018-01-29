package at.fhooe.mc.emg.desktop


import at.fhooe.mc.emg.core.client.network.NetworkClientDriver
import at.fhooe.mc.emg.core.client.simulation.SimulationClientDriver
import at.fhooe.mc.emg.core.tools.conconi.ConconiTool
import at.fhooe.mc.emg.core.tools.fatigue.MuscleFatigueTool
import at.fhooe.mc.emg.core.tools.peaks.PeakDetectionTool
import at.fhooe.mc.emg.core.util.config.JsonEmgConfigStorage
import at.fhooe.mc.emg.desktop.client.bluetooth.DesktopBluetoothClientDriver
import at.fhooe.mc.emg.desktop.client.bluetooth.DesktopBluetoothClientDriverConfigView
import at.fhooe.mc.emg.desktop.client.network.DesktopNetworkClientDriverConfigView
import at.fhooe.mc.emg.desktop.client.serial.DesktopSerialClientDriverConfigView
import at.fhooe.mc.emg.desktop.client.serial.SerialClientDriver
import at.fhooe.mc.emg.desktop.client.simulation.DesktopSimulationClientDriverConfigView
import at.fhooe.mc.emg.desktop.core.DesktopEmgPresenter
import at.fhooe.mc.emg.desktop.storage.DesktopFileStorage
import at.fhooe.mc.emg.desktop.tools.conconi.SwingConconiView
import at.fhooe.mc.emg.desktop.tools.fatigue.SwingMuscleFatigueView
import at.fhooe.mc.emg.desktop.tools.peaks.SwingPeakDetectionView
import at.fhooe.mc.emg.desktop.ui.DesktopMainWindow
import java.awt.EventQueue
import java.io.File
import javax.swing.UIManager

object Main {

    @JvmStatic
    fun main(args: Array<String>) {
        EventQueue.invokeLater {

            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

            DesktopEmgPresenter(
                    listOf(
                            SerialClientDriver(DesktopSerialClientDriverConfigView()),
                            SimulationClientDriver(DesktopSimulationClientDriverConfigView(),
                                    System.getProperty("user.dir") + "/data/simulation"),
                            NetworkClientDriver(DesktopNetworkClientDriverConfigView()),
                            DesktopBluetoothClientDriver(DesktopBluetoothClientDriverConfigView())),
                    listOf(
                            ConconiTool(SwingConconiView(), DesktopFileStorage()),
                            PeakDetectionTool(SwingPeakDetectionView()),
                            MuscleFatigueTool(SwingMuscleFatigueView())),
                    DesktopMainWindow(),
                    JsonEmgConfigStorage(File(System.getProperty("user.dir") + "/data/config.json")))
                    .start()
        }
    }

}
