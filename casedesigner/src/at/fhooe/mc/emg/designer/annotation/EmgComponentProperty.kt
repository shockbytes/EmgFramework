package at.fhooe.mc.emg.designer.annotation

@Target(AnnotationTarget.FIELD)
annotation class EmgComponentProperty(val defaultValue: String, val displayName: String)