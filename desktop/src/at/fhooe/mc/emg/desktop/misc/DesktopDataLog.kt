package at.fhooe.mc.emg.desktop.misc

import at.fhooe.mc.emg.core.misc.DataLog
import at.fhooe.mc.emg.designer.ComponentViewType
import at.fhooe.mc.emg.designer.EmgComponentType
import at.fhooe.mc.emg.designer.annotation.EmgComponent
import at.fhooe.mc.emg.designer.annotation.EmgComponentInputPort
import at.fhooe.mc.emg.designer.annotation.EmgComponentPlatformView
import at.fhooe.mc.emg.designer.annotation.EmgComponentProperty
import javax.swing.JComponent
import javax.swing.JScrollPane
import javax.swing.JTextArea

@EmgComponent(type = EmgComponentType.SINK, displayTitle = "Data Logging View")
class DesktopDataLog : DataLog<JComponent> {

    private val textAreaConsole = JTextArea()

    private var counter = 0

    init {
        textAreaConsole.isEditable = false
    }

    @EmgComponentPlatformView(viewType = ComponentViewType.DESKTOP, requestedWidth = 300)
    override val view: JComponent = JScrollPane(textAreaConsole)

    @JvmField
    @EmgComponentProperty("5000", "Display cache")
    var cacheThreshold: Int = 5000

    @EmgComponentInputPort(String::class)
    override fun update(data: String) {

        checkClearance()

        textAreaConsole.append("$data\n")
        textAreaConsole.caretPosition = textAreaConsole.document?.length ?: 0
    }

    override fun clear() {
        textAreaConsole.text = ""
    }

    private fun checkClearance() {
        counter++
        if (counter > cacheThreshold) {
            clear()
            counter = 0
        }
    }

}