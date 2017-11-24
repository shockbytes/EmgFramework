package at.fhooe.mc.emg.desktop


import java.awt.EventQueue
import javax.swing.UIManager

object Main {

    @JvmStatic
    fun main(args: Array<String>) {
        EventQueue.invokeLater {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
                val window = at.fhooe.mc.emg.desktop.ui.MainWindow()
                window.isVisible = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}
