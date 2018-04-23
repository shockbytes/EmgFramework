package at.fhooe.mc.emg.designer.annotation

import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
annotation class EmgComponentOutputPort(val produces: KClass<*>)