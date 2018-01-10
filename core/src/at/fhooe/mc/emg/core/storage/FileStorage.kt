package at.fhooe.mc.emg.core.storage

import io.reactivex.Completable
import io.reactivex.Single
import java.io.Serializable


interface FileStorage {

    val baseDirectory: String

    fun listFiles(directory: String, concatToBase: Boolean = false, fileType: String? = null): Single<List<String>?>

    fun storeFile(fileName: String, content: String): Completable

    fun<T : Serializable> storeFileAsObject(obj: T, fileName: String): Completable

    fun loadFromFileAsString(fileName: String): Single<String?>

    fun<T> loadFromFileAsObject(filename: String): Single<T?>

}