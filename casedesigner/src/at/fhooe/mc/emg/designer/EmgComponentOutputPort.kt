package at.fhooe.mc.emg.designer

import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
annotation class EmgComponentOutputPort(val produces: KClass<*>)