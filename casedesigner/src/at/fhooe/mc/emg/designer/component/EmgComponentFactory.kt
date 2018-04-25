package at.fhooe.mc.emg.designer.component

import at.fhooe.mc.emg.designer.EmgComponentType
import at.fhooe.mc.emg.designer.component.util.EmgComponentParameter


class EmgComponentFactory {

    companion object {

        fun byType(name: String, qualifiedName: String,
                   parameter: List<EmgComponentParameter>, type: EmgComponentType): EmgBaseComponent {
            return when (type) {
                EmgComponentType.FILTER -> EmgFilterComponent(name, qualifiedName, parameter)
                EmgComponentType.SINK -> EmgSinkComponent(name, qualifiedName, parameter)
                EmgComponentType.RELAY_SINK -> EmgRelaySinkComponent(name, qualifiedName, parameter)
                EmgComponentType.TOOL -> EmgToolComponent(name, qualifiedName, parameter)
                EmgComponentType.DEVICE -> EmgDeviceComponent(name, qualifiedName, parameter)
                EmgComponentType.RELAY -> EmgRelayComponent(name, qualifiedName, parameter)
            }
        }
    }
}