package at.fhooe.mc.emg.designer.component

import at.fhooe.mc.emg.designer.EmgComponentType
import at.fhooe.mc.emg.designer.component.util.EmgComponentParameter


class EmgComponentFactory {

    companion object {

        fun byType(name: String, qualifiedName: String,
                   parameters: List<EmgComponentParameter>, type: EmgComponentType): EmgBaseComponent {
            return when (type) {
                EmgComponentType.FILTER -> EmgFilterComponent(name, qualifiedName, parameters)
                EmgComponentType.SOURCE -> EmgSourceComponent(name, qualifiedName, parameters)
                EmgComponentType.SINK -> EmgSinkComponent(name, qualifiedName, parameters)
                EmgComponentType.RELAY_SINK -> EmgRelaySinkComponent(name, qualifiedName, parameters)
                EmgComponentType.TOOL -> EmgToolComponent(name, qualifiedName, parameters)
                EmgComponentType.DEVICE -> EmgDeviceComponent(name, qualifiedName, parameters)
            }
        }
    }
}