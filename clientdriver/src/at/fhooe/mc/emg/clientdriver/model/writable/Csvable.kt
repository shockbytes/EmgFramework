package at.fhooe.mc.emg.clientdriver.model.writable

interface Csvable {

    fun asCsv(excelCompat: Boolean = true): String
}