package com.tamer84.tango.product.validator.rulevalidators

import com.tamer84.tango.icecream.domain.IceCreamDomain
import com.tamer84.tango.icecream.domain.IceCreamEvent
import com.tamer84.tango.icecream.domain.ValidatableEvent
import com.tamer84.tango.icecream.domain.icsi.event.IceCreamStockItemInvalidatedEvent
import com.tamer84.tango.icecream.domain.media.event.MediaValidatedEvent
import com.tamer84.tango.icecream.domain.media.model.Media
import com.tamer84.tango.icecream.domain.violation.model.Violation

import com.tamer84.tango.product.validator.client.AggregatorClient
import com.tamer84.tango.product.validator.validator.rules.MediaRules
import com.tamer84.tango.product.validator.validator.rules.RuleResult
import org.slf4j.LoggerFactory

class MediaValidator(private val aggregatorClient: AggregatorClient) : EventValidator {

    companion object {
        private val log = LoggerFactory.getLogger(MediaValidator::class.java)
    }

    override fun validate(event: ValidatableEvent): IceCreamEvent {
        log.info("check started")

        val media : Media = aggregatorClient.fetchMedia(event.productId)

        val result : RuleResult = MediaRules.apply(media)

        if(result.pass) {
            log.debug("check success")

            return MediaValidatedEvent(
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
            IceCreamDomain.MEDIA,
            event.market,
            System.currentTimeMillis()
        ).withViolations(violations)
    }
}
