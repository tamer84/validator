package com.tamer84.tango.product.validator.util

import com.tamer84.tango.icecream.domain.IceCreamDomain;
import com.tamer84.tango.product.validator.client.AggregatorClient
import com.tamer84.tango.product.validator.event.Publisher
import com.tamer84.tango.product.validator.rulevalidators.*
import java.net.http.HttpClient
import java.time.Duration

// This Object serves the role that is typically reserved for IOC or DI Frameworks
// For complicated projects, it may make sense to use a lightweight DI such as Koin
object IocContainer {

    val aggregatorClient = AggregatorClient(createHttpClient())
    val publisher        = Publisher()

    private object EventValidators {
        val iceCreamStockItemValidator : IceCreamStockItemValidator by lazy {
            IceCreamStockItemValidator()
        }
        val mediaValidator : MediaValidator by lazy {
            MediaValidator(aggregatorClient)
        }
        val priceValidator : PriceValidator by lazy {
            PriceValidator(aggregatorClient)
        }
    }


    private fun createHttpClient(connectTimeoutSec: Long = 10): HttpClient {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(connectTimeoutSec))
                .build()
    }

    fun getEventValidator(domain : IceCreamDomain) : EventValidator {
         return when(domain){
             IceCreamDomain.MEDIA -> EventValidators.mediaValidator
             IceCreamDomain.PRICE -> EventValidators.priceValidator
             else -> EventValidators.iceCreamStockItemValidator
         }
    }
}
