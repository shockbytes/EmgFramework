package at.fhooe.mc.emg.client

/**
 * Author:  Mescht
 * Date:    26.11.2017
 */

// TODO Rework this
interface EmgClientConfigView {

    fun<T> getView(): T

    fun<C> setup(client: C)
}