package at.fhooe.mc.emg.desktop.view

import at.fhooe.mc.emg.designer.component.EmgBaseComponent
import at.fhooe.mc.emg.designer.component.util.EmgComponentParameter
import at.fhooe.mc.emg.designer.view.ComponentPropertyView
import java.awt.GridLayout
import javax.swing.*
import javax.swing.border.EmptyBorder

class DesktopComponentPropertyView : ComponentPropertyView {

    override fun showPropertyView(component: EmgBaseComponent,
                                  point: Pair<Int, Int>,
                                  callback: ((EmgBaseComponent, EmgComponentParameter, String) -> Boolean)) {

        val frame = JFrame(component.name)
        frame.setBounds(point.first, point.second, 300, 300)
        frame.defaultCloseOperation = WindowConstants.HIDE_ON_CLOSE

        val panel = JPanel(GridLayout((2 * component.parameter.size) + 1, 1, 4, 4))
        panel.border = EmptyBorder(4, 4, 4, 4)
        val labelStatus = JLabel("")

        component.parameter.forEach { property ->
            panel.add(JLabel(property.name))
            val textField = JTextField(property.value)
            textField.addActionListener {
                val isValid = callback.invoke(component, property, textField.text)
                if (isValid) {
                    labelStatus.text = "${property.name} changed to ${textField.text}"
                } else {
                    labelStatus.text = "Invalid input for ${property.name}!"
                }
            }
            panel.add(textField)
        }
        panel.add(labelStatus)

        frame.contentPane = panel
        frame.isVisible = true
    }
}