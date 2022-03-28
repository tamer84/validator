package com.tamer84.tango.product.validator.client

import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.ConnectException
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*

private val log = LoggerFactory.getLogger(HttpClient::class.java)

fun HttpClient.sendInstrumented(req: HttpRequest) : String {

    try {
        log.debug("Request: [method=${req.method()}, host=${req.uri().host}]")
        val start = System.nanoTime()
        val resp = this.send(req, HttpResponse.BodyHandlers.ofString())
        val duration = ((System.nanoTime() - start) / 1e6).toInt()
        log.info("Response: [method=${resp.request().method()}, code=${resp.statusCode()}, durationMs=$duration, host=${resp.request().uri().host}]")

        if(resp.statusCode() != 200)
            throw IOException("Request failed: [code=${resp.statusCode()}, data=${resp.body()}]")

        return resp.body()
    }
    catch (e: ConnectException) {
        throw IOException("CONNECTION FAILURE - ( method=[method=${req.method()}, uri=${req.uri()}] )", e)
    }
}

fun basicAuth(username: String, password: String? = ""): String {
    require(username.isNotBlank()) { "basicAuth() username is required" }
    return "Basic " + Base64.getEncoder().encodeToString("$username:$password".toByteArray())
}
