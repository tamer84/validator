package com.tamer84.tango.product.validator.validator.rules

fun interface ChainableRule<T> : ((T) -> RuleResult) {

    fun and(rule : ChainableRule<T>) : ChainableRule<T> {
        return ChainableRule { data: T ->
            setOf(this.invoke(data), rule.invoke(data))
                .fold(RuleResult.of(true, emptySet()))
                { partial, element ->
                    val validationErrors = setOf(partial,element)
                        .filter (RuleResult::isFail)
                        .flatMap { it.errorCodes }
                        .toSet()

                    RuleResult.of(partial.isPass() && element.isPass(), validationErrors)
                }
        }
    }
}
