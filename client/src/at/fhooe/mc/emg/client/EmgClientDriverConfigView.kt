package at.fhooe.mc.emg.client

/**
 * Author:  Mescht
 * Date:    26.11.2017
 */

interface EmgClientDriverConfigView {

    val name: String

    fun show(client: EmgClientDriver)

}