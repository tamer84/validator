package com.tamer84.tango.product.validator.util

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.InputStream

val mapper: ObjectMapper = jacksonObjectMapper()
        .registerModules(Jdk8Module())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

object JsonUtil {

    inline fun <reified T> fromJson(json : InputStream) : T = mapper.readValue(json)

    inline fun <reified T> fromJson(json : String) : T = mapper.readValue(json)

    fun toJson(value: Any) : String = mapper.writeValueAsString(value)
}
