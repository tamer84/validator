package com.tamer84.tango.product.validator.rulevalidators

import com.tamer84.tango.icecream.domain.violation.model.Violation
import com.tamer84.tango.product.validator.util.EnvVar
import com.tamer84.tango.product.validator.validator.rules.RuleResult


internal fun EventValidator.createSource(receivedEventSource : String) : String {
    val parts = receivedEventSource.split("\\.")
    return "${parts.getOrNull(0) ?: receivedEventSource}.${EnvVar.applicationName}"
}

internal fun EventValidator.toViolations(result : RuleResult) : List<Violation> {
    return result.errorCodes.map { Violation(it) }
}
