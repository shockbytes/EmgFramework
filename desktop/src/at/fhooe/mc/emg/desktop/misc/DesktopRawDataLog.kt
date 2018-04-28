package at.fhooe.mc.emg.desktop.misc

import at.fhooe.mc.emg.core.misc.RawDataLog
import at.fhooe.mc.emg.designer.EmgComponentType
import at.fhooe.mc.emg.designer.ViewType
import at.fhooe.mc.emg.designer.annotation.EmgComponent
import at.fhooe.mc.emg.designer.annotation.EmgComponentInputPort
import at.fhooe.mc.emg.designer.annotation.EmgComponentPlatformView
import javax.swing.JScrollPane
import javax.swing.JTextArea

@EmgComponent(type = EmgComponentType.SINK)
class DesktopRawDataLog: RawDataLog<JScrollPane> {

    private val textAreaConsole = JTextArea()

    init {
        textAreaConsole.isEditable = false
    }


    @EmgComponentPlatformView(ViewType.DESKTOP)
    override val view: JScrollPane
        get() = JScrollPane(textAreaConsole)

    @EmgComponentInputPort(String::class)
    override fun update(data: String) {
        textAreaConsole.append("$data\n")
        textAreaConsole.caretPosition = textAreaConsole.document?.length ?: 0
    }

    override fun clear() {
        textAreaConsole.text = ""
    }

}