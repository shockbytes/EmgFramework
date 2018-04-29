package at.fhooe.mc.emg.desktop.misc

import at.fhooe.mc.emg.core.misc.RawDataLog
import at.fhooe.mc.emg.designer.ComponentViewType
import at.fhooe.mc.emg.designer.EmgComponentType
import at.fhooe.mc.emg.designer.annotation.EmgComponent
import at.fhooe.mc.emg.designer.annotation.EmgComponentInputPort
import at.fhooe.mc.emg.designer.annotation.EmgComponentPlatformView
import javax.swing.JComponent
import javax.swing.JScrollPane
import javax.swing.JTextArea

@EmgComponent(type = EmgComponentType.SINK)
class DesktopRawDataLog : RawDataLog<JComponent> {

    private val textAreaConsole = JTextArea()

    init {
        textAreaConsole.isEditable = false
    }

    @EmgComponentPlatformView(viewType = ComponentViewType.DESKTOP, requestedWidth = 200)
    override val view: JComponent = JScrollPane(textAreaConsole)

    @EmgComponentInputPort(String::class)
    override fun update(data: String) {
        textAreaConsole.append("$data\n")
        textAreaConsole.caretPosition = textAreaConsole.document?.length ?: 0
    }

    override fun clear() {
        textAreaConsole.text = ""
    }

}