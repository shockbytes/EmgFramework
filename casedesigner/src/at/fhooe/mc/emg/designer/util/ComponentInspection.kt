package at.fhooe.mc.emg.designer.util

import at.fhooe.mc.emg.designer.ComponentLogic
import at.fhooe.mc.emg.designer.ComponentViewType
import at.fhooe.mc.emg.designer.annotation.*
import at.fhooe.mc.emg.designer.component.EmgBaseComponent
import io.reactivex.subjects.PublishSubject
import org.reflections.ReflectionUtils
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import kotlin.reflect.KClass

/**
 * This object class handles all the heavy reflection stuff in order to create a single source of truth for
 * reflection related tasks.
 */
object ComponentInspection {

    /**
     *
     * The method extracts the consumer type, producer type and the usage of a relay port from the given component
     *
     * @param c Component of interest
     *
     * @return a Triple of format (consumes, produces, isRelayPort)
     */
    fun getPortConnectivityInformation(c: EmgBaseComponent): Triple<KClass<*>?, KClass<*>?, Boolean> {

        val clazz = Class.forName(c.qualifiedName)
        val inputPort = ReflectionUtils.getMethods(clazz,
                ReflectionUtils.withAnnotation(EmgComponentInputPort::class.java)).firstOrNull()
        var outputPort = ReflectionUtils.getFields(clazz,
                ReflectionUtils.withAnnotation(EmgComponentOutputPort::class.java)).firstOrNull()
        val relayPort = ReflectionUtils.getMethods(clazz,
                ReflectionUtils.withAnnotation(EmgComponentRelayPort::class.java)).firstOrNull()

        // Check in super class if in concrete class no output port is defined
        if (outputPort == null && relayPort == null) {
            outputPort = ReflectionUtils.getSuperTypes(Class.forName(c.qualifiedName))
                    .mapNotNull { cls ->
                        ReflectionUtils
                                .getFields(cls, ReflectionUtils.withAnnotation(EmgComponentOutputPort::class.java))
                                .firstOrNull()
                    }.firstOrNull()
        }

        // Extra check if output port has the right signature
        if (outputPort != null) {
            if (Modifier.isPrivate(outputPort.modifiers) || (outputPort.type == PublishSubject::class)) {
                throw ComponentLogic.ValidationException("Output port in ${outputPort.declaringClass} is not from type PublishSubject or public")
            }
        }

        val consumes: KClass<*>?
        val produces: KClass<*>?
        if (relayPort != null) {
            val relay = relayPort.annotations
                    ?.find { a -> a.annotationClass == EmgComponentRelayPort::class }
                    .let { (it as? EmgComponentRelayPort) }
            consumes = relay?.consumes
            produces = relay?.produces
        } else {
            consumes = inputPort?.annotations
                    ?.find { a -> a.annotationClass == EmgComponentInputPort::class }
                    .let { (it as? EmgComponentInputPort)?.consumes }
            produces = outputPort?.annotations
                    ?.find { a -> a.annotationClass == EmgComponentOutputPort::class }
                    .let { (it as? EmgComponentOutputPort)?.produces }
        }
        return Triple(consumes, produces, relayPort != null)
    }

    fun getOutputPort(c: EmgBaseComponent): Field {

        val (_, _, isRelayPort) = getPortConnectivityInformation(c)
        return if (!isRelayPort) {
            // Check in super class if in concrete class no output port is defined
            ReflectionUtils.getFields(Class.forName(c.qualifiedName),
                    ReflectionUtils.withAnnotation(EmgComponentOutputPort::class.java)).firstOrNull()
                    ?: ReflectionUtils.getSuperTypes(Class.forName(c.qualifiedName))
                            .mapNotNull { cls ->
                                ReflectionUtils
                                        .getFields(cls, ReflectionUtils.withAnnotation(EmgComponentOutputPort::class.java))
                                        .firstOrNull()
                            }.first()
        } else {
            throw IllegalAccessException("Relay port as output port is not supported, must be squashed inside next consumer")
        }
    }

    fun getInputPort(c: EmgBaseComponent): Method {

        val (_, _, isRelayPort) = getPortConnectivityInformation(c)
        return if (isRelayPort) {
            ReflectionUtils.getMethods(Class.forName(c.qualifiedName),
                    ReflectionUtils.withAnnotation(EmgComponentRelayPort::class.java)).first()
        } else {
            ReflectionUtils.getMethods(Class.forName(c.qualifiedName),
                    ReflectionUtils.withAnnotation(EmgComponentInputPort::class.java)).first()
        }
    }

    fun getDeviceEntryPoint(c: EmgBaseComponent): Method {
        return ReflectionUtils.getMethods(Class.forName(c.qualifiedName),
                ReflectionUtils.withAnnotation(EmgComponentEntryPoint::class.java)).first()
    }

    fun getDeviceExitPoint(c: EmgBaseComponent): Method {
        return ReflectionUtils.getMethods(Class.forName(c.qualifiedName),
                ReflectionUtils.withAnnotation(EmgComponentExitPoint::class.java)).first()
    }

    /**
     *
     * @return A pair containing a field and the requested width
     */
    fun getPlatformViewField(c: EmgBaseComponent, viewType: ComponentViewType): Pair<Field, Int>? {
        return ReflectionUtils.getFields(Class.forName(c.qualifiedName),
                ReflectionUtils.withAnnotation(EmgComponentPlatformView::class.java))
                .map { field ->
                    val viewAnnotation = field.annotations.mapNotNull { it as? EmgComponentPlatformView }.firstOrNull()
                    if (viewAnnotation?.viewType == viewType) {
                        field.isAccessible = true
                        Pair(field, viewAnnotation.requestedWidth)
                    } else {
                        null
                    }
                }.firstOrNull()
    }


    fun setProperty(instance: Any, fieldName: String, value: String, type: String) {
        instance.javaClass.getField(fieldName)?.set(instance, convertValueToType(value, type))
    }

    fun matchDataTypes(type: String, data: String): Boolean {
        return when (type.toLowerCase()) {
            "double" -> data.toDoubleOrNull() != null
            "int" -> data.toIntOrNull() != null
            "boolean" -> data.toBoolean()
            String::class.java.name.toLowerCase() -> true
            "float" -> data.toFloatOrNull() != null
            "byte" -> data.toByteOrNull() != null
            "long" -> data.toLongOrNull() != null
            else -> false
        }
    }

    private fun convertValueToType(value: String, type: String): Any {
        return when (type.toLowerCase()) {
            "double" -> value.toDouble()
            "int" -> value.toInt()
            "boolean" -> value.toBoolean()
            "float" -> value.toFloat()
            "byte" -> value.toByte()
            "long" -> value.toLong()
            else -> value
        }
    }

}