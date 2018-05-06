package at.fhooe.mc.emg.designer.component

import at.fhooe.mc.emg.designer.EmgComponentType
import at.fhooe.mc.emg.designer.component.util.EmgComponentParameter


class EmgComponentFactory {

    companion object {

        fun byType(name: String,
                   qualifiedName: String,
                   parameter: List<EmgComponentParameter>,
                   type: EmgComponentType,
                   displayTitle: String): EmgBaseComponent {
            return when (type) {
                EmgComponentType.FILTER -> EmgFilterComponent(name, qualifiedName, parameter, displayTitle)
                EmgComponentType.SINK -> EmgSinkComponent(name, qualifiedName, parameter, displayTitle)
                EmgComponentType.RELAY_SINK -> EmgRelaySinkComponent(name, qualifiedName, parameter, displayTitle)
                EmgComponentType.TOOL -> EmgToolComponent(name, qualifiedName, parameter, displayTitle)
                EmgComponentType.DEVICE -> EmgDeviceComponent(name, qualifiedName, parameter, displayTitle)
                EmgComponentType.RELAY -> EmgRelayComponent(name, qualifiedName, parameter, displayTitle)
            }
        }
    }
}