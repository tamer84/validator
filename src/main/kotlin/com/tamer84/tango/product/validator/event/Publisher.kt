package com.tamer84.tango.product.validator.event


import com.tamer84.tango.icecream.domain.IceCreamEvent
import com.tamer84.tango.product.validator.util.EnvVar
import com.tamer84.tango.product.validator.util.JsonUtil
import org.slf4j.LoggerFactory
import software.amazon.awssdk.services.eventbridge.EventBridgeClient
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry
import java.time.Duration
import java.time.Instant

class Publisher(private val eventBridge: EventBridgeClient = EventBridgeClient.create(),
                private val destinationBus: String = EnvVar.destinationBus) {

    companion object {
        private val log = LoggerFactory.getLogger(Publisher::class.java)
    }

    fun publish(event: IceCreamEvent) {
        publish(listOf(event))
    }

    fun publish(events: List<IceCreamEvent>) {
        require(events.size < 11){"Maximum 10 events allowed at once [eventsSize=${events.size}]"}

        val start = Instant.now()

        val entries = events.map { e -> PutEventsRequestEntry.builder()
                .detail(JsonUtil.toJson(e))
                .detailType(e.detailType())
                .eventBusName(destinationBus)
                .source(e.source)
                .time(start)
                .build()
        }

        log.debug("Publishing {} event(s)", events.size)

        val res = eventBridge.putEvents(PutEventsRequest.builder().entries(entries).build())

        when(res.failedEntryCount()) {
            0 -> log.info(
                    "Publishing complete [detailTypes={}, durationMs={}]",
                    events.joinToString { e -> e.detailType() },
                    Duration.between(start, Instant.now()).toMillis()
            )
            else -> log.error("Publishing failed [detailTypes={}, errors={}]",
                    events.joinToString { e -> e.eventName }, res.failedEntryCount())
        }
    }
}
