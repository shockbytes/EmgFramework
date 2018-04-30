package at.fhooe.mc.emg.core.storage

import at.fhooe.mc.emg.clientdriver.model.EmgData
import java.io.BufferedWriter
import java.io.FileWriter
import java.io.IOException
import java.io.PrintWriter

class CsvEmgDataStorage : EmgDataStorage {

    override fun store(path: String, data: EmgData): Boolean {
        return try {
            PrintWriter(BufferedWriter(FileWriter(path))).use { writer -> writer.println(data.asCsv()) }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

}
