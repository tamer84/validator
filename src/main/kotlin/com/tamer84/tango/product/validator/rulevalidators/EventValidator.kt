package com.tamer84.tango.product.validator.rulevalidators

import com.tamer84.tango.icecream.domain.IceCreamEvent
import com.tamer84.tango.icecream.domain.ValidatableEvent

interface EventValidator {

    fun validate(event : ValidatableEvent) : IceCreamEvent

}
