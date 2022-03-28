package com.tamer84.tango.product.validator.rulevalidators



import com.tamer84.tango.icecream.domain.IceCreamDomain
import com.tamer84.tango.icecream.domain.IceCreamEvent
import com.tamer84.tango.icecream.domain.ValidatableEvent
import com.tamer84.tango.icecream.domain.icsi.event.IceCreamStockItemInvalidatedEvent
import com.tamer84.tango.icecream.domain.pricing.event.PricingValidatedEvent
import com.tamer84.tango.icecream.domain.pricing.model.Pricing
import com.tamer84.tango.icecream.domain.violation.model.Violation
import com.tamer84.tango.product.validator.client.AggregatorClient
import com.tamer84.tango.product.validator.validator.rules.PriceRules
import com.tamer84.tango.product.validator.validator.rules.RuleResult
import org.slf4j.LoggerFactory

class PriceValidator(private val aggregatorClient: AggregatorClient) : EventValidator {

    companion object {
        private val log = LoggerFactory.getLogger(PriceValidator::class.java)
    }

    override fun validate(event: ValidatableEvent): IceCreamEvent {
        log.info("check started")

        val pricing : Pricing = aggregatorClient.fetchPricing(event.productId)

        val result : RuleResult = PriceRules.apply(pricing)

        if(result.pass){
            log.debug("check success")

            return PricingValidatedEvent(
                event.productId,
                event.sagaId,
                createSource(event.source),
                event.market,
                System.currentTimeMillis()
            )
        }

        ////////  fail /////////
        val violations: List<Violation> = toViolations(result)

        return IceCreamStockItemInvalidatedEvent(
            event.productId,
            event.sagaId,
            createSource(event.source),
            IceCreamDomain.PRICE,
            event.market,
            System.currentTimeMillis()
        ).withViolations(violations)
    }
}
