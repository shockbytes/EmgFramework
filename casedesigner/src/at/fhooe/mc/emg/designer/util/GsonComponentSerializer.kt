package at.fhooe.mc.emg.designer.util

import at.fhooe.mc.emg.designer.component.*
import at.fhooe.mc.emg.designer.component.internal.ConnectorComponent
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type
import java.util.*

class GsonComponentSerializer : JsonSerializer<ArrayList<EmgBaseComponent>> {

    override fun serialize(src: ArrayList<EmgBaseComponent>?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement? {

        val list = src ?: return null

        val array = JsonArray()
        list.forEach {
            val c = mapping[it.type] ?: throw RuntimeException("Unknown class: ${it.type}")
            array.add(context?.serialize(it, c))
        }
        return array
    }


    companion object {

        val mapping: Map<String, Class<*>>

        init {
            mapping = TreeMap()
            mapping.put("EmgDeviceComponent", EmgDeviceComponent::class.java)
            mapping.put("EmgFilterComponent", EmgFilterComponent::class.java)
            mapping.put("EmgRelaySinkComponent", EmgRelaySinkComponent::class.java)
            mapping.put("EmgSinkComponent", EmgSinkComponent::class.java)
            mapping.put("EmgToolComponent", EmgToolComponent::class.java)
            mapping.put("ConnectorComponent", ConnectorComponent::class.java)
        }
    }
}