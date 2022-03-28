package com.tamer84.tango.product.validator

import java.io.BufferedReader

object TestUtils {

    fun resourceToString(fileName: String) : String {
        val input = this.javaClass.classLoader.getResourceAsStream(fileName)
        requireNotNull(input) { "fileName was not found or could not be read [$fileName]"}
        return input.bufferedReader().use(BufferedReader::readText)
    }
}
