package com.tamer84.tango.product.validator.validator.rules

import com.tamer84.tango.icecream.domain.media.model.Media
import com.tamer84.tango.icecream.domain.violation.model.ViolationErrorCode
import com.tamer84.tango.product.validator.util.EnvVar


object MediaRules : RuleValidator<Media> {

    private val hasRequiredAmountOfImages = ChainableRule { media : Media ->
        val passFail = media.images.size >= EnvVar.minimumImagesRequired
        RuleResult.of(passFail, ViolationErrorCode.MEDIA_IMAGE_COUNT_TO0_LOW)
    }

    private val hasValidUrls = ChainableRule { media : Media ->
        val passFail = !(media.images?.mapNotNull { it.url }.isNullOrEmpty())
        RuleResult.of(passFail, ViolationErrorCode.MEDIA_IMAGE_URL_INVALID)
    }

    override fun apply(data: Media): RuleResult {
        return hasRequiredAmountOfImages
            .and(hasValidUrls)(data)
    }
}
