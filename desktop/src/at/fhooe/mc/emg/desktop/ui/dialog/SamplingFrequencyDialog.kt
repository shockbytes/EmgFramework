package at.fhooe.mc.emg.desktop.ui.dialog

import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.border.TitledBorder
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

class SamplingFrequencyDialog(private val listener: OnSamplingFrequencySelectedListener?,
                              mainWindow: JFrame) : JDialog(), ActionListener {

    interface OnSamplingFrequencySelectedListener {
        fun onSamplingFrequencySelected(frequency: Double)
    }

    private val doneButton: JButton
    private val textFrequency: JTextField

    private val samplingFrequency: Double
        get() {

            val text = textFrequency.text
            return try {
                java.lang.Double.parseDouble(text)
            } catch (e: Exception) {
                e.printStackTrace()
                (-1).toDouble()
            }
        }

    init {
        setIconImage(Toolkit.getDefaultToolkit()
                .getImage(System.getProperty("user.dir") + "/desktop/icons/ic_dialog_fs.png"))

        val width = 200
        val height = 120
        setBounds(mainWindow.x + mainWindow.width / 2 - width / 2,
                mainWindow.y + mainWindow.height / 2 - height / 2,
                width,
                height)
        defaultCloseOperation = JDialog.DISPOSE_ON_CLOSE
        isResizable = false
        title = "Sampling frequency"
        contentPane.layout = BorderLayout()

        val contentPanel = JPanel()
        contentPanel.layout = BorderLayout()
        contentPanel.border = EmptyBorder(8, 8, 8, 8)

        doneButton = JButton("Set frequency")
        doneButton.addActionListener(this)

        contentPane.add(contentPanel, BorderLayout.CENTER)

        textFrequency = JTextField()
        textFrequency.addActionListener(this)
        textFrequency.border = TitledBorder(null, "Frequency in Hz",
                TitledBorder.LEADING, TitledBorder.TOP, null, null)
        contentPanel.add(textFrequency, BorderLayout.CENTER)
        textFrequency.columns = 10
        contentPane.add(doneButton, BorderLayout.SOUTH)
    }

    override fun actionPerformed(e: ActionEvent) {

        if (e.source === doneButton) {
            validateData()
        } else if (e.source === textFrequency) {
            validateData()
        }
    }

    private fun validateData() {
        val fs = samplingFrequency
        if (fs > 0) {
            if (listener != null) {
                listener.onSamplingFrequencySelected(fs)
                dispose()
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Please enter a number bigger than 0.0", "Parse error", JOptionPane.ERROR_MESSAGE)
        }
    }

    companion object {

        fun show(listener: OnSamplingFrequencySelectedListener, mainWindow: JFrame) {
            val dialog = SamplingFrequencyDialog(listener, mainWindow)
            dialog.isVisible = true
        }
    }

}
