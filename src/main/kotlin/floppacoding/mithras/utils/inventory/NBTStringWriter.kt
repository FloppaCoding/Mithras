package floppacoding.mithras.utils.inventory

import net.minecraft.item.ItemStack
import net.minecraft.nbt.*
import net.minecraft.nbt.visitor.NbtElementVisitor
import net.minecraft.text.Text
import java.util.regex.Pattern

/**
 * Class to create nicely readable strings from nbt data.
 *
 * Based on the vanilla method [net.minecraft.nbt.visitor.StringNbtWriter].
 * @author Aton
 */
class NBTStringWriter : NbtElementVisitor {
    private var result = StringBuilder()

    fun apply(element: NbtElement?): String {
        element?.accept(this as NbtElementVisitor)
        return result.toString()
    }
    override fun visitString(element: NbtString) {
//        result.append(NbtString.escape(element.asString()))
        try {
            result.append(NbtString.escape(Text.Serializer.fromJson(element.asString())?.string))
        } catch (_: Exception) {
            result.append(NbtString.escape(element.asString()))
        }

    }

    override fun visitByte(element: NbtByte) {
        result.append(element.numberValue()).append('b')
    }

    override fun visitShort(element: NbtShort) {
        result.append(element.numberValue()).append('s')
    }

    override fun visitInt(element: NbtInt) {
        result.append(element.numberValue())
    }

    override fun visitLong(element: NbtLong) {
        result.append(element.numberValue()).append('L')
    }

    override fun visitFloat(element: NbtFloat) {
        result.append(element.floatValue()).append('f')
    }

    override fun visitDouble(element: NbtDouble) {
        result.append(element.doubleValue()).append('d')
    }

    override fun visitByteArray(element: NbtByteArray) {
        result.append("[B;")
        val bs = element.byteArray
        for (i in bs.indices) {
            if (i != 0) {
                result.append(',')
            }
            result.append(bs[i].toInt()).append('B')
        }
        result.append(']')
    }

    override fun visitIntArray(element: NbtIntArray) {
        result.append("[I;")
        val `is` = element.intArray
        for (i in `is`.indices) {
            if (i != 0) {
                result.append(',')
            }
            result.append(`is`[i])
        }
        result.append(']')
    }

    override fun visitLongArray(element: NbtLongArray) {
        result.append("[L;")
        val ls = element.longArray
        for (i in ls.indices) {
            if (i != 0) {
                result.append(',')
            }
            result.append(ls[i]).append('L')
        }
        result.append(']')
    }

    override fun visitList(element: NbtList) {
        result.append('[')
        for (i in element.indices) {
            if (i != 0) {
                result.append(',')
            }
            result.append(NEW_LINE)
            result.append(NBTStringWriter().apply(element[i]))
        }
        result = StringBuilder(result.replace(NEW_LINE.toRegex(), INDENTED_NEW_LINE))
        result.append(NEW_LINE).append(']')
    }

    override fun visitCompound(compound: NbtCompound) {
        result.append('{')
        val list: MutableList<String> = compound.keys.toMutableList()
        list.sort()
        var string: String
        val iterator: Iterator<String> = list.iterator()
        while (iterator.hasNext()) {
            string = iterator.next()
            if (result.length != 1) {
                result.append(',')
            }
            result.append(NEW_LINE)
            result.append(escapeName(string)).append(':').append(NBTStringWriter().apply(compound[string]))
        }
        result = StringBuilder(result.replace(NEW_LINE.toRegex(), INDENTED_NEW_LINE))
        result.append(NEW_LINE).append('}')
    }

    private fun escapeName(name: String): String {
        return if (SIMPLE_NAME.matcher(name).matches()) name else NbtString.escape(name)
//        return name
    }

    override fun visitEnd(element: NbtEnd?) {
        result.append("END")
    }

    companion object {
        private val SIMPLE_NAME = Pattern.compile("[A-Za-z0-9._+-]+")
        private const val INDENT = "    "
        private val NEW_LINE: String = System.lineSeparator()
        private val INDENTED_NEW_LINE: String = System.lineSeparator() + INDENT

        /**
         * Turns the nbt data of the item stack into a nicely readable string.
         * This includes indents and new lines for individual components.
         */
        @JvmStatic
        fun creatNbtString(stack: ItemStack): String {
            val nbt = NbtCompound()
            stack.writeNbt(nbt)
            return NBTStringWriter().apply(nbt)
        }
    }
}