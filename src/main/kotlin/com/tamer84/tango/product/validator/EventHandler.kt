package com.tamer84.tango.product.validator

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestStreamHandler
import com.tamer84.tango.icecream.domain.IceCreamDomain
import com.tamer84.tango.product.validator.event.AwsTangoEvent

import com.tamer84.tango.product.validator.event.Publisher
import com.tamer84.tango.product.validator.util.IocContainer
import com.tamer84.tango.product.validator.util.JsonUtil
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import java.io.InputStream
import java.io.OutputStream

class EventHandler(private val publisher: Publisher = IocContainer.publisher) : RequestStreamHandler {

    companion object {
        private val log = LoggerFactory.getLogger(EventHandler::class.java)
    }

    override fun handleRequest(inputStream: InputStream, outStream: OutputStream, ctx: Context) {

        val start = System.currentTimeMillis()

        // Receive Event
        val event = JsonUtil.fromJson<AwsTangoEvent>(inputStream)
        val detail = event.detail

        MDC.put("awsReqId", ctx.awsRequestId)
        MDC.put("sagaId", detail.sagaId)
        MDC.put("productId", detail.productId())
        MDC.put("market", detail.market())
        MDC.put("domain", detail.domain.name)

        try {
            log.info("EVENT received")

            val validator = IocContainer.getEventValidator(detail.domain as IceCreamDomain)

            val result = validator.validate(detail)

            // PUBLISH EXAMPLE
            publisher.publish(result)

            log.info(
                "END - Event processing complete [durationMs={}]",
                System.currentTimeMillis() - start
            )

        } catch (e : RuntimeException) {
            log.error("END_ERROR - Event processing failed");
            throw e
        } catch (e : Exception) {
            log.error("END_ERROR - Event processing failed");
            throw RuntimeException(e);
        }
        finally {
            MDC.clear()
        }
    }
}

