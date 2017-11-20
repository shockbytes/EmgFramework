package at.fhooe.mc.emg.ui.dialog;

import at.fhooe.mc.emg.util.Configuration;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class FilterConfigDialog extends JDialog implements ChangeListener {

	private static final long serialVersionUID = 1L;

	private JSlider sliderRunningAverage;
	private JLabel labelRunningAverage;
	private JSlider sliderSavitzkyGolay;
	private JLabel labelSavitzkyGolay;

	private Configuration config;

	public FilterConfigDialog(Configuration config, JFrame mainWindow) {
		setResizable(false);
		setTitle("Filter configuration");
		setIconImage(Toolkit.getDefaultToolkit().getImage(System.getProperty("user.dir") + "/icons/ic_dialog_filter_config.png"));
		this.config = config;

		int WIDTH = 345;
		int HEIGHT = 245;
		setBounds(mainWindow.getX() + (mainWindow.getWidth()/2) - (WIDTH /2), mainWindow.getY() + (mainWindow.getHeight()/2) - (HEIGHT /2), WIDTH, HEIGHT);
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.PAGE_AXIS));
		contentPanel.setBorder(new EmptyBorder(16, 16, 16, 16));
		setContentPane(contentPanel);
		
		JPanel panelRunningAverage = new JPanel();
		panelRunningAverage.setBorder(new CompoundBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Running average window size", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), new EmptyBorder(16, 16, 16, 16)));
		getContentPane().add(panelRunningAverage);
		panelRunningAverage.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		sliderRunningAverage = new JSlider();
		sliderRunningAverage.setMinimum(10);
		sliderRunningAverage.setMaximum(70);
		sliderRunningAverage.setValue(config.getRunningAverageWindowSize());
		sliderRunningAverage.addChangeListener(this);
		panelRunningAverage.add(sliderRunningAverage);
		
		labelRunningAverage = new JLabel();
		labelRunningAverage.setText(String.valueOf(config.getRunningAverageWindowSize()));
		panelRunningAverage.add(labelRunningAverage);
		
		JPanel panelSavitzkyGolay = new JPanel();
		panelSavitzkyGolay.setBorder(new CompoundBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Savitzky Golay Filter width", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), new EmptyBorder(16, 16, 16, 16)));
		contentPanel.add(panelSavitzkyGolay);
		panelSavitzkyGolay.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		sliderSavitzkyGolay = new JSlider();
		sliderSavitzkyGolay.setMinimum(1);
		sliderSavitzkyGolay.setMaximum(40);
		sliderSavitzkyGolay.setValue(config.getSavitzkyGolayFilterWidth());
		sliderSavitzkyGolay.addChangeListener(this);
		panelSavitzkyGolay.add(sliderSavitzkyGolay);
		
		labelSavitzkyGolay = new JLabel();
		labelSavitzkyGolay.setText(String.valueOf(config.getSavitzkyGolayFilterWidth()));
		panelSavitzkyGolay.add(labelSavitzkyGolay);
		
		JLabel labelInfo = new JLabel("Changing filter parameters will need a restart to become active");
		labelInfo.setAlignmentX(Component.CENTER_ALIGNMENT);
		labelInfo.setHorizontalAlignment(SwingConstants.CENTER);
		contentPanel.add(labelInfo);
	}

	@Override
	public void stateChanged(ChangeEvent event) {

		if (event.getSource() == sliderRunningAverage) {
			labelRunningAverage.setText(String.valueOf(sliderRunningAverage.getValue()));
			config.setRunningAverageWindowSize(sliderRunningAverage.getValue());
		} else if (event.getSource() == sliderSavitzkyGolay) {
			labelSavitzkyGolay.setText(String.valueOf(sliderSavitzkyGolay.getValue()));
			config.setSavitzkyGolayFilterWidth(sliderSavitzkyGolay.getValue());
		}
	}
}
