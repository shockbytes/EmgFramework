package at.fhooe.mc.emg.clientdriver

/**
 * Author:  Martin Macheiner
 * Date:    26.11.2017
 *
 * Each client driver has some configurations (like Ip address, MAC address or other settings). Some of those
 * won't change over time, but they change. EmgClientDriverConfigView allows each client driver to expose a UI where
 * those properties can be changed.
 */
interface EmgClientDriverConfigView {

    /**
     * Name of the view (show as title)
     */
    val name: String

    /**
     * Calling the show function means, that each client driver exposes itself to the configuration view. This
     * is not a really clean approach, but in order to shorten the development time it is a working solution.
     *
     * @param client EmgClientDriver, which exposes access to all non-private member variables.
     */
    fun show(client: EmgClientDriver)

}