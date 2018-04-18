package at.fhooe.mc.emg.designer.util

import at.fhooe.mc.emg.designer.component.EmgBaseComponent
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class GsonComponentDeserializer : JsonDeserializer<EmgBaseComponent> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): EmgBaseComponent? {

        if (context == null || json == null) {
            return null
        }

        val type = json.asJsonObject["type"].asString
        val clazz = GsonComponentSerializer.mapping[type] ?: throw RuntimeException("Unknown class: $type")
        return context.deserialize(json, clazz) as EmgBaseComponent
    }
}