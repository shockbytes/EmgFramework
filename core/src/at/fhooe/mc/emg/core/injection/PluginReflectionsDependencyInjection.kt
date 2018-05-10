package at.fhooe.mc.emg.core.injection

import java.io.File
import java.net.URL
import java.net.URLClassLoader

/**
 * Author:  Martin Macheiner
 * Date:    10.05.2018
 *
 * Extension of BasicReflectionDependencyInjection. Basically provides the same functionality, but with the feature
 * of loading extensible classes (Filter, Tools, Driver, EmgComponents) from a separate deployed jar file.
 *
 */
class PluginReflectionsDependencyInjection(platformConfiguration: PlatformConfiguration, pluginDirectory: File)
    : BasicReflectionsDependencyInjection(platformConfiguration) {

    /**
     * Load the plugins at runtime into classpath, { @code BasicReflectionsDependencyInjection } will find classes
     * in class path
     */
    init {
        pluginDirectory.listFiles()
                .filter { it.isFile && it.extension == ".jar" }
                .forEach { jar -> loadLibrary(jar) }
    }

    /**
     * Adds the supplied Java Archive library to java.class.path. This is benign
     * if the library is already loaded.
     */
    @Throws(Exception::class)
    private fun loadLibrary(jar: File) {

        val loader = ClassLoader.getSystemClassLoader() as? URLClassLoader
                ?: throw ClassCastException("No UrlClassLoader!")
        val url = jar.toURI().toURL()

        // Disallow if already loaded
        if (loader.urLs.any { (it == url) }) {
            return
        }

        val method = URLClassLoader::class.java.getDeclaredMethod("addURL", URL::class.java)
        method.isAccessible = true /*promote the method to public access*/
        method.invoke(loader, url)
    }


}