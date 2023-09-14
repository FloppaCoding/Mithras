package floppacoding.mithras.module.impl.debug

import com.google.gson.Gson
import com.google.gson.JsonIOException
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken



class SampleClass(
    val name: String,
    val number: Int,
    val wat: String,
)

fun main() {
    val json = "[{\"name\":\"Name1\",\"wat\":\"aaaa\",\"number\":7},{\"name\":\"Name2\",\"wat\":\"abba\"}]"
    val roomList: Set<SampleClass> = try {
        Gson().fromJson(
            json,
            object : TypeToken<Set<SampleClass>>() {}.type
        )
    } catch (e: JsonSyntaxException) {
        println("Error parsing data.")
        setOf()
    } catch (e: JsonIOException) {
        println("Error reading data.")
        setOf()
    }
    roomList.forEach {
        println("name = ${it.name}, number = ${it.number}, wat = ${it.wat}")
    }
    println(Gson().toJson(roomList))
}