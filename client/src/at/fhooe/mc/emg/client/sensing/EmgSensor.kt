package at.fhooe.mc.emg.client.sensing

/**
 * @author Martin Macheiner
 * Date: 24.01.2018.
 *
 * Main interface for reading emg data from the hardware
 *
 */
interface EmgSensor {

    /**
     * Each sensor must be capable of providing an arbitrary amount
     * of channels. Usually this will be 1-2
     *
     * @return a list of double values for every channel
     */
    fun provideEmgValues(): List<Double>

    fun setup()

    fun tearDown()

}