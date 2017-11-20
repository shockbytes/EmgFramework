package at.fhooe.mc.emg;


import at.fhooe.mc.emg.ui.MainWindow;

import javax.swing.*;
import java.awt.*;

public class Main {

	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                MainWindow window = new MainWindow();
                window.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
	}

	
}
