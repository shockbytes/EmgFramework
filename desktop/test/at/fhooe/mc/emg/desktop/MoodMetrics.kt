package at.fhooe.mc.emg.desktop

import at.fhooe.mc.emg.core.util.CoreUtils
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.reflections.Reflections
import org.reflections.ReflectionsException
import org.reflections.scanners.MemberUsageScanner
import org.reflections.scanners.MethodAnnotationsScanner
import org.reflections.scanners.SubTypesScanner
import java.lang.reflect.Modifier

class MoodMetrics(private val inspectingPackage: String) {

    private val reflections = Reflections(inspectingPackage,
            SubTypesScanner(false), MethodAnnotationsScanner(), MemberUsageScanner())

    private val classes: List<Class<*>> = reflections.allTypes
            .filter { !it.contains("$") }
            .mapNotNull { Class.forName(it) }

    fun metrics(): Single<String> {
        return Single.fromCallable {

            val metricsMap = mutableMapOf<String, Double>()
            metricsMap.putAll(grabHidingFactorMetrics())
            metricsMap.putAll(grabInheritanceFactorMetrics())
            metricsMap.putAll(grabCouplingFactorMetric())

            metricsMap.entries.joinToString("\n") { "${it.key}:\t${it.value}%" }
        }.subscribeOn(Schedulers.computation())
    }


    /**
     * MHF = 1 - MethodsVisible
     * AHF = 1 - AttributesVisible
     * MethodsVisible = sum(MV) / (C-1) / Number of methods
     * MV = number of other classes where method is visible
     * AttributesVisible = sum(AV) / (C-1) / Number of attributes
     * AV = number of other classes where attribute is visible
     * C = number of classes
     */
    private fun grabHidingFactorMetrics(): Map<String, Double> {

        val c: Int = classes.size
        var av = 0
        var mv = 0
        var numAttributes = 0
        var numMethods = 0

        classes.forEach { cls ->

            // Increment overall number of attributes and methods
            numAttributes += cls.declaredFields.size
            numMethods += cls.declaredMethods.size


            // Sweep through class fields and take a look into field visibility
            cls.declaredFields.forEach { field ->
                val modifier = field.modifiers
                val incAv = extractAvMv(modifier, cls, c)
                av += incAv
            }

            // Sweep through class methods and take a look into method visibility
            cls.declaredMethods.forEach { method ->
                val modifier = method.modifiers
                val incMv = extractAvMv(modifier, cls, c)
                mv += incMv
            }
        }

        val attributesVisible = av / (c - 1).toDouble() / numAttributes
        val methodsVisible = mv / (c - 1).toDouble() / numMethods

        val mhf = (1 - methodsVisible) * 100
        val ahf = (1 - attributesVisible) * 100

        return mapOf(Pair("AHF", CoreUtils.roundDouble(ahf, 2)),
                Pair("MHF", CoreUtils.roundDouble(mhf, 2)))
    }

    /**
     * MIF = inherited methods/total methods available in classes
     * AIF = inherited attributes/total attributes available in classes
     */
    private fun grabInheritanceFactorMetrics(): Map<String, Double> {

        var inheritedMethods = 0
        var availableMethods = 0
        var inheritedAttributes = 0
        var availableAttributes = 0

        classes.filter { !it.isInterface }
                .forEach { cls ->

                    // Methods
                    availableMethods += cls.methods.size
                    val incInheritedMethods = cls.methods
                            .filter { method ->
                                // Remove object methods
                                method.declaringClass != Object::class.java
                                        && method.declaringClass != Annotation::class.java
                                        && method.declaringClass.name != cls.name
                            }.size
                    inheritedMethods += incInheritedMethods

                    // Fields
                    availableAttributes += cls.fields.size
                    val incInheritedAttributes = cls.fields
                            .filter { field ->
                                // Remove object fields
                                field.declaringClass != Object::class.java
                                        && field.declaringClass != Annotation::class.java
                                        && field.declaringClass.name != cls.name
                                        && field.name != "Companion"
                            }.size
                    inheritedAttributes += incInheritedAttributes
                }
        val mif = (inheritedMethods / availableMethods.toDouble()) * 100
        val aif = (inheritedAttributes / availableAttributes.toDouble()) * 100

        return mapOf(Pair("MIF", CoreUtils.roundDouble(mif, 2)),
                Pair("AIF", CoreUtils.roundDouble(aif, 2)))
    }

    private fun grabCouplingFactorMetric(): Map<String, Double> {

        var fieldCoupling = 0
        var methodCoupling = 0
        classes.filter { !it.isInterface && it != this.javaClass }
                .forEach { cls ->

                    cls.fields
                            .forEach { field ->
                                try {
                                    // Filter calls from the own class
                                    val fieldUsage = reflections.getFieldUsage(field)
                                            .filter { f -> f.declaringClass != cls }.size
                                    fieldCoupling += fieldUsage
                                } catch (e: ReflectionsException) {
                                }
                            }
                    cls.methods
                            // Filter system specific calls
                            .filter { m -> m.declaringClass.`package`.name.startsWith(inspectingPackage) }
                            .forEach { method ->
                                try {
                                    // Filter calls from the own class
                                    val methodUsage = reflections.getMethodUsage(method)
                                            .filter { m -> m.declaringClass != cls }.size
                                    methodCoupling += methodUsage
                                } catch (e: ReflectionsException) {
                                }
                            }
                }

        val tc = classes.size
        val cf = (fieldCoupling + methodCoupling) / (Math.pow(tc.toDouble(), 2.toDouble()) - tc) * 100
        return mapOf(Pair("COF", CoreUtils.roundDouble(cf, 2)))
    }

    private fun extractAvMv(modifier: Int, cls: Class<*>, classCount: Int): Int {

        return when {
            Modifier.isPrivate(modifier) -> 0
            Modifier.isProtected(modifier) -> {
                val tmpReflections = Reflections(cls.`package`.name, SubTypesScanner(false))
                tmpReflections.allTypes.size - 1
            }
            Modifier.isPublic(modifier) -> classCount - 1
            else -> 0
        }
    }


}