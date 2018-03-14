package at.fhooe.mc.emg.clientdriver.model.writable

/**
 * Simple interface to indicate, that the implementing class is able to convert its data into a csv format.
 */
interface Csvable {

    /**
     * Converts the data content of the implementing class into a csv-compatible format.
     *
     * @param excelCompat Indicates if the output should be excel compatible (enter an additional line at the beginning).
     * @return A string representation in a valid csv format.
     */
    fun asCsv(excelCompat: Boolean = true): String

}