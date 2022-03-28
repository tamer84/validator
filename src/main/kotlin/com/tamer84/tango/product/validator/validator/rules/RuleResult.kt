package com.tamer84.tango.product.validator.validator.rules

import com.tamer84.tango.icecream.domain.violation.model.ViolationErrorCode


data class RuleResult private constructor(val pass: Boolean, val errorCodes : Set<ViolationErrorCode>){

    private constructor(pass: Boolean, errorCode : ViolationErrorCode) : this(pass, setOf(errorCode))

    companion object {
        fun fail(err : ViolationErrorCode) = RuleResult(false,err)
        fun pass() = RuleResult(true, emptySet())
        fun of(valid : Boolean, err : ViolationErrorCode) = RuleResult(valid,err)
        fun of(valid : Boolean, err : Set<ViolationErrorCode>) = RuleResult(valid,err)
    }

    fun isPass() : Boolean {
        return pass
    }

    fun isFail() : Boolean {
        return !pass
    }
}
