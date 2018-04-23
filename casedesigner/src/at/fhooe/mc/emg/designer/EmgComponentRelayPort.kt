package at.fhooe.mc.emg.designer

import kotlin.reflect.KClass

@Target(AnnotationTarget.FUNCTION)
annotation class EmgComponentRelayPort(val produces: KClass<*>, val consumes: KClass<*>)