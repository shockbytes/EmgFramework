package at.fhooe.mc.emg.desktop.ui

import at.fhooe.mc.emg.core.analysis.FrequencyAnalysisMethod
import at.fhooe.mc.emg.clientdriver.ChannelData
import at.fhooe.mc.emg.clientdriver.EmgClientDriver
import at.fhooe.mc.emg.core.EmgController
import at.fhooe.mc.emg.desktop.ui.dialog.FilterConfigDialog
import at.fhooe.mc.emg.desktop.ui.dialog.SamplingFrequencyDialog
import at.fhooe.mc.emg.desktop.ui.dialog.VisualYMaxDialog
import at.fhooe.mc.emg.desktop.view.DesktopEmgView
import at.fhooe.mc.emg.core.filter.Filter
import at.fhooe.mc.emg.core.storage.CsvDataStorage
import at.fhooe.mc.emg.core.tools.Tool
import at.fhooe.mc.emg.core.util.Configuration
import at.fhooe.mc.emg.core.view.EmgViewCallback
import at.fhooe.mc.emg.core.view.VisualView
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
    private var menuItemFilterConfig: JMenuItem? = null
    private var menuItemPowerSpectrum: JMenuItem? = null
    private var menuItemSamplingFrequency: JMenuItem? = null

    private var cbMenuItemLogging: JCheckBoxMenuItem? = null
    private var cbMenuItemCopyToSimulation: JCheckBoxMenuItem? = null

    private var menuFilter: JMenu? = null
    private var menuClient: JMenu? = null
    private var mnClients: JMenu? = null
    private var mnTools: JMenu? = null

    // --------------------------------------------------

    private lateinit var viewCallback: EmgViewCallback

    private lateinit var config: Configuration

    private lateinit var visualView: VisualView<JComponent>

    init {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        iconImage = Toolkit.getDefaultToolkit()
                .getImage(System.getProperty("user.dir") + "/desktop/icons/ic_main.jpg")
        initialize()
    }

    private fun initialize() {
        isResizable = true
        setBounds(100, 150, 450, 300)
        title = "Emg Desktop v0.9"
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

    // --------------- Initialization & show methods ----------------
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

        menuClient = JMenu("Client")
        menuBar.add(menuClient)

        mnClients = JMenu("Clients")
        menuClient?.add(mnClients)
        menuClient?.add(JSeparator())

        menuItemConnect = JMenuItem("Connect")
        menuItemConnect?.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_F10, InputEvent.SHIFT_MASK)
        menuItemConnect?.addActionListener(this)
        menuClient?.add(menuItemConnect)

        menuItemDisconnect = JMenuItem("Disconnect")
        menuItemDisconnect?.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK)
        menuItemDisconnect?.addActionListener(this)
        menuClient?.add(menuItemDisconnect)

        menuItemSamplingFrequency = JMenuItem("Sampling Frequency")
        menuItemSamplingFrequency?.addActionListener(this)
        menuClient?.add(menuItemSamplingFrequency)

        menuClient?.add(JSeparator())

        menuFilter = JMenu("Filter")
        menuBar.add(menuFilter)

        menuItemFilterConfig = JMenuItem("Configuration")
        menuItemFilterConfig?.addActionListener(this)
        menuFilter?.add(menuItemFilterConfig)

        val separator1 = JSeparator()
        menuFilter?.add(separator1)

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
            e.source === menuItemReset -> reset()
            e.source === menuItemFft -> viewCallback.requestFrequencyAnalysis(FrequencyAnalysisMethod.Method.FFT)
            e.source === menuItemPowerSpectrum -> viewCallback.requestFrequencyAnalysis(FrequencyAnalysisMethod.Method.SPECTRUM)
            e.source === menuItemFilterConfig -> showFilterConfigurationDialog()
            e.source === menuItemExport -> {
                val fileName = UiUtils.showCsvSaveDialog()
                viewCallback.exportData(fileName, CsvDataStorage())
            }
        }
    }

    override fun setDeviceControlsEnabled(isEnabled: Boolean) {

        menuItemDisconnect?.isEnabled = isEnabled
        menuItemSamplingFrequency?.isEnabled = isEnabled

        // Logic is in reverse for connection and channels
        menuFilter?.isEnabled = !isEnabled
        mnClients?.isEnabled = !isEnabled
        menuItemConnect?.isEnabled = !isEnabled
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
            menuFilter?.add(item)
        }
        menuFilter?.getItem(2)?.isSelected = true
    }

    override fun setupEmgClientDriverView(clients: List<EmgClientDriver>, defaultClient: EmgClientDriver) {

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
        }
    }

    override fun setupEmgClientDriverConfigViews(clients: List<EmgClientDriver>) {

        clients.forEach { c ->
            if (c.hasConfigView) {
                val item = JMenuItem(c.configView?.name)
                item.addActionListener {
                    c.configView?.show(c)
                }
                menuClient?.add(item)
            }
        }
    }


    override fun setupView(viewCallback: EmgViewCallback, config: Configuration) {
        this.viewCallback = viewCallback
        this.config = config

        // Initialize checkboxes from stored config
        cbMenuItemLogging?.isSelected = config.isWriteToLogEnabled
        cbMenuItemCopyToSimulation?.isSelected = config.isCopyToSimulationEnabled
    }

    override fun showFrequencyAnalysis(method: FrequencyAnalysisMethod) {
        method.evaluate(FrequencyAnalysisFrame())
    }

    override fun setVisualView(view: VisualView<JComponent>) {
        visualView = view
        visualView.initialize()
        splitPane?.rightComponent = visualView.view
    }
}
