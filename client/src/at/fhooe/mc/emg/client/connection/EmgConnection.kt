package at.fhooe.mc.emg.client.connection

import io.reactivex.Flowable
import io.reactivex.functions.Consumer

/**
 * @author  Martin Macheiner
 * Date:    24.01.2018.
 */

interface EmgConnection {

    fun sendMessage(msg: String)

    fun setup(successHandler: Consumer<String>? = null, errorHandler: Consumer<Throwable>? = null)

    fun subscribeToIncomingMessages(): Flowable<String>

    fun cleanupAfterDisconnect()

    fun tearDown()

}