package at.fhooe.mc.emg.designer.component.pipe

import kotlin.reflect.KClass

interface EmgComponentPipe<in K, out V> {

    val ports: Pair<KClass<*>, KClass<*>>

    val name: String

    fun pipe(arg: K): V
}