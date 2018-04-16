package at.fhooe.mc.emg.desktop.ui

import at.fhooe.mc.emg.clientdriver.EmgClientDriver
import at.fhooe.mc.emg.core.EmgPresenter
import at.fhooe.mc.emg.core.analysis.FrequencyAnalysisMethod
import at.fhooe.mc.emg.core.filter.Filter
import at.fhooe.mc.emg.core.storage.CsvDataStorage
import at.fhooe.mc.emg.core.tools.Tool
import at.fhooe.mc.emg.core.util.EmgConfig
import at.fhooe.mc.emg.core.view.EmgViewCallback
import at.fhooe.mc.emg.core.view.VisualView
import at.fhooe.mc.emg.desktop.ui.dialog.FilterConfigDialog
import at.fhooe.mc.emg.desktop.ui.dialog.SamplingFrequencyDialog
import at.fhooe.mc.emg.desktop.ui.dialog.VisualYMaxDialog
import at.fhooe.mc.emg.desktop.view.DesktopEmgView
import io.reactivex.Observable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Toolkit
import java.awt.event.*
import java.io.File
import javax.swing.*
import javax.swing.border.EmptyBorder

class DesktopMainWindow : JFrame(), DesktopEmgView<JComponent>, ActionListener {

    private lateinit var labelStatus: JLabel
    private lateinit var textAreaConsole: JTextArea

    private lateinit var splitPane: JSplitPane

    private lateinit var menuItemExit: JMenuItem
    private lateinit var menuItemReset: JMenuItem
    private lateinit var menuItemExport: JMenuItem
    private lateinit var menuItemConnect: JMenuItem
    private lateinit var menuItemVisualMax: JMenuItem
    private lateinit var menuItemDisconnect: JMenuItem
    private lateinit var menuItemAcqDesigner: JMenuItem
    private lateinit var menuItemFilterConfig: JMenuItem
    private lateinit var menuItemSamplingFrequency: JMenuItem
    private lateinit var menuItemOpenAcqDesignerFile: JMenuItem

    private lateinit var cbMenuItemLogging: JCheckBoxMenuItem
    private lateinit var cbMenuItemEnableVisual: JCheckBoxMenuItem
    private lateinit var cbMenuItemCopyToSimulation: JCheckBoxMenuItem

    private lateinit var mnAnalysisMethods: JMenu
    private lateinit var menuFilter: JMenu
    private lateinit var menuClient: JMenu
    private lateinit var mnClients: JMenu
    private lateinit var mnTools: JMenu

    // --------------------------------------------------

    private lateinit var viewCallback: EmgViewCallback

    private lateinit var config: EmgConfig

    private lateinit var visualView: VisualView<JComponent>

    init {
        iconImage = Toolkit.getDefaultToolkit()
                .getImage(System.getProperty("user.dir") + "/desktop/icons/ic_launcher.png")
        initialize()
    }

    private fun initialize() {
        isResizable = true
        setBounds(100, 150, 650, 350)
        title = "Emg Desktop"
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
        splitPane.resizeWeight = 0.4
        contentPane.add(splitPane, BorderLayout.CENTER)

        labelStatus = JLabel()
        labelStatus.isOpaque = true
        labelStatus.background = Color.decode("#2196F3")
        labelStatus.foreground = Color.WHITE
        labelStatus.border = EmptyBorder(4, 4, 4, 4)
        contentPane.add(labelStatus, BorderLayout.SOUTH)

        textAreaConsole = JTextArea()
        textAreaConsole.isEditable = false
        splitPane.leftComponent = JScrollPane(textAreaConsole)

        updateStatus("Not connected")
    }

    private fun initializeMenu() {

        val menuBar = JMenuBar()
        jMenuBar = menuBar

        val mnGen = JMenu("General")
        menuBar.add(mnGen)

        cbMenuItemLogging = JCheckBoxMenuItem("Write file on disconnect")
        cbMenuItemLogging.isSelected = true
        cbMenuItemLogging.addActionListener(this)
        mnGen.add(cbMenuItemLogging)

        cbMenuItemCopyToSimulation = JCheckBoxMenuItem("Copy to simulation")
        cbMenuItemCopyToSimulation.addActionListener(this)
        cbMenuItemCopyToSimulation.isSelected = true
        mnGen.add(cbMenuItemCopyToSimulation)

        cbMenuItemEnableVisual = JCheckBoxMenuItem("Enable VisualView")
        cbMenuItemEnableVisual.addActionListener(this)
        cbMenuItemEnableVisual.isSelected = true
        mnGen.add(cbMenuItemEnableVisual)

        menuItemExport = JMenuItem("Export")
        menuItemExport.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK)
        menuItemExport.addActionListener(this)
        mnGen.add(menuItemExport)

        menuItemReset = JMenuItem("Reset")
        menuItemReset.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK)
        menuItemReset.addActionListener(this)
        mnGen.add(menuItemReset)

        menuItemExit = JMenuItem("Exit")
        menuItemExit.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK)
        menuItemExit.addActionListener(this)
        mnGen.add(menuItemExit)

        menuClient = JMenu("Client")
        menuBar.add(menuClient)

        mnClients = JMenu("Clients")
        menuClient.add(mnClients)
        menuClient.add(JSeparator())

        menuItemConnect = JMenuItem("Connect")
        menuItemConnect.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK + InputEvent.SHIFT_MASK)
        menuItemConnect.addActionListener(this)
        menuClient.add(menuItemConnect)

        menuItemDisconnect = JMenuItem("Disconnect")
        menuItemDisconnect.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK)
        menuItemDisconnect.addActionListener(this)
        menuClient.add(menuItemDisconnect)

        menuItemSamplingFrequency = JMenuItem("Sampling Frequency")
        menuItemSamplingFrequency.addActionListener(this)
        menuClient.add(menuItemSamplingFrequency)

        menuClient.add(JSeparator())

        menuFilter = JMenu("Filter")
        menuBar.add(menuFilter)

        menuItemFilterConfig = JMenuItem("Filter configuration")
        menuItemFilterConfig.addActionListener(this)
        menuFilter.add(menuItemFilterConfig)

        val separator1 = JSeparator()
        menuFilter.add(separator1)

        mnAnalysisMethods = JMenu("Frequency Analysis")
        menuBar.add(mnAnalysisMethods)

        mnTools = JMenu("Tools")
        menuBar.add(mnTools)

        val mnAcqCaseDesigner = JMenu("Acquisition Case Designer")
        menuBar.add(mnAcqCaseDesigner)

        menuItemAcqDesigner = JMenuItem("Open Acquisition Designer")
        menuItemAcqDesigner.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK)
        menuItemAcqDesigner.addActionListener(this)
        mnAcqCaseDesigner.add(menuItemAcqDesigner)

        menuItemOpenAcqDesignerFile = JMenuItem("Open acd file")
        menuItemOpenAcqDesignerFile.addActionListener(this)
        mnAcqCaseDesigner.add(menuItemOpenAcqDesignerFile)

        val mnDebug = JMenu("Debug")
        menuBar.add(mnDebug)

        menuItemVisualMax = JMenuItem("Visual max Y")
        menuItemVisualMax.addActionListener(this)
        mnDebug.add(menuItemVisualMax)

        // Disable all controls until connection is established
        lockDeviceControls(false)
    }

    // ---------------------------------------------------------------

    // ------------------------- Menu actions ------------------------

    override fun reset() {

        synchronized(this) {
            textAreaConsole.text = ""
            splitPane.remove(2)
            visualView.reset()
            splitPane.rightComponent = visualView.view
        }
    }

    private fun disconnectFromDevice() {

        var logFilename: String? = null
        if (cbMenuItemLogging.isSelected && viewCallback.isDataStorageEnabled()) {
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
            e.source === cbMenuItemLogging -> config.isWriteToLogEnabled = cbMenuItemLogging.isSelected
            e.source === cbMenuItemEnableVisual -> viewCallback.setVisualViewEnabled(cbMenuItemEnableVisual.isSelected)
            e.source === cbMenuItemCopyToSimulation -> config.isCopyToSimulationEnabled = cbMenuItemCopyToSimulation.isSelected
            e.source === menuItemReset -> reset()
            e.source === menuItemFilterConfig -> showFilterConfigurationDialog()
            e.source === menuItemAcqDesigner -> viewCallback.openAcquisitionCaseDesigner()
            e.source === menuItemOpenAcqDesignerFile -> {
                val fileName = UiUtils.showAcdOpenDialog()
                if (fileName != null) {
                    viewCallback.openAcquisitionCaseDesignerFile(File(fileName))
                }
            }
            e.source === menuItemExport -> {
                val fileName = UiUtils.showCsvSaveDialog()
                if (fileName != null) {
                    viewCallback.exportData(fileName, CsvDataStorage())
                }
            }
        }
    }

    override fun lockDeviceControls(isLocked: Boolean) {

        menuItemDisconnect.isEnabled = isLocked
        menuItemSamplingFrequency.isEnabled = isLocked

        // Logic is in reverse for connection and channels
        menuFilter.isEnabled = !isLocked
        mnClients.isEnabled = !isLocked
        menuItemConnect.isEnabled = !isLocked
        cbMenuItemEnableVisual.isEnabled = !isLocked
    }

    override fun updateStatus(status: String) {
        labelStatus.text = status
    }

    override fun exposeRawClientDataObservable(observable: Observable<String>) {
        observable.subscribeOn(Schedulers.io()).subscribe {
            textAreaConsole.append("$it\n")
            textAreaConsole.caretPosition = textAreaConsole.document?.length ?: 0
        }
    }

    override fun setupToolsView(tools: List<Tool>, presenter: EmgPresenter) {

        mnTools.removeAll()
        tools.forEach { t ->
            val item = JMenuItem(t.name)
            item.addActionListener { t.start(presenter, true) }
            mnTools.add(item)
        }
    }

    override fun setupFilterViews(filter: List<Filter>) {

        filter.forEach { f ->
            val item = JCheckBoxMenuItem(f.name)
            item.addItemListener { f.isEnabled = it.stateChange == ItemEvent.SELECTED }
            menuFilter.add(item)
        }
        menuFilter.getItem(2)?.isSelected = true
    }

    override fun setupEmgClientDriverView(clients: List<EmgClientDriver>, defaultClient: EmgClientDriver) {

        mnClients.removeAll()
        clients.forEach { c ->

            // Setup chooser for available clients
            val item = JCheckBoxMenuItem(c.shortName)
            item.isSelected = c === defaultClient // Select default client
            item.addActionListener {
                mnClients.menuComponents?.forEach {
                    val otherItem = it as JCheckBoxMenuItem
                    if (otherItem !== item) {
                        otherItem.isSelected = false
                    }
                    viewCallback.setSelectedClient(c)
                }
            }
            mnClients.add(item)
        }
    }

    override fun setupEmgClientDriverConfigViews(clients: List<EmgClientDriver>) {

        clients.forEach { c ->
            if (c.hasConfigView) {
                val item = JMenuItem(c.configView?.name)
                item.addActionListener {
                    c.configView?.show(c)
                }
                menuClient.add(item)
            }
        }
    }

    override fun setupFrequencyAnalysisMethods(methods: List<FrequencyAnalysisMethod>) {
        methods.forEach { m ->
            val item = JMenuItem(m.name)
            item.addActionListener {
                viewCallback.requestFrequencyAnalysisView(m)
            }
            mnAnalysisMethods.add(item)
        }
    }

    override fun setupView(viewCallback: EmgViewCallback, config: EmgConfig) {
        this.viewCallback = viewCallback
        this.config = config

        // Initialize checkboxes from stored config
        cbMenuItemLogging.isSelected = config.isWriteToLogEnabled
        cbMenuItemCopyToSimulation.isSelected = config.isCopyToSimulationEnabled
    }

    override fun showFrequencyAnalysisView(method: FrequencyAnalysisMethod, data: DoubleArray, fs: Double) {
        val window = if (method.hasDisplay) FrequencyAnalysisFrame() else null
        // TODO Better callback handling!
        method.calculate(data, fs, window).subscribe(Consumer {
            println(it)
        })
    }

    override fun showConnectionError(throwable: Throwable) {
        val msg = "${throwable.javaClass.simpleName}: ${throwable.localizedMessage}"
        UiUtils.showErrorMessage(this, msg, "ClientDriver connection error")
    }

    override fun setVisualView(view: VisualView<JComponent>) {
        visualView = view
        splitPane.rightComponent = visualView.view
    }
}
