package at.fhooe.mc.emg.desktop.util

import java.awt.Component
import java.awt.Toolkit
import javax.swing.ImageIcon
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.filechooser.FileFilter
import javax.swing.filechooser.FileNameExtensionFilter

object UiUtils {

    fun showCsvSaveDialog(): String? {
        return showDialog("Save logged data",
                FileNameExtensionFilter("CSV files", "csv", "txt"),
                ".csv", true)
    }

    fun showConconiSaveDialog(): String? {
        return showDialog("Save Conconi test data",
                FileNameExtensionFilter("Conconi files", "ctf"),
                ".ctf", true)
    }

    fun showAcdOpenDialog(): String? {
        return showDialog("Load Acquisition Case Designer file",
                FileNameExtensionFilter("Acquisition Case Designer files", "acd"),
                ".acd", false)
    }

    fun showAcdSaveDialog(): String? {
        return showDialog("Save Acquisition Case Designer file",
                FileNameExtensionFilter("Acquisition Case Designer files", "acd"),
                ".acd", true)
    }

    fun showConconiLoadDialog(): String? {
        return showDialog("Load Conconi test data",
                FileNameExtensionFilter("Conconi files", "ctf"),
                ".ctf", false)
    }

    fun showErrorMessage(parentComponent: Component, msg: String, title: String) {
        val icon = Toolkit.getDefaultToolkit()
                .getImage(System.getProperty("user.dir") + "/desktop/icons/ic_error.png")
        JOptionPane.showMessageDialog(parentComponent, msg, title, JOptionPane.ERROR_MESSAGE, ImageIcon(icon))
    }

    private fun showDialog(title: String, filter: FileFilter,
                           defaultExtension: String, isSaveDialog: Boolean): String? {

        val fileChooser = JFileChooser()
        fileChooser.fileSelectionMode = JFileChooser.FILES_ONLY
        fileChooser.dialogTitle = title
        fileChooser.fileFilter = filter

        val approval: Int
        approval = if (isSaveDialog) {
            fileChooser.showSaveDialog(null)
        } else {
            fileChooser.showOpenDialog(null)
        }

        return if (approval == JFileChooser.APPROVE_OPTION) {
            var path = fileChooser.selectedFile.absolutePath
            if (!path.endsWith(defaultExtension)) {
                path += defaultExtension
            }
            path
        } else { null }
    }

}
