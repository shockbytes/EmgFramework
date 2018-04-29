package at.fhooe.mc.emg.designer.annotation

import kotlin.reflect.KClass

@Target(AnnotationTarget.FUNCTION)
annotation class EmgComponentStartablePoint(val viewProperty: String, val viewClass: KClass<*>)