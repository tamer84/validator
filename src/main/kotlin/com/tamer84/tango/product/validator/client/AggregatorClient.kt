package com.tamer84.tango.product.validator.client

import com.tamer84.tango.icecream.domain.IceCreamDomain.MEDIA
import com.tamer84.tango.icecream.domain.IceCreamDomain.PRICE
import com.tamer84.tango.icecream.domain.IceCreamDomain.PRODUCT_RECORD
import com.tamer84.tango.icecream.domain.media.model.Media
import com.tamer84.tango.icecream.domain.pricing.model.Pricing
import com.tamer84.tango.icecream.domain.productRecord.model.ProductRecord
import com.tamer84.tango.product.validator.util.EnvVar
import com.tamer84.tango.product.validator.util.JsonUtil
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.util.*

class AggregatorClient(private val httpClient: HttpClient = HttpClient.newHttpClient(),
                       private val url: String = EnvVar.aggregatorUrl) {

    companion object {
        private val log = LoggerFactory.getLogger(AggregatorClient::class.java)
    }

    private fun getHeaders() : Array<String> {
        val traceId : String = kotlin.runCatching { MDC.get("traceId") }.getOrNull() ?: EnvVar.applicationName
        return arrayOf("Content-Type", "application/json; charset=utf-8", "traceId", traceId)
    }


    fun fetchMedia(productId: UUID): Media {
        val req = HttpRequest.newBuilder(URI("$url/$productId/$MEDIA"))
            .headers(*getHeaders())
            .GET()
            .build()

        val res = httpClient.sendInstrumented(req)

        return JsonUtil.fromJson(res)
    }

    fun fetchPricing(productId: UUID): Pricing {
        val req = HttpRequest.newBuilder(URI("$url/$productId/$PRICE"))
            .headers(*getHeaders())
            .GET()
            .build()

        val res = httpClient.sendInstrumented(req)

        return JsonUtil.fromJson(res)
    }

    fun fetchProductRecord(productId: UUID) : ProductRecord {

        val req = HttpRequest.newBuilder(URI("$url/$productId/${PRODUCT_RECORD}"))
            .headers(*getHeaders())
            .GET()
            .build()

        val res = httpClient.sendInstrumented(req)

        return JsonUtil.fromJson(res)
    }
}
