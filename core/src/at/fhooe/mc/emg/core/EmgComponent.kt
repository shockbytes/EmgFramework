package at.fhooe.mc.emg.core

/**
 * Classes which are annotated with this annotation are exposed as components to the EMG drag and drop framework.
 * Each and every subclass must be exposed with this annotation in order to provide a granular API for public and
 * private components. Not all components must be exposed if not desired.
 */
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
annotation class EmgComponent(val type: EmgComponentType)