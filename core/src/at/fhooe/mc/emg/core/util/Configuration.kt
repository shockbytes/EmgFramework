package at.fhooe.mc.emg.core.util

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors

object Configuration {

    private val KEY_WRITE_LOG = "write_log"
    private val KEY_COPY_TO_SIM = "copy_to_simulation"
    private val KEY_SIM_ENDLESS_LOOP = "simulation_endless_loop"
    private val KEY_RAVG_WS = "ravg_window_size"
    private val KEY_SG_WIDTH = "savitzky_golay_width"

    private val CONFIG_PATH = System.getProperty("user.dir") + "/data/config.json"

    var isWriteToLogEnabled: Boolean = false
    var isCopyToSimulationEnabled: Boolean = false
    var isSimulationEndlessLoopEnabled: Boolean = false

    var runningAverageWindowSize: Int = 0
    var savitzkyGolayFilterWidth: Int = 0

    init {
        load()
    }

    private fun load() {

        try {

            val file = File(CONFIG_PATH)
            if (!file.exists()) {
                file.createNewFile()
            }

            val strJson = Files.readAllLines(Paths.get(file.absolutePath))
                    .stream().collect(Collectors.joining("\n"))

            val element = JsonParser().parse(strJson).asJsonObject
            isWriteToLogEnabled = element.has(KEY_WRITE_LOG) && element.get(KEY_WRITE_LOG).asBoolean
            isCopyToSimulationEnabled = element.has(KEY_COPY_TO_SIM) && element.get(KEY_COPY_TO_SIM).asBoolean
            isSimulationEndlessLoopEnabled = element.has(KEY_SIM_ENDLESS_LOOP) && element.get(KEY_SIM_ENDLESS_LOOP).asBoolean
            runningAverageWindowSize = if (element.has(KEY_RAVG_WS)) element.get(KEY_RAVG_WS).asInt else 30
            savitzkyGolayFilterWidth = if (element.has(KEY_SG_WIDTH)) element.get(KEY_SG_WIDTH).asInt else 10

        } catch (e: Exception) {
            e.printStackTrace()
            isWriteToLogEnabled = false
            isCopyToSimulationEnabled = false
            isSimulationEndlessLoopEnabled = false
            runningAverageWindowSize = 30
            savitzkyGolayFilterWidth = 10
        }
    }

    fun save() {

        val element = JsonObject()
        element.addProperty(KEY_WRITE_LOG, isWriteToLogEnabled)
        element.addProperty(KEY_COPY_TO_SIM, isCopyToSimulationEnabled)
        element.addProperty(KEY_SIM_ENDLESS_LOOP, isSimulationEndlessLoopEnabled)
        element.addProperty(KEY_RAVG_WS, runningAverageWindowSize)
        element.addProperty(KEY_SG_WIDTH, savitzkyGolayFilterWidth)

        try {
            AppUtils.writeFile(File(CONFIG_PATH), element.toString())
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}
