package at.fhooe.mc.emg.desktop.ui

import javax.swing.*
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

    fun showConconiLoadDialog(): String? {
        return showDialog("Load Conconi test data",
                FileNameExtensionFilter("Conconi files", "ctf"),
                ".ctf", false)
    }

    private fun showDialog(title: String, filter: FileFilter, defaultExtension: String, isSaveDialog: Boolean): String? {

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
