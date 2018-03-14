package at.fhooe.mc.emg.client.sensing

/**
 * @author  Martin Macheiner
 * Date:    24.01.2018.
 *
 * Main interface for reading emg data from the hardware. Each sensor must be capable of providing an arbitrary amount
 * of channels. Usually this will be 1-2 utilized channels. Therefore the method #provideEmgValues() returns a list
 * of double values, instead of one value.
 *
 */
interface EmgSensor {

    /**
     * Expose the current available data to the EmgClient.
     *
     * @return a list of double values representing the different channels.
     */
    fun provideEmgValues(): List<Double>

    /**
     * Setup the underlying hardware in order to measure EMG data.
     */
    fun setup()

    /**
     * Tear down all used resources associated with the data acquisition.
     */
    fun tearDown()

}