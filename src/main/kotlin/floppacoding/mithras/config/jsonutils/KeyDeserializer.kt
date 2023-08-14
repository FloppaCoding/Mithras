package floppacoding.mithras.config.jsonutils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import net.minecraft.client.util.InputUtil
import net.minecraft.client.util.InputUtil.Key
import net.minecraft.client.util.InputUtil.UNKNOWN_KEY
import java.lang.reflect.Type

class KeyDeserializer : JsonDeserializer<Key> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Key {
        if (json?.isJsonPrimitive == true && json.asJsonPrimitive.isNumber) {
            val code = json.asJsonPrimitive.asNumber.toInt()
            return when {
                code == -999 -> UNKNOWN_KEY
                code > 1800 -> InputUtil.Type.SCANCODE.createFromCode(code - 2000)
                code < -10 -> InputUtil.Type.MOUSE.createFromCode(code + 100)
                else -> InputUtil.Type.KEYSYM.createFromCode(code)
            }
        }
        return UNKNOWN_KEY
    }
}