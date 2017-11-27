package at.fhooe.mc.emg.desktop.ui.dialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class VisualYMaxDialog extends JDialog implements ActionListener {

	public interface OnVisualMaxEnteredListener {

		void onVisualMaxEntered(double max);
	}

	private static final long serialVersionUID = 1L;

	private JButton doneButton;
	
	private OnVisualMaxEnteredListener listener;
	private JTextField textMax;

	public static void show(OnVisualMaxEnteredListener listener, JFrame mainWindow) {
	    VisualYMaxDialog dialog = new VisualYMaxDialog(listener, mainWindow);
        dialog.setVisible(true);
	}

	private VisualYMaxDialog(OnVisualMaxEnteredListener listener, JFrame mainWindow) {
		setIconImage(Toolkit.getDefaultToolkit()
                .getImage(System.getProperty("user.dir") + "/desktop/icons/ic_dialog_graph_max.png"));
		this.listener = listener;
		int WIDTH = 200;
		int HEIGHT = 120;
		setBounds(mainWindow.getX() + (mainWindow.getWidth()/2) - (WIDTH /2), mainWindow.getY() + (mainWindow.getHeight()/2) - (HEIGHT /2), WIDTH, HEIGHT);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setResizable(false);
		setTitle("Maximum visualView value");
		getContentPane().setLayout(new BorderLayout());

		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

		doneButton = new JButton("Set visualView max");
		doneButton.addActionListener(this);
		
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		
		textMax = new JTextField();
		textMax.setBorder(new TitledBorder(null, "Y-Axis maximum", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		textMax.addActionListener(this);
		contentPanel.add(textMax, BorderLayout.CENTER);
		textMax.setColumns(10);
		getContentPane().add(doneButton, BorderLayout.SOUTH);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == doneButton) {
			validateData();
		} else if (e.getSource() == textMax) {
			validateData();
		}
	}
	
	private void validateData() {
		
		double max = getValue();
		if (max > 0) {
			if (listener != null) {
				listener.onVisualMaxEntered(max);
				dispose();
			}
		} else {
			JOptionPane.showMessageDialog(this, "Please enter a number bigger than 0.0","Parse error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private double getValue() {
		
		double val = -1;
		String text = textMax.getText();
		try {
			val = Integer.parseInt(text);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return val;
	}

}
