package at.fhooe.mc.emg.desktop.ui.dialog

import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.border.TitledBorder
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

class VisualYMaxDialog private constructor(private val listener: OnVisualMaxEnteredListener?,
                                           mainWindow: JFrame) : JDialog(), ActionListener {

    private val doneButton: JButton
    private val textMax: JTextField

    private val value: Double
        get() {
            val text = textMax.text
            return try {
                Integer.parseInt(text).toDouble()
            } catch (e: Exception) {
                e.printStackTrace()
                (-1).toDouble()
            }
        }

    interface OnVisualMaxEnteredListener {

        fun onVisualMaxEntered(max: Double)
    }

    init {
        setIconImage(Toolkit.getDefaultToolkit()
                .getImage(System.getProperty("user.dir") + "/desktop/icons/ic_dialog_graph_max.png"))
        val width = 200
        val height = 120
        setBounds(mainWindow.x + mainWindow.width / 2 - width / 2,
                mainWindow.y + mainWindow.height / 2 - height / 2,
                width,
                height)
        defaultCloseOperation = JDialog.DISPOSE_ON_CLOSE
        isResizable = false
        title = "Maximum visualView value"
        contentPane.layout = BorderLayout()

        val contentPanel = JPanel()
        contentPanel.layout = BorderLayout()
        contentPanel.border = EmptyBorder(8, 8, 8, 8)

        doneButton = JButton("Set visualView max")
        doneButton.addActionListener(this)

        contentPane.add(contentPanel, BorderLayout.CENTER)

        textMax = JTextField()
        textMax.border = TitledBorder(null, "Y-Axis maximum", TitledBorder.LEADING, TitledBorder.TOP,
                null, null)
        textMax.addActionListener(this)
        contentPanel.add(textMax, BorderLayout.CENTER)
        textMax.columns = 10
        contentPane.add(doneButton, BorderLayout.SOUTH)
    }

    override fun actionPerformed(e: ActionEvent) {

        if (e.source === doneButton) {
            validateData()
        } else if (e.source === textMax) {
            validateData()
        }
    }

    private fun validateData() {

        val max = value
        if (max > 0) {
            if (listener != null) {
                listener.onVisualMaxEntered(max)
                dispose()
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Please enter a number bigger than 0.0", "Parse error", JOptionPane.ERROR_MESSAGE)
        }
    }

    companion object {

        fun show(listener: OnVisualMaxEnteredListener, mainWindow: JFrame) {
            val dialog = VisualYMaxDialog(listener, mainWindow)
            dialog.isVisible = true
        }
    }

}
