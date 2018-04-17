package at.fhooe.mc.emg.designer.component

import at.fhooe.mc.emg.designer.EmgComponentType


class EmgComponentFactory {

    companion object {

        fun byType(name: String, qualifiedName: String, type: EmgComponentType): EmgBaseComponent {
            return when (type) {
                EmgComponentType.FILTER -> EmgFilterComponent(name, qualifiedName)
                EmgComponentType.SOURCE -> EmgSourceComponent(name, qualifiedName)
                EmgComponentType.SINK -> EmgSinkComponent(name, qualifiedName)
                EmgComponentType.RELAY_SINK -> EmgRelaySinkComponent(name, qualifiedName)
                EmgComponentType.TOOL -> EmgToolComponent(name, qualifiedName)
                EmgComponentType.DEVICE -> EmgDeviceComponent(name, qualifiedName)
            }
        }
    }
}