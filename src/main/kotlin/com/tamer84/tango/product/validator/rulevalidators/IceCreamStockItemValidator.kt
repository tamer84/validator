package com.tamer84.tango.product.validator.rulevalidators

import com.tamer84.tango.icecream.domain.IceCreamEvent
import com.tamer84.tango.icecream.domain.ValidatableEvent
import com.tamer84.tango.icecream.domain.icsi.event.IceCreamStockItemValidatedEvent

class IceCreamStockItemValidator : EventValidator {
    override fun validate(event: ValidatableEvent): IceCreamEvent {
        return IceCreamStockItemValidatedEvent(
            event.productId,
            event.sagaId,
            createSource(event.source),
            event.market,
            System.currentTimeMillis()
        )
    }
}
