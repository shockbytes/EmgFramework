package at.fhooe.mc.emg.desktop.storage

import at.fhooe.mc.emg.core.storage.FileStorage
import at.fhooe.mc.emg.core.util.CoreUtils
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.Serializable
import java.nio.file.Files
import java.nio.file.Paths

class DesktopFileStorage : FileStorage {

    /**
     * @param concatToBase is ignored, because Desktop searches everywhere and
     * has no single directory scope
     */
    override fun listFiles(directory: String, concatToBase: Boolean, fileType: String?): Single<List<String>?> {
        return Single.fromCallable {
            val f = File(directory)
            if (f.exists()) {
                f.list { _, name -> if(fileType != null) name.endsWith(fileType) else true }.toList()
            } else {
                null
            }
        }.subscribeOn(Schedulers.io())
    }

    override fun storeFile(fileName: String, content: String): Completable {
        return Completable.fromAction {
            CoreUtils.writeFile(File(fileName), content)
        }.subscribeOn(Schedulers.io())
    }

    override fun <T : Serializable> storeFileAsObject(obj: T, fileName: String): Completable {
        return Completable.fromAction {
            CoreUtils.serializeToFile(obj, fileName)
        }.subscribeOn(Schedulers.io())
    }

    override fun loadFromFileAsString(fileName: String): Single<String?> {
        return Single.fromCallable {
            Files.readAllLines(Paths.get(fileName)).joinToString(System.lineSeparator())
        }.subscribeOn(Schedulers.io())
    }

    override fun <T> loadFromFileAsObject(filename: String): Single<T?> {
        return Single.fromCallable {
            CoreUtils.unsafeDeserializeFromFile<T>(filename)
        }.subscribeOn(Schedulers.io())
    }

}