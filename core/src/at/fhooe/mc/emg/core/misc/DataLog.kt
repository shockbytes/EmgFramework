package at.fhooe.mc.emg.core.misc

interface DataLog<T> {

    val view: T

    fun update(data: String)

    fun clear()
}