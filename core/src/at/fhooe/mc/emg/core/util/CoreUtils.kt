package at.fhooe.mc.emg.core.util

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.io.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.regex.Pattern

object CoreUtils {

    private val macAddressRegex = Pattern.compile("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})\$")

    fun validateMacAddress(mac: String): Boolean {
        return macAddressRegex.matcher(mac).matches()
    }

    fun roundDouble(value: Double, digits: Int): Double {

        if (value == 0.0 || digits < 0 || value == Double.NaN) {
            return 0.00
        }
        return BigDecimal(value).setScale(digits, RoundingMode.HALF_UP).toDouble()
    }

    @Throws(IOException::class)
    fun writeFile(file: File, text: String) {
        BufferedWriter(FileWriter(file)).use { writer -> writer.write(text) }
    }

    @Throws(IOException::class)
    fun <T : Serializable> serializeToFile(obj: T?, filename: String): Boolean {
        return if (obj != null) {
            val oos = ObjectOutputStream(FileOutputStream(filename))
            oos.writeObject(obj)
            oos.close()
            true
        } else {
            false
        }
    }

    fun readFile(file: File): Single<String> {
        return Single.fromCallable {
            file.bufferedReader().useLines { it.joinToString("\n") }
        }.subscribeOn(Schedulers.io())
    }

    fun readFileLines(file: File): Single<Sequence<String>> {
        return Single.fromCallable {
            file.bufferedReader().lineSequence()
        }.subscribeOn(Schedulers.io())
    }

    @Throws(Exception::class)
    inline fun <reified T> deserializeFromFile(filename: String): T {

        ObjectInputStream(FileInputStream(filename)).use {
            val obj = it.readObject()
            if (obj is T) {
                return obj
            }
        }
        throw IOException("Cannot read value from file or invalid cast of ${T::class.java.simpleName}")
    }

    @Throws(Exception::class)
    @Suppress("UNCHECKED_CAST")
    fun <T> unsafeDeserializeFromFile(filename: String): T? {
        ObjectInputStream(FileInputStream(filename)).use {
            return it.readObject() as? T
        }
    }

}
