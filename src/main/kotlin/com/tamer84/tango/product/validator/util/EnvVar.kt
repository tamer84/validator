package com.tamer84.tango.product.validator.util

object EnvVar {

    val applicationName             = System.getenv("APPLICATION_NAME") ?: "validator-dev"
    val aggregatorUrl               = System.getenv("AGGREGATOR_URL") ?: "https://product-aggregator.dev.tamer84.com/aggregator"
    val destinationBus              = System.getenv("DESTINATION_BUS") ?: "events-dev"
    val minimumImagesRequired       = System.getenv("MIN_IMAGE_COUNT_REQUIREMENT")?.toInt() ?: 1

}
