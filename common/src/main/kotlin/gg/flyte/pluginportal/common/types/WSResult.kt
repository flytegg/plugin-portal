package gg.flyte.pluginportal.common.types

import com.google.gson.*
import java.lang.reflect.Type

// Custom serializer for WSResult to ensure proper JSON structure
class WSResultSerializer : JsonSerializer<WSResult<*>> {
    override fun serialize(src: WSResult<*>, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val jsonObject = JsonObject()
        
        when (src) {
            is WSResult.Success<*> -> {
                jsonObject.addProperty("status", "success")
                jsonObject.add("data", context.serialize(src.data))
            }
            is WSResult.Error -> {
                jsonObject.addProperty("status", "error")
                jsonObject.add("error", context.serialize(src.error))
            }
        }
        
        return jsonObject
    }
}

// Register this serializer with GSON
fun createWSGson(): Gson {
    return GsonBuilder()
        .registerTypeHierarchyAdapter(WSResult::class.java, WSResultSerializer())
        .create()
}