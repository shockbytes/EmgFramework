package at.fhooe.mc.emg


import at.fhooe.mc.emg.ui.MainWindow
import java.awt.EventQueue
import javax.swing.UIManager

object Main {

    @JvmStatic
    fun main(args: Array<String>) {
        EventQueue.invokeLater {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
                val window = MainWindow()
                window.isVisible = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}
