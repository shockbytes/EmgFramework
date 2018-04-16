package at.fhooe.mc.emg.core.storage.config

import at.fhooe.mc.emg.core.util.CoreUtils
import at.fhooe.mc.emg.core.util.EmgConfig
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

    override val emgConfig: EmgConfig by lazy {

        var config = EmgConfig()  // Return default values if it cannot be loaded
        try {
            if (file.exists()) {
                val jsonString = Files.readAllLines(Paths.get(file.absolutePath)).joinToString("\n")
                config = gson.fromJson(jsonString, EmgConfig::class.java)
            }
        } catch (e: Exception) {
            println(e.message)
        }
        config
    }

    override fun store(config: EmgConfig) {

        try {

            if (!file.exists()) {
                file.createNewFile()
            }

            println(gson.toJson(config))
            CoreUtils.writeFile(file, gson.toJson(config))

        } catch (e: IOException) {
            e.printStackTrace()
        }

    }


}