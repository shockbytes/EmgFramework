package at.fhooe.mc.emg.ui;

import at.fhooe.mc.emg.client.ChannelData;
import at.fhooe.mc.emg.client.ClientDataCallback;
import at.fhooe.mc.emg.client.serial.SerialClient;
import at.fhooe.mc.emg.core.EmgController;
import at.fhooe.mc.emg.storage.CsvDataStorage;
import at.fhooe.mc.emg.ui.dialog.FilterConfigDialog;
import at.fhooe.mc.emg.ui.dialog.SamplingFrequencyDialog;
import at.fhooe.mc.emg.ui.dialog.VisualYMaxDialog;
import at.fhooe.mc.emg.visual.Visual;
import at.fhooe.mc.emg.visual.XChartVisual;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;

public class MainWindow extends JFrame implements ActionListener, ClientDataCallback,
		EmgController.OnSimulationSourcesChangedListener, SamplingFrequencyDialog.OnSamplingFrequencySelectedListener,
		VisualYMaxDialog.OnVisualMaxEnteredListener {

	private static final long serialVersionUID = 1L;

	private JLabel labelStatus;
	private JTextArea textAreaConsole;

    private JSplitPane splitPane;

    private JMenuItem menuItemExit;
    private JMenuItem menuItemFft;
    private JMenuItem menuItemReset;
    private JMenuItem menuItemExport;
    private JMenuItem menuItemConnect;
    private JMenuItem menuItemVisualMax;
    private JMenuItem menuItemDisconnect;
    private JMenuItem menuItemReloadPorts;
    private JMenuItem menuItemFilterConfig;
    private JMenuItem menuItemPowerSpectrum;
    private JMenuItem menuItemSamplingFrequency;

    private JCheckBoxMenuItem cbMenuItemLogging;
    private JCheckBoxMenuItem cbMenuItemPlaybackLoop;
    private JCheckBoxMenuItem cbMenuItemCopyToSimulation;

    private JMenu menuPorts;
	private JMenu mnChannel;
    private JMenu mnClients;
    private JMenu mnDataRate;
    private JMenu mnTools;
    private JMenu mnSimulationData;

	// --------------------------------------------------

	private EmgController<JComponent> controller;

	private Visual<JComponent> visual;

    public MainWindow() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(System.getProperty("user.dir") + "/icons/ic_main.jpg"));
		initialize();
	}

	private void initialize() {
		setResizable(true);
		setBounds(100, 150, 450, 300);
		setTitle("EMG Data Suite v1.0");
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);
				controller.saveConfig();
			}
		});

        initializeController();
		initializeMenu();
		initializeViews();
		setupMenus();
	}

	// --------------- Initialization & setup methods ----------------

	private void initializeController() {
        controller = new EmgController<>(new XChartVisual());
        controller.addCallbackListener(this);
        controller.setOnSimulationSourcesChangedListener(this);
    }

	private void initializeViews() {

        getContentPane().setBackground(Color.WHITE);

        splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.3);
		getContentPane().add(splitPane, BorderLayout.CENTER);

		labelStatus = new JLabel();
		labelStatus.setBorder(new EmptyBorder(4, 4, 4, 4));
		getContentPane().add(labelStatus, BorderLayout.SOUTH);

		textAreaConsole = new JTextArea();
		textAreaConsole.setEditable(false);
		splitPane.setLeftComponent(new JScrollPane(textAreaConsole));

        visual = controller.getVisual();
        visual.initialize();
        splitPane.setRightComponent(visual.getView());

		updateStatus(false);
	}

	private void initializeMenu() {

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);

		cbMenuItemLogging = new JCheckBoxMenuItem("Write data file");
		cbMenuItemLogging.setSelected(true);
		cbMenuItemLogging.addActionListener(this);
		mnFile.add(cbMenuItemLogging);

        cbMenuItemCopyToSimulation = new JCheckBoxMenuItem("Copy to simulation");
        cbMenuItemCopyToSimulation.addActionListener(this);
        cbMenuItemCopyToSimulation.setSelected(true);
        mnFile.add(cbMenuItemCopyToSimulation);

		menuItemExport = new JMenuItem("Export");
        menuItemExport.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK));
		menuItemExport.addActionListener(this);
		mnFile.add(menuItemExport);

        menuItemReset = new JMenuItem("Reset");
        menuItemReset.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK));
        menuItemReset.addActionListener(this);
        mnFile.add(menuItemReset);

		menuItemExit = new JMenuItem("Exit");
		menuItemExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK));
		menuItemExit.addActionListener(this);
        mnFile.add(menuItemExit);

		JMenu mnDevice = new JMenu("Device");
		menuBar.add(mnDevice);

		mnClients = new JMenu("Clients");
		mnDevice.add(mnClients);
		mnDevice.add(new JSeparator());

		menuItemConnect = new JMenuItem("Connect");
        menuItemConnect.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F10, InputEvent.SHIFT_MASK));
        menuItemConnect.addActionListener(this);
		mnDevice.add(menuItemConnect);

		menuItemDisconnect = new JMenuItem("Disconnect");
		menuItemDisconnect.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK));
		menuItemDisconnect.addActionListener(this);
		mnDevice.add(menuItemDisconnect);

		menuItemSamplingFrequency = new JMenuItem("Sampling Frequency");
		menuItemSamplingFrequency.addActionListener(this);
		mnDevice.add(menuItemSamplingFrequency);

		mnDataRate = new JMenu("Data rate");

		menuPorts = new JMenu("Ports");
		mnDevice.add(menuPorts);
		
		menuItemReloadPorts = new JMenuItem("Reload");
		menuItemReloadPorts.addActionListener(this);

		mnDevice.add(mnDataRate);

		mnChannel = new JMenu("Channel");
		menuBar.add(mnChannel);
		
		menuItemFilterConfig = new JMenuItem("Configuration");
		menuItemFilterConfig.addActionListener(this);
		mnChannel.add(menuItemFilterConfig);
		
		JSeparator separator_1 = new JSeparator();
		mnChannel.add(separator_1);

		JMenu mnSimulation = new JMenu("Simulation");
		menuBar.add(mnSimulation);

		mnSimulationData = new JMenu("Data source");
		mnSimulation.add(mnSimulationData);

		cbMenuItemPlaybackLoop = new JCheckBoxMenuItem("Playback loop");
		cbMenuItemPlaybackLoop.addActionListener(this);
		mnSimulation.add(cbMenuItemPlaybackLoop);

		JMenu mnAnalysis = new JMenu("Analysis");
		menuBar.add(mnAnalysis);

		menuItemFft = new JMenuItem("FFT");
        menuItemFft.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK));
        menuItemFft.addActionListener(this);
		mnAnalysis.add(menuItemFft);

		menuItemPowerSpectrum = new JMenuItem("Power Spectrum");
		menuItemPowerSpectrum.addActionListener(this);
        menuItemPowerSpectrum.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK));
        mnAnalysis.add(menuItemPowerSpectrum);

        mnTools = new JMenu("Tools");
        menuBar.add(mnTools);

        JMenu mnDebug = new JMenu("Debug");
        menuBar.add(mnDebug);

        menuItemVisualMax = new JMenuItem("Visual max");
		menuItemVisualMax.addActionListener(this);
		mnDebug.add(menuItemVisualMax);

		// Disable all controls until connection is established
		setDeviceControlsEnabled(false);

		// Initialize checkboxes from stored config
        cbMenuItemLogging.setSelected(controller.getConfig().isWriteToLogEnabled());
        cbMenuItemCopyToSimulation.setSelected(controller.getConfig().isCopyToSimulationEnabled());
        cbMenuItemPlaybackLoop.setSelected(controller.getConfig().isSimulationEndlessLoopEnabled());
	}

	private void setupMenus() {

		// Setup all dynamic menus
		setupAvailableCommPorts(false);
		setupSimulationSourceMenu();
        setupDataRateMenu();
        setupFilterMenu();
        setupClientMenu();
        setupToolsMenu();
	}

	private void setupSimulationSourceMenu() {

		mnSimulationData.removeAll();
		controller.getSimulationSources().forEach((src) -> {

			JCheckBoxMenuItem item = new JCheckBoxMenuItem(src.getName());
			item.addActionListener(e -> {

			    Arrays.stream(mnSimulationData.getMenuComponents()).forEach(element -> {
                    JCheckBoxMenuItem otherItem = (JCheckBoxMenuItem) element;
                    if (otherItem != item) {
                        otherItem.setSelected(false);
                    }
                });

                controller.setSelectedSimulationSource(src);
            });
			mnSimulationData.add(item);
		});

		if (controller.getSimulationSources().size() > 0) {
		    mnSimulationData.getItem((controller.getSimulationSources().size()/2) + 1).doClick();
        }
    }

	private void setupFilterMenu() {
		
		controller.getFilters().forEach(filter -> {
			
			JCheckBoxMenuItem item = new JCheckBoxMenuItem(filter.getName());
			item.addItemListener(e -> filter.setEnabled(e.getStateChange() == ItemEvent.SELECTED));
			mnChannel.add(item);
		});
		mnChannel.getItem(2).setSelected(true);		
	}
	
	private void setupAvailableCommPorts(boolean forceUpdate) {
		
		menuPorts.removeAll();
		
		menuPorts.add(menuItemReloadPorts);
		menuPorts.add(new JSeparator());
				
		controller.getAvailableSerialPorts(forceUpdate).forEach(s -> {
			
			JCheckBoxMenuItem item = new JCheckBoxMenuItem(s);
			item.addActionListener(e -> controller.setSerialPortSelected(item.getText()));
			menuPorts.add(item);
		});
		
		// Select the first port
		if (menuPorts.getItemCount() > 2) {
			menuPorts.getItem(2).setSelected(true);
			controller.setSerialPortSelected(menuPorts.getItem(2).getText());
		}
	}

	private void setupDataRateMenu() {
        Arrays.stream(SerialClient.SUPPORTED_DATA_RATES).forEach((dataRate) -> {

            JCheckBoxMenuItem item = new JCheckBoxMenuItem(String.valueOf(dataRate));
            item.addActionListener(e -> {

                Arrays.stream(mnDataRate.getMenuComponents()).forEach(element -> {

                    JCheckBoxMenuItem otherItem = (JCheckBoxMenuItem) element;
                    if (otherItem != item) {
                        otherItem.setSelected(false);
                    }
                });

                int rate = Integer.parseInt(item.getText());
                controller.getClient().setDataRate(rate);
                updateStatus(true);
            });

            if (dataRate == SerialClient.DEFAULT_DATA_RATE) {
                item.setSelected(true);
            }
            mnDataRate.add(item);
        });
    }

    private void setupClientMenu() {

        mnClients.removeAll();

	    controller.getClients().forEach(c -> {

	        JCheckBoxMenuItem item = new JCheckBoxMenuItem(c.getShortName());
            item.setSelected(c == controller.getClient()); // Select default client
	        item.addActionListener(e -> Arrays.stream(mnClients.getMenuComponents()).forEach(element -> {

                JCheckBoxMenuItem otherItem = (JCheckBoxMenuItem) element;
                if (otherItem != item) {
                    otherItem.setSelected(false);
                }
                controller.setClient(c);
            }));

	        mnClients.add(item);
	    });
    }

    private void setupToolsMenu() {

        mnTools.removeAll();
        controller.getTools().forEach(p -> {
            JMenuItem item = new JMenuItem(p.getName());
            item.addActionListener(e -> p.start(controller));
            mnTools.add(item);
        });
    }

	// ---------------------------------------------------------------

	// ------------------------ Helper methods -----------------------

	private void setDeviceControlsEnabled(boolean isEnabled) {

		mnDataRate.setEnabled(isEnabled);
		menuItemDisconnect.setEnabled(isEnabled);
		menuItemSamplingFrequency.setEnabled(isEnabled);

		// Logic is in reverse for connection and channels
		mnChannel.setEnabled(!isEnabled);
		mnClients.setEnabled(!isEnabled);
		menuItemConnect.setEnabled(!isEnabled);
	}

	private void updateStatus(boolean isConnected) {

		String text;
		if (isConnected) {
			text = "Status: Connected to " + controller.getClient().getName();
			text += " | Baud: " + controller.getClient().getDataRate();
			double fs = controller.getClient().getSamplingFrequency();
			text += " | fs = " + ((fs > 0) ? (fs + "Hz") : "N/A");

		} else {
			text = "Status: Not connected";
		}

		labelStatus.setText(text);
	}

	// ---------------------------------------------------------------

	// ------------------------- Menu actions ------------------------

	// ---> File menu
	private void reset() {
		
		synchronized (this) {

			textAreaConsole.setText("");
			splitPane.remove(2);

			visual.reset();
            splitPane.setRightComponent(visual.getView());
		}
	}

    // ---> Device menu
	private void connectToDevice() {

        try {

            reset();

            controller.connect();

            updateStatus(true);
            setDeviceControlsEnabled(true);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, e.getLocalizedMessage());
        }
	}

	private void disconnectFromDevice() {

        controller.disconnect();
		setDeviceControlsEnabled(false);
		updateStatus(false);
	}

	private void showSamplingFrequencyDialog() {
		SamplingFrequencyDialog dialog = new SamplingFrequencyDialog(this, this);
		dialog.setVisible(true);
	}

	// ---> Channel menu
	private void showFilterConfigurationDialog() {
		FilterConfigDialog dialog = new FilterConfigDialog(controller.getConfig(), this);
		dialog.setVisible(true);
	}

	// ---> Analysis menu
	private void doFrequencyAnalysis(FrequencyAnalysisFrame.AnalysisType type) {

		FrequencyAnalysisFrame fftFrame = new FrequencyAnalysisFrame(type, visual.getDataForFrequencyAnalysis(),
                controller.getClient().getSamplingFrequency(), this);
		fftFrame.setVisible(true);
	}

	// ---> Debug menu
	private void showVisualMaxDialog() {
		VisualYMaxDialog dialog = new VisualYMaxDialog(this, this);
		dialog.setVisible(true);
	}
	
	// ---------------------------------------------------------------

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == menuItemConnect) {
			connectToDevice();
		} else if (e.getSource() == menuItemDisconnect) {
			disconnectFromDevice();
		} else if (e.getSource() == menuItemExit) {
			System.exit(0);
		} else if (e.getSource() == menuItemSamplingFrequency) {
			showSamplingFrequencyDialog();
		} else if (e.getSource() == menuItemVisualMax) {
			showVisualMaxDialog();
		} else if (e.getSource() == cbMenuItemLogging) {
			controller.getConfig().setWriteToLogEnabled(cbMenuItemLogging.isSelected());
		} else if (e.getSource() == cbMenuItemCopyToSimulation) {
			controller.getConfig().setCopyToSimulationEnabled(cbMenuItemCopyToSimulation.isSelected());
		} else if (e.getSource() == cbMenuItemPlaybackLoop) {
		    controller.setSimulationPlaybackLoopEnabled(cbMenuItemPlaybackLoop.isSelected());
		} else if (e.getSource() == menuItemReset) {
			reset();
		} else if (e.getSource() == menuItemFft) {
			doFrequencyAnalysis(FrequencyAnalysisFrame.AnalysisType.FFT);
		} else if (e.getSource() == menuItemPowerSpectrum) {
			doFrequencyAnalysis(FrequencyAnalysisFrame.AnalysisType.SPECTRUM);
		} else if (e.getSource() == menuItemReloadPorts) {
			setupAvailableCommPorts(true);
		} else if (e.getSource() == menuItemFilterConfig) {
			showFilterConfigurationDialog();
		} else if (e.getSource() == menuItemExport) {
			String fileName = UiUtils.showCsvSaveDialog();
            controller.exportData(fileName, new CsvDataStorage());
        }
	}

	@Override
	public void onRawDataAvailable(String line) {
		textAreaConsole.append(line + "\n");
		textAreaConsole.setCaretPosition(textAreaConsole.getDocument().getLength());
	}

	@Override
	public void onChanneledDataAvailable(ChannelData channelData) {
	    visual.update(channelData, controller.getFilters());
	}

	@Override
	public void onSamplingFrequencySelected(double frequency) {
        controller.setSamplingFrequency(frequency);
		updateStatus(true);
	}

	@Override
	public void onVisualMaxEntered(double max) {
		visual.setYMaximum(max);
	}

    @Override
    public void onSourcesChanged() {
        setupSimulationSourceMenu();
    }
}
