package floppacoding.mithras.config.jsonutils

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import net.minecraft.util.math.BlockPos

/**
 * Type Adapter to convert a [Set] of [BlockPos] into a json array of json arrays of 3 integers.
 *
 * Example:
 *
 *      setOf(BlockPos(1,2,3), BlockPos(4,5,6))
 *
 * would become
 *
 *      [[1,2,3],[4,5,6]]
 *
 * @author Aton
 */
class SetBlockPosAdapter: TypeAdapter<Set<BlockPos>>() {
    override fun write(writer: JsonWriter, value: Set<BlockPos>?) {
        if (value == null) {
            writer.nullValue()
            return
        }
        writer.beginArray()
        value.forEach {
            writer.beginArray()
            writer.value(it.x)
            writer.value(it.y)
            writer.value(it.z)
            writer.endArray()
        }
        writer.endArray()
    }

    override fun read(reader: JsonReader): Set<BlockPos>? {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            return null
        }
        val set = mutableSetOf<BlockPos>()
        reader.beginArray()
        while (reader.hasNext()) {
            reader.beginArray()
            val x = reader.nextInt()
            val y = reader.nextInt()
            val z = reader.nextInt()
            set.add(BlockPos(x,y,z))
            reader.endArray()
        }
        reader.endArray()
        return set
    }
}