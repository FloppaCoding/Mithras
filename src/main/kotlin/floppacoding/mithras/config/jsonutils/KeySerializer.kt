package floppacoding.mithras.config.jsonutils

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import net.minecraft.client.util.InputUtil
import net.minecraft.client.util.InputUtil.Key
import java.lang.reflect.Type

class KeySerializer : JsonSerializer<Key> {
    override fun serialize(src: Key?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        if (src == null || src == InputUtil.UNKNOWN_KEY) return JsonPrimitive(-999)
        val adjustedCode = when(src.category) {
            InputUtil.Type.KEYSYM   -> src.code
            InputUtil.Type.SCANCODE -> src.code + 2000
            InputUtil.Type.MOUSE    -> src.code - 100
            else                    -> -999
        }
        return JsonPrimitive(adjustedCode)
    }

}