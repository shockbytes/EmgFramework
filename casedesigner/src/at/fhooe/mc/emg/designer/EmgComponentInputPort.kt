package at.fhooe.mc.emg.designer

import kotlin.reflect.KClass

@Target(AnnotationTarget.FUNCTION)
annotation class EmgComponentInputPort(val consumes: KClass<*>)