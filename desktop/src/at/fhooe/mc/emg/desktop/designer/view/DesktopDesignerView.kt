package at.fhooe.mc.emg.desktop.designer.view

import at.fhooe.mc.emg.designer.component.EmgBaseComponent
import at.fhooe.mc.emg.designer.view.DesignerView
import at.fhooe.mc.emg.desktop.designer.DesktopDesignerHelper
import java.awt.*
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*
import javax.swing.border.CompoundBorder
import javax.swing.border.EmptyBorder
import javax.swing.border.TitledBorder




/**
 * Author:  Martin Macheiner
 * Date:    16.04.2018
 */
class DesktopDesignerView: DesignerView {

    private val bgColor =  Color.decode("#0017a5")

    private lateinit var contentPanel: JPanel
    private lateinit var componentPanel: JPanel
    private lateinit var miniMap: DesktopMiniMap
    private lateinit var componentInteractionView: DesktopComponentInteractionView

    override fun show(components: List<EmgBaseComponent>) {
        val frame = wrap()
        frame.addWindowListener(object : WindowAdapter() {
            override fun windowClosed(e: WindowEvent?) {
                super.windowClosed(e)
                // TODO Fire callback for closing window
            }
        })
        frame.isVisible = true

        setComponents(components)
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
        componentInteractionView = DesktopComponentInteractionView()
        componentInteractionView.background = bgColor
        contentPanel.add(componentInteractionView, BorderLayout.CENTER)

        frame.contentPane = contentPanel
        return frame
    }

    private fun sideBarPanel(): Component {

        val sideBar = Box.createVerticalBox()
        sideBar.background = bgColor
        sideBar.add(initializeMiniMapPanel())

        componentPanel = JPanel(GridBagLayout())
        componentPanel.background = bgColor
        componentPanel.border = CompoundBorder(null, TitledBorder(UIManager.getBorder("TitledBorder.border"),
                "Components", TitledBorder.LEADING, TitledBorder.TOP, null, Color.WHITE))
        sideBar.add(JScrollPane(componentPanel))
        return sideBar
    }

    private fun initializeMiniMapPanel(): JPanel {

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

    private fun setComponents(components: List<EmgBaseComponent>) {

        val constraints = componentConstraints()
        components.forEach {
            val l = JLabel(it.name, DesktopDesignerHelper.componentIcon(it), SwingConstants.LEFT)
            l.border = EmptyBorder(8,4,8,4)
            l.foreground = Color.WHITE
            // TODO Add drag and drop functionality
            componentPanel.add(l, constraints.clone())
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

    companion object {
        const val SIDEBAR_SCALE_FACTOR = 6
    }

}