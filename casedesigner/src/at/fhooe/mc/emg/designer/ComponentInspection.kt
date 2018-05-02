package at.fhooe.mc.emg.designer

import at.fhooe.mc.emg.designer.annotation.*
import at.fhooe.mc.emg.designer.component.EmgBaseComponent
import at.fhooe.mc.emg.designer.model.Workflow
import io.reactivex.subjects.Subject
import org.reflections.ReflectionUtils
import org.reflections.Reflections
import org.reflections.scanners.FieldAnnotationsScanner
import org.reflections.scanners.SubTypesScanner
import org.reflections.scanners.TypeAnnotationsScanner
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import kotlin.reflect.KClass

/**
 * This object class handles all the heavy reflection stuff in order to create a single source of truth for
 * reflection related tasks.
 */
object ComponentInspection {

    @JvmField
    val reflections = Reflections(FieldAnnotationsScanner(), SubTypesScanner(), TypeAnnotationsScanner())

    /**
     *
     * The method extracts the consumer type, producer type and the usage of a relay port from the given component
     *
     * @param c Component of interest
     *
     * @return a Triple of format (consumes, produces, isRelayPort)
     */
    fun getPortConnectivityInformation(c: EmgBaseComponent): Pair<KClass<*>?, KClass<*>?> {

        val clazz = Class.forName(c.qualifiedName)
        val inputPort = ReflectionUtils.getMethods(clazz,
                ReflectionUtils.withAnnotation(EmgComponentInputPort::class.java)).firstOrNull()
        var outputPort = ReflectionUtils.getFields(clazz,
                ReflectionUtils.withAnnotation(EmgComponentOutputPort::class.java)).firstOrNull()

        // Check in super class if in concrete class no output port is defined
        if (outputPort == null) {
            outputPort = ReflectionUtils.getSuperTypes(Class.forName(c.qualifiedName))
                    .mapNotNull { cls ->
                        ReflectionUtils
                                .getFields(cls, ReflectionUtils.withAnnotation(EmgComponentOutputPort::class.java))
                                .firstOrNull()
                    }.firstOrNull()
        }

        // Extra check if output port has the right signature
        if (outputPort != null) {
            if (Modifier.isPrivate(outputPort.modifiers) || (outputPort.type == Subject::class)) {
                throw ComponentLogic.ValidationException("Output port in ${outputPort.declaringClass} is not from type PublishSubject or public")
            }
        }

        val consumes: KClass<*>?
        val produces: KClass<*>?
        consumes = inputPort?.annotations
                ?.find { a -> a.annotationClass == EmgComponentInputPort::class }
                .let { (it as? EmgComponentInputPort)?.consumes }
        produces = outputPort?.annotations
                ?.find { a -> a.annotationClass == EmgComponentOutputPort::class }
                .let { (it as? EmgComponentOutputPort)?.produces }

        return Pair(consumes, produces)
    }

    fun getOutputPort(c: EmgBaseComponent): Field {
        // Check in super class if in concrete class no output port is defined
        return ReflectionUtils.getFields(Class.forName(c.qualifiedName),
                ReflectionUtils.withAnnotation(EmgComponentOutputPort::class.java)).firstOrNull()
                ?: ReflectionUtils.getSuperTypes(Class.forName(c.qualifiedName))
                        .mapNotNull { cls ->
                            ReflectionUtils
                                    .getFields(cls, ReflectionUtils.withAnnotation(EmgComponentOutputPort::class.java))
                                    .firstOrNull()
                        }.firstOrNull() ?: throw IllegalStateException("No output port defined in ${c.qualifiedName}")
    }

    fun getInputPort(c: EmgBaseComponent): Method {
        return ReflectionUtils.getMethods(Class.forName(c.qualifiedName),
                ReflectionUtils.withAnnotation(EmgComponentInputPort::class.java)).firstOrNull()
                ?: throw IllegalStateException("No input port defined in ${c.qualifiedName}")
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

    /**
     * @return A method if the class has a valid startableMethod
     */
    fun getStartableMethod(c: EmgBaseComponent): Workflow.StartableMethod? {
        return ReflectionUtils.getMethods(Class.forName(c.qualifiedName),
                ReflectionUtils.withAnnotation(EmgComponentStartablePoint::class.java))
                .map { method ->

                    val startableAnnotation = method.annotations.mapNotNull { it as? EmgComponentStartablePoint }.firstOrNull()
                    if (startableAnnotation != null) {
                        val viewPropertyName = startableAnnotation.viewProperty
                        val concreteViewClass = reflections.getSubTypesOf(startableAnnotation.viewClass.java)
                                .firstOrNull { !it.isInterface }
                        Workflow.StartableMethod(method, viewPropertyName, concreteViewClass)
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