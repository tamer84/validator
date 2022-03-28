package com.tamer84.tango.product.validator.validator.rules

interface RuleValidator<T> {

    fun apply(data : T) : RuleResult

}
