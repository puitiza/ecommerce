package com.ecommerce.orderservice.infrastructure.adapter.kafka;

import io.cloudevents.CloudEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * Listener for centralized Dead Letter Topics (DLTs).
 * <p>
 * Purpose:
 * - Capture messages that could not be processed after retries.
 * - Log details for debugging.
 * - Optionally persist into a database for auditing/reprocessing.
 */
@Slf4j
@Component
public class DeadLetterListener {

    @KafkaListener(topics = {"order-dead-letter-topic", "product-dead-letter-topic"})
    public void handleDeadLetter(CloudEvent event,
                                 @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                 @Header(KafkaHeaders.OFFSET) long offset,
                                 @Header(KafkaHeaders.PARTITION) int partition) {
        log.error("Dead letter received on {} (partition {}, offset {}): {}",
                topic, partition, offset, event);

        // TODO: opcional -> persistir en tabla dead_letters
        // deadLetterRepository.save(new DeadLetterMessage(topic, offset, event));
    }
}

