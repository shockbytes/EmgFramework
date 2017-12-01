package at.fhooe.mc.emg.core.util.config

import at.fhooe.mc.emg.core.util.AppUtils
import com.google.gson.Gson
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Author:  Mescht
 * Date:    01.12.2017
 */
class JsonEmgConfigStorage(private val file: File) : EmgConfigStorage {

    private val gson: Gson = Gson()

    override fun load(): EmgConfig {

        try {

            if (!file.exists()) {
                return EmgConfig() // File does not exist yet, return default instance
            }

            val jsonString = Files.readAllLines(Paths.get(file.absolutePath)).joinToString("\n")
            return gson.fromJson(jsonString, EmgConfig::class.java)

        } catch (e: Exception) {
            println(e.message)
        }
        return EmgConfig() // Return default values if it cannot be loaded
    }

    override fun store(config: EmgConfig) {

        try {

            if (!file.exists()) {
                file.createNewFile()
            }

            AppUtils.writeFile(file, gson.toJson(config))

        } catch (e: IOException) {
            e.printStackTrace()
        }

    }


}