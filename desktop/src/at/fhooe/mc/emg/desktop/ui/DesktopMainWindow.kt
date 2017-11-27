package at.fhooe.mc.emg.desktop.ui

import at.fhooe.mc.emg.client.ChannelData
import at.fhooe.mc.emg.client.EmgClient
import at.fhooe.mc.emg.client.network.NetworkClient
import at.fhooe.mc.emg.client.simulation.SimulationClient
import at.fhooe.mc.emg.core.EmgController
import at.fhooe.mc.emg.desktop.client.serial.SerialClient
import at.fhooe.mc.emg.desktop.ui.dialog.FilterConfigDialog
import at.fhooe.mc.emg.desktop.ui.dialog.SamplingFrequencyDialog
import at.fhooe.mc.emg.desktop.ui.dialog.VisualYMaxDialog
import at.fhooe.mc.emg.desktop.view.DesktopEmgView
import at.fhooe.mc.emg.filter.Filter
import at.fhooe.mc.emg.storage.CsvDataStorage
import at.fhooe.mc.emg.tools.Tool
import at.fhooe.mc.emg.util.Configuration
import at.fhooe.mc.emg.util.FrequencyAnalysis
import at.fhooe.mc.emg.view.EmgViewCallback
import at.fhooe.mc.emg.view.VisualView
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Toolkit
import java.awt.event.*
import javax.swing.*
import javax.swing.border.EmptyBorder

class DesktopMainWindow : JFrame(), DesktopEmgView<JComponent>, ActionListener {

    private var labelStatus: JLabel? = null
    private var textAreaConsole: JTextArea? = null

    private var splitPane: JSplitPane? = null

    private var menuItemExit: JMenuItem? = null
    private var menuItemFft: JMenuItem? = null
    private var menuItemReset: JMenuItem? = null
    private var menuItemExport: JMenuItem? = null
    private var menuItemConnect: JMenuItem? = null
    private var menuItemVisualMax: JMenuItem? = null
    private var menuItemDisconnect: JMenuItem? = null
    private var menuItemReloadPorts: JMenuItem? = null
    private var menuItemFilterConfig: JMenuItem? = null
    private var menuItemPowerSpectrum: JMenuItem? = null
    private var menuItemSamplingFrequency: JMenuItem? = null

    private var cbMenuItemLogging: JCheckBoxMenuItem? = null
    private var cbMenuItemPlaybackLoop: JCheckBoxMenuItem? = null
    private var cbMenuItemCopyToSimulation: JCheckBoxMenuItem? = null

    private var menuPorts: JMenu? = null
    private var mnChannel: JMenu? = null
    private var mnClients: JMenu? = null
    private var mnDataRate: JMenu? = null
    private var mnTools: JMenu? = null
    private var mnSimulationData: JMenu? = null

    // --------------------------------------------------

    private lateinit var viewCallback: EmgViewCallback

    private lateinit var config: Configuration

    private lateinit var visualView: VisualView<JComponent>

    init {
        iconImage = Toolkit.getDefaultToolkit().getImage(System.getProperty("user.dir") + "/icons/ic_main.jpg")
        initialize()
    }

    private fun initialize() {
        isResizable = true
        setBounds(100, 150, 450, 300)
        title = "Emg Desktop v1.0"
        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        addWindowListener(object : WindowAdapter() {

            override fun windowClosing(e: WindowEvent?) {
                super.windowClosing(e)
                viewCallback.closeView(config)
            }
        })

        initializeMenu()
        initializeViews()

        isVisible = true
    }

    // --------------- Initialization & setup methods ----------------
    private fun initializeViews() {

        contentPane.background = Color.WHITE

        splitPane = JSplitPane()
        splitPane?.resizeWeight = 0.3
        contentPane.add(splitPane, BorderLayout.CENTER)

        labelStatus = JLabel()
        labelStatus?.border = EmptyBorder(4, 4, 4, 4)
        contentPane.add(labelStatus, BorderLayout.SOUTH)

        textAreaConsole = JTextArea()
        textAreaConsole?.isEditable = false
        splitPane?.leftComponent = JScrollPane(textAreaConsole)

        updateStatus("Status: Not connected")
    }

    private fun initializeMenu() {

        val menuBar = JMenuBar()
        jMenuBar = menuBar

        val mnFile = JMenu("File")
        menuBar.add(mnFile)

        cbMenuItemLogging = JCheckBoxMenuItem("Write file on disconnect")
        cbMenuItemLogging?.isSelected = true
        cbMenuItemLogging?.addActionListener(this)
        mnFile.add(cbMenuItemLogging)

        cbMenuItemCopyToSimulation = JCheckBoxMenuItem("Copy to simulation")
        cbMenuItemCopyToSimulation?.addActionListener(this)
        cbMenuItemCopyToSimulation?.isSelected = true
        mnFile.add(cbMenuItemCopyToSimulation)

        menuItemExport = JMenuItem("Export")
        menuItemExport?.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK)
        menuItemExport?.addActionListener(this)
        mnFile.add(menuItemExport)

        menuItemReset = JMenuItem("Reset")
        menuItemReset?.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK)
        menuItemReset?.addActionListener(this)
        mnFile.add(menuItemReset)

        menuItemExit = JMenuItem("Exit")
        menuItemExit?.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK)
        menuItemExit?.addActionListener(this)
        mnFile.add(menuItemExit)

        val menuClient = JMenu("Client")
        menuBar.add(menuClient)

        mnClients = JMenu("Clients")
        menuClient.add(mnClients)
        menuClient.add(JSeparator())

        menuItemConnect = JMenuItem("Connect")
        menuItemConnect?.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_F10, InputEvent.SHIFT_MASK)
        menuItemConnect?.addActionListener(this)
        menuClient.add(menuItemConnect)

        menuItemDisconnect = JMenuItem("Disconnect")
        menuItemDisconnect?.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK)
        menuItemDisconnect?.addActionListener(this)
        menuClient.add(menuItemDisconnect)

        menuItemSamplingFrequency = JMenuItem("Sampling Frequency")
        menuItemSamplingFrequency?.addActionListener(this)
        menuClient.add(menuItemSamplingFrequency)

        menuClient.add(JSeparator())

        mnDataRate = JMenu("Data rate")

        menuPorts = JMenu("Ports")
        menuClient.add(menuPorts)

        menuItemReloadPorts = JMenuItem("Reload")
        menuItemReloadPorts?.addActionListener(this)

        menuClient.add(mnDataRate)

        mnChannel = JMenu("Channel")
        menuBar.add(mnChannel)

        menuItemFilterConfig = JMenuItem("Configuration")
        menuItemFilterConfig?.addActionListener(this)
        mnChannel?.add(menuItemFilterConfig)

        val separator1 = JSeparator()
        mnChannel?.add(separator1)

        val mnSimulation = JMenu("Simulation")
        menuBar.add(mnSimulation)

        mnSimulationData = JMenu("Sources")
        mnSimulation.add(mnSimulationData)

        cbMenuItemPlaybackLoop = JCheckBoxMenuItem("Playback loop")
        cbMenuItemPlaybackLoop?.addActionListener(this)
        mnSimulation.add(cbMenuItemPlaybackLoop)

        val mnAnalysis = JMenu("Analysis")
        menuBar.add(mnAnalysis)

        menuItemFft = JMenuItem("FFT")
        menuItemFft?.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK)
        menuItemFft?.addActionListener(this)
        mnAnalysis.add(menuItemFft)

        menuItemPowerSpectrum = JMenuItem("Power Spectrum")
        menuItemPowerSpectrum?.addActionListener(this)
        menuItemPowerSpectrum?.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK)
        mnAnalysis.add(menuItemPowerSpectrum)

        mnTools = JMenu("Tools")
        menuBar.add(mnTools)

        val mnDebug = JMenu("Debug")
        menuBar.add(mnDebug)

        menuItemVisualMax = JMenuItem("Visual max Y")
        menuItemVisualMax?.addActionListener(this)
        mnDebug.add(menuItemVisualMax)

        // Disable all controls until connection is established
        setDeviceControlsEnabled(false)
    }

    private fun setupAvailableCommPorts(client: SerialClient, forceUpdate: Boolean) {

        menuPorts?.removeAll()
        menuPorts?.add(menuItemReloadPorts)
        menuPorts?.add(JSeparator())

        client.getAvailablePortNames(forceUpdate).forEach { s ->
            val item = JCheckBoxMenuItem(s)
            item.addActionListener { client.setSerialPortSelected(item.text) }
            menuPorts?.add(item)
        }

        // Select the first port
        if (menuPorts!!.itemCount > 2) {
            menuPorts!!.getItem(2).isSelected = true
            client.setSerialPortSelected(menuPorts!!.getItem(2).text)
        }
    }

    private fun setupDataRateMenu(client: SerialClient) {
        SerialClient.supportedDataRates.forEach {
            val item = JCheckBoxMenuItem(it.toString())
            item.addActionListener {
                mnDataRate?.menuComponents?.forEach {
                    val otherItem = it as JCheckBoxMenuItem
                    if (otherItem !== item) {
                        otherItem.isSelected = false
                    }
                }
                val rate = Integer.parseInt(item.text)
                client.dataRate = rate
            }

            if (it == SerialClient.defaultDataRate) {
                item.isSelected = true
            }
            mnDataRate?.add(item)
        }
    }

    // ---------------------------------------------------------------

    // ------------------------- Menu actions ------------------------

    override fun reset() {

        synchronized(this) {

            textAreaConsole?.text = ""
            splitPane?.remove(2)

            visualView.reset()
            splitPane?.rightComponent = visualView.view
        }
    }

    private fun disconnectFromDevice() {

        var logFilename: String? = null
        if (cbMenuItemLogging?.isSelected!!) {
            logFilename = UiUtils.showCsvSaveDialog()
        }
        viewCallback.disconnectFromClient(logFilename)
    }

    private fun showSamplingFrequencyDialog() {
        SamplingFrequencyDialog.show({ frequency ->
            viewCallback.setSamplingFrequency(frequency)
        }, this)
    }

    private fun showFilterConfigurationDialog() {
        val dialog = FilterConfigDialog(config, this)
        dialog.isVisible = true
    }

    private fun showVisualMaxDialog() {
        VisualYMaxDialog.show({ max ->
            visualView.setYMaximum(max)
        }, this)
    }

    // ---------------------------------------------------------------

    override fun actionPerformed(e: ActionEvent) {

        when {
            e.source === menuItemConnect -> viewCallback.connectToClient()
            e.source === menuItemDisconnect -> disconnectFromDevice()
            e.source === menuItemExit -> System.exit(0)
            e.source === menuItemSamplingFrequency -> showSamplingFrequencyDialog()
            e.source === menuItemVisualMax -> showVisualMaxDialog()
            e.source === cbMenuItemLogging -> config.isWriteToLogEnabled = cbMenuItemLogging?.isSelected!!
            e.source === cbMenuItemCopyToSimulation -> config.isCopyToSimulationEnabled = cbMenuItemCopyToSimulation?.isSelected!!
            e.source === cbMenuItemPlaybackLoop -> { viewCallback.setSimulationPlaybackLoopEnabled(cbMenuItemPlaybackLoop?.isSelected!!) }
            e.source === menuItemReset -> reset()
            e.source === menuItemFft -> viewCallback.requestFrequencyAnalysis(FrequencyAnalysis.AnalysisType.FFT)
            e.source === menuItemPowerSpectrum -> viewCallback.requestFrequencyAnalysis(FrequencyAnalysis.AnalysisType.SPECTRUM)
            e.source === menuItemReloadPorts -> {
                // TODO This should not be necessary, because it will be replaced by a ConfigView anyway
                // viewCallback.reloadSerialPorts();
            }
            e.source === menuItemFilterConfig -> showFilterConfigurationDialog()
            e.source === menuItemExport -> {
                val fileName = UiUtils.showCsvSaveDialog()
                viewCallback.exportData(fileName, CsvDataStorage())
            }
        }
    }

    override fun setDeviceControlsEnabled(isEnabled: Boolean) {

        mnDataRate?.isEnabled = isEnabled
        menuItemDisconnect?.isEnabled = isEnabled
        menuItemSamplingFrequency?.isEnabled = isEnabled

        // Logic is in reverse for connection and channels
        mnChannel?.isEnabled = !isEnabled
        mnClients?.isEnabled = !isEnabled
        menuItemConnect?.isEnabled = !isEnabled
    }

    override fun setupSerialClientView(client: SerialClient) {
        setupAvailableCommPorts(client, false)
        setupDataRateMenu(client)
    }

    override fun updateStatus(status: String) {
        labelStatus?.text = status
    }

    override fun onRawClientDataAvailable(raw: String) {
        textAreaConsole?.append(raw + "\n")
        textAreaConsole?.caretPosition = textAreaConsole!!.document.length
    }

    override fun onChanneledClientDataAvailable(cd: ChannelData, filters: List<Filter>) {
        visualView.update(cd, filters)
    }

    override fun setupSimulationClientView(client: SimulationClient) {

        mnSimulationData?.removeAll()
        client.simulationSources.forEach { src ->

            val item = JCheckBoxMenuItem(src.name)
            item.addActionListener {
                mnSimulationData?.menuComponents?.forEach {
                    val otherItem = it as JCheckBoxMenuItem
                    if (otherItem !== item) {
                        otherItem.isSelected = false
                    }
                }
                client.simulationSource = src
            }
            mnSimulationData?.add(item)
        }

        if (client.simulationSources.isNotEmpty()) {
            mnSimulationData?.getItem(client.simulationSources.size / 2 + 1)?.doClick()
        }
    }

    override fun setupToolsView(tools: List<Tool>, controller: EmgController) {

        mnTools?.removeAll()
        tools.forEach { t ->
            val item = JMenuItem(t.name)
            item.addActionListener { t.start(controller) }
            mnTools?.add(item)
        }
    }

    override fun setupFilterViews(filter: List<Filter>) {

        filter.forEach { f ->
            val item = JCheckBoxMenuItem(f.name)
            item.addItemListener { f.isEnabled = it.stateChange == ItemEvent.SELECTED }
            mnChannel?.add(item)
        }
        mnChannel?.getItem(2)?.isSelected = true
    }

    override fun setupEmgClientView(clients: List<EmgClient>, defaultClient: EmgClient) {

        mnClients?.removeAll()
        clients.forEach { c ->

            // Setup chooser for available clients
            val item = JCheckBoxMenuItem(c.shortName)
            item.isSelected = c === defaultClient // Select default client
            item.addActionListener {
                mnClients?.menuComponents?.forEach {
                    val otherItem = it as JCheckBoxMenuItem
                    if (otherItem !== item) {
                        otherItem.isSelected = false
                    }
                    viewCallback.setSelectedClient(c)
                }
            }
            mnClients?.add(item)

            // TODO Setup client config views
        }
    }

    override fun setup(viewCallback: EmgViewCallback, config: Configuration) {
        this.viewCallback = viewCallback
        this.config = config

        // Initialize checkboxes from stored config
        cbMenuItemLogging?.isSelected = config.isWriteToLogEnabled
        cbMenuItemCopyToSimulation?.isSelected = config.isCopyToSimulationEnabled
        cbMenuItemPlaybackLoop?.isSelected = config.isSimulationEndlessLoopEnabled
    }

    override fun setupNetworkClientView(client: NetworkClient) {
        // TODO Show config view with ip and port (Possibly not needed)
    }

    override fun showFrequencyAnalysis(type: FrequencyAnalysis.AnalysisType, fs: Double) {
        FrequencyAnalysisFrame.show(type, visualView.dataForFrequencyAnalysis, fs, this)
    }

    override fun setVisualView(view: VisualView<JComponent>) {
        visualView = view
        visualView.initialize()
        splitPane?.rightComponent = visualView.view
    }
}
