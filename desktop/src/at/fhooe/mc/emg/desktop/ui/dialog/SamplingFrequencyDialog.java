package at.fhooe.mc.emg.desktop.ui.dialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SamplingFrequencyDialog extends JDialog implements ActionListener {

	public interface OnSamplingFrequencySelectedListener {

		void onSamplingFrequencySelected(double frequency);

	}

	private static final long serialVersionUID = 1L;

    private JButton doneButton;
	
	private OnSamplingFrequencySelectedListener listener;
	private JTextField textFrequency;

	public SamplingFrequencyDialog(OnSamplingFrequencySelectedListener listener, JFrame mainWindow) {
		setIconImage(Toolkit.getDefaultToolkit().getImage(System.getProperty("user.dir") + "/icons/ic_dialog_fs.png"));
		this.listener = listener;

		int WIDTH = 200;
		int HEIGHT = 120;
		setBounds(mainWindow.getX() + (mainWindow.getWidth()/2) - (WIDTH /2), mainWindow.getY() + (mainWindow.getHeight()/2) - (HEIGHT /2), WIDTH, HEIGHT);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setResizable(false);
		setTitle("Sampling frequency");
		getContentPane().setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

		doneButton = new JButton("Set frequency");
		doneButton.addActionListener(this);
		
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		
		textFrequency = new JTextField();
		textFrequency.addActionListener(this);
		textFrequency.setBorder(new TitledBorder(null, "Frequency in Hz", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		contentPanel.add(textFrequency, BorderLayout.CENTER);
		textFrequency.setColumns(10);
		getContentPane().add(doneButton, BorderLayout.SOUTH);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == doneButton) {
			validateData();
		} else if (e.getSource() == textFrequency) {
			validateData();
		}
	}
	
	private void validateData() {
		double fs = getSamplingFrequency();
		if (fs > 0) {
			if (listener != null) {
				listener.onSamplingFrequencySelected(fs);
				dispose();
			}
		} else {
			JOptionPane.showMessageDialog(this, "Please enter a number bigger than 0.0","Parse error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private double getSamplingFrequency() {
		
		double val = -1;
		String text = textFrequency.getText();
		try {
			val = Double.parseDouble(text);
		} catch(Exception e) {
		    e.printStackTrace();
		}
		return val;
	}

}
