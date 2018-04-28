package at.fhooe.mc.emg.core.misc

interface RawDataLog<T> {

    val view: T

    fun update(data: String)

    fun clear()
}