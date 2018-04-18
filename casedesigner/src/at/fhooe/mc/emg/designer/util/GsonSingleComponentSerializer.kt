package at.fhooe.mc.emg.designer.util

import at.fhooe.mc.emg.designer.component.EmgBaseComponent
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

class GsonSingleComponentSerializer : JsonSerializer<EmgBaseComponent> {

    override fun serialize(src: EmgBaseComponent?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement? {

        val value = src ?: return null
        val c = GsonComponentSerializer.mapping[value.type]
                ?: throw RuntimeException("Unknown class: ${value.type}")
        return context?.serialize(value, c)
    }

}