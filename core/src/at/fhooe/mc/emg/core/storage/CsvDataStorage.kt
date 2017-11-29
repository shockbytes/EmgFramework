package at.fhooe.mc.emg.core.storage

import at.fhooe.mc.emg.clientdriver.ChannelData
import java.io.BufferedWriter
import java.io.FileWriter
import java.io.IOException
import java.io.PrintWriter

class CsvDataStorage : DataStorage {

    override fun store(path: String, data: ChannelData): Boolean {

        try {
            PrintWriter(BufferedWriter(FileWriter(path))).use { writer -> writer.println(data.csvLogOutput) }
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }

        return true
    }

}
