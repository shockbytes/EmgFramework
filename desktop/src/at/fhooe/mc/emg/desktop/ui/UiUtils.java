package at.fhooe.mc.emg.desktop.ui;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class UiUtils {


    public static String showCsvSaveDialog() {
        return showDialog("Save logged data",
                new FileNameExtensionFilter("CSV files", "csv", "txt"),
                ".csv", true);
    }

    public static String showConconiSaveDialog() {
        return showDialog("Save Conconi test data",
                new FileNameExtensionFilter("Conconi files", "ctf"),
                ".ctf", true);
    }

    public static String showConconiLoadDialog() {
        return showDialog("Load Conconi test data",
                new FileNameExtensionFilter("Conconi files", "ctf"),
                ".ctf", false);
    }

	private static String showDialog(String title, FileFilter filter, String defaultExtension, boolean isSaveDialog) {

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setDialogTitle(title);
        fileChooser.setFileFilter(filter);


        int approval;
        if (isSaveDialog) {
            approval = fileChooser.showSaveDialog(null);
        } else {
            approval = fileChooser.showOpenDialog(null);
        }

        if (approval == JFileChooser.APPROVE_OPTION) {

            String path =  fileChooser.getSelectedFile().getAbsolutePath();
            if (!path.endsWith(defaultExtension)) {
                path = path.concat(defaultExtension);
            }
            return path;

        } else {
            return null;
        }

    }
	
}
