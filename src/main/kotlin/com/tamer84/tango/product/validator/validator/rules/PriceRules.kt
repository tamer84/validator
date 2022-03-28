package com.tamer84.tango.product.validator.validator.rules

import com.tamer84.tango.icecream.domain.pricing.model.Pricing
import com.tamer84.tango.icecream.domain.violation.model.ViolationErrorCode
import java.math.BigDecimal

object PriceRules : RuleValidator<Pricing> {

    private const val MINIMUM_GROSS_PRICE = 0L
    private const val MAXIMUM_GROSS_PRICE = 100000L

    private val hasPriceBelowMin = ChainableRule { pricing : Pricing ->
        val passFail = (pricing.price.value ?: BigDecimal.ZERO) > BigDecimal.valueOf(MINIMUM_GROSS_PRICE)
        RuleResult.of(passFail, ViolationErrorCode.PRICE_TOO_LOW)
    }

    private val hasPriceAboveMax = ChainableRule { pricing : Pricing ->
        val passFail = (pricing.price.value ?: BigDecimal.ZERO) < BigDecimal.valueOf(MAXIMUM_GROSS_PRICE)
        RuleResult.of(passFail, ViolationErrorCode.PRICE_TOO_LOW)
    }


    override fun apply(data: Pricing): RuleResult {
        return hasPriceBelowMin
            .and(hasPriceAboveMax)(data)
    }
}
