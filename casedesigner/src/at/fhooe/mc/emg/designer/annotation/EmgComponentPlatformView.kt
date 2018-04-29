package at.fhooe.mc.emg.designer.annotation

import at.fhooe.mc.emg.designer.ComponentViewType

@Target(AnnotationTarget.FIELD)
annotation class EmgComponentPlatformView(val viewType: ComponentViewType, val requestedWidth: Int)