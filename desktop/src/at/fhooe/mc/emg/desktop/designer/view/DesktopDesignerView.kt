package at.fhooe.mc.emg.desktop.designer.view

import at.fhooe.mc.emg.designer.DesignerViewCallback
import at.fhooe.mc.emg.designer.component.EmgBaseComponent
import at.fhooe.mc.emg.designer.view.DesignerView
import at.fhooe.mc.emg.desktop.designer.DesktopDesignerHelper
import at.fhooe.mc.emg.desktop.designer.util.DragDropTransferHandler
import at.fhooe.mc.emg.desktop.ui.UiUtils
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.File
import javax.swing.*
import javax.swing.border.CompoundBorder
import javax.swing.border.EmptyBorder
import javax.swing.border.TitledBorder


/**
 * Author:  Martin Macheiner
 * Date:    16.04.2018
 *
 * TODO Line Connect functionality
 *
 */
class DesktopDesignerView : DesignerView {

    override var components: List<EmgBaseComponent> = listOf()
        set(value) {
            miniMap.miniMapComponents = value
            componentInteractionView.interactionComponents = value
        }

    private val bgColor = Color.decode("#0017a5")

    private var viewCallback: DesignerViewCallback? = null

    private lateinit var labelMsg: JLabel
    private lateinit var contentPanel: JPanel
    private lateinit var componentPanel: JPanel
    private lateinit var miniMap: DesktopMiniMap
    private lateinit var componentInteractionView: DesktopComponentInteractionView

    override fun show(viewCallback: DesignerViewCallback, components: List<EmgBaseComponent>) {
        this.viewCallback = viewCallback

        val frame = wrap()
        frame.addWindowListener(object : WindowAdapter() {
            override fun windowClosed(e: WindowEvent?) {
                super.windowClosed(e)
                viewCallback.stop()
            }
        })
        frame.isVisible = true

        setAvailableComponents(components)
    }

    override fun askForStorageBeforeQuit(): File? {

        val answer = JOptionPane.showConfirmDialog(contentPanel, "Save acquisition case?", "Save",
                JOptionPane.YES_NO_OPTION)

        return if (answer == JOptionPane.YES_OPTION) {
            val fileName = UiUtils.showAcdSaveDialog() ?: return null
            File(fileName)
        } else {
            null
        }
    }

    override fun showStatusMessage(msg: String) {
        labelMsg.text = "Messages: $msg"
    }

    private fun wrap(): JFrame {
        val frame = JFrame()
        frame.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
        frame.title = "Acquisition Case Designer"
        frame.iconImage = Toolkit.getDefaultToolkit()
                .getImage(System.getProperty("user.dir") + "/desktop/icons/ic_case_designer.png")
        frame.setBounds(250, 100, 1000, 600)

        contentPanel = JPanel(BorderLayout())
        contentPanel.background = bgColor
        contentPanel.add(sideBarPanel(), BorderLayout.EAST)
        contentPanel.add(componentInteractionView(), BorderLayout.CENTER)

        labelMsg = JLabel("Messages: Working")
        labelMsg.foreground = Color.WHITE
        labelMsg.border = EmptyBorder(4, 8, 4, 4)
        contentPanel.add(labelMsg, BorderLayout.SOUTH)

        frame.contentPane = contentPanel
        frame.jMenuBar = menuBar()
        return frame
    }

    private fun menuBar(): JMenuBar {
        val menuBar = JMenuBar()

        val mnFile = JMenu("File")
        val mnItemOpen = JMenuItem("Open")
        mnItemOpen.addActionListener {
            val fileName = UiUtils.showAcdOpenDialog()
            if (fileName != null) {
                viewCallback?.open(File(fileName))
            }
        }
        mnFile.add(mnItemOpen)
        val mnItemSave = JMenuItem("Save")
        mnItemSave.addActionListener {
            val fileName = UiUtils.showAcdSaveDialog()
            if (fileName != null) {
                viewCallback?.save(File(fileName))
            }
        }
        mnFile.add(mnItemSave)
        val mnItemReset = JMenuItem("Reset")
        mnItemReset.addActionListener { viewCallback?.reset() }
        mnFile.add(mnItemReset)

        val mnBuild = JMenu("Build")
        val mnItemRun = JMenuItem("Run")
        mnItemRun.addActionListener {
            viewCallback?.run()
        }
        mnBuild.add(mnItemRun)

        menuBar.add(mnFile)
        menuBar.add(mnBuild)
        return menuBar
    }

    private fun sideBarPanel(): JComponent {

        val sideBar = Box.createVerticalBox()
        sideBar.background = bgColor
        sideBar.add(miniMapPanel())

        componentPanel = JPanel(GridBagLayout())
        componentPanel.background = bgColor
        componentPanel.border = CompoundBorder(null, TitledBorder(UIManager.getBorder("TitledBorder.border"),
                "Components", TitledBorder.LEADING, TitledBorder.TOP, null, Color.WHITE))
        sideBar.add(JScrollPane(componentPanel))
        return sideBar
    }

    private fun miniMapPanel(): JPanel {

        val overviewRootPanel = JPanel()
        overviewRootPanel.background = bgColor
        overviewRootPanel.border = CompoundBorder(null, TitledBorder(UIManager.getBorder("TitledBorder.border"),
                "MiniMap", TitledBorder.LEADING, TitledBorder.TOP, null, Color.WHITE))
        overviewRootPanel.layout = BorderLayout()

        val screen = Toolkit.getDefaultToolkit().screenSize
        val ratio = screen.getWidth() / screen.getHeight()
        val width = (screen.getWidth() / SIDEBAR_SCALE_FACTOR).toInt()
        val height = (width / ratio).toInt()
        overviewRootPanel.preferredSize = Dimension(width, height)
        overviewRootPanel.maximumSize = Dimension(width, height)

        miniMap = DesktopMiniMap(SIDEBAR_SCALE_FACTOR)
        overviewRootPanel.add(miniMap)
        return overviewRootPanel
    }

    private fun componentInteractionView(): JComponent {
        componentInteractionView = DesktopComponentInteractionView()
        componentInteractionView.background = bgColor
        componentInteractionView.setup(viewCallback)
        return componentInteractionView
    }

    private fun setAvailableComponents(components: List<EmgBaseComponent>) {
        val constraints = componentConstraints()
        components.forEach { component ->
            componentPanel.add(labelFromComponent(component), constraints.clone())
        }
    }

    private fun componentConstraints(): GridBagConstraints {
        val constraints = GridBagConstraints()
        constraints.fill = GridBagConstraints.HORIZONTAL
        constraints.gridwidth = GridBagConstraints.REMAINDER
        constraints.weightx = 1.0
        constraints.gridx = 0
        return constraints
    }

    private fun labelFromComponent(component: EmgBaseComponent): JLabel {
        val label = JLabel(component.name, DesktopDesignerHelper.componentIcon(component), SwingConstants.LEFT)
        label.border = EmptyBorder(8, 4, 8, 4)
        label.foreground = Color.WHITE
        label.transferHandler = DragDropTransferHandler(component)

        label.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent?) {
                super.mousePressed(e)

                if (e?.button == MouseEvent.BUTTON3) {
                    // Add by right click
                    viewCallback?.addComponentByDoubleClick(component)
                } else {
                    // Add by drag
                    label.transferHandler.exportAsDrag(label, e, TransferHandler.MOVE)
                }
            }
        })
        return label
    }

    companion object {
        const val SIDEBAR_SCALE_FACTOR = 6
    }

}