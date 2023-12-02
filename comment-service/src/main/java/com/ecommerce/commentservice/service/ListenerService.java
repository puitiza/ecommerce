package com.ecommerce.commentservice.service;

import com.ecommerce.commentservice.model.Order;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.CloudEventUtils;
import io.cloudevents.core.data.PojoCloudEventData;
import io.cloudevents.jackson.PojoCloudEventDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public record ListenerService (ObjectMapper mapper) {

    @KafkaListener(
            topics = "${custom.consumer.user.topic-name}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void consumeOrder(@Payload CloudEvent cloudEvent) {
        //convert cloudEvent data from binary json to SampleData
        PojoCloudEventData<Order> deserializedData = CloudEventUtils
                .mapData(cloudEvent, PojoCloudEventDataMapper.from(mapper, Order.class));

        if (deserializedData != null) {
            Order data = deserializedData.getValue();
            log.info("Received cloudEvent. Id: {}; Data: {}", cloudEvent.getId(), data.toString());
        } else {
            log.warn("No data in cloudEvent {}", cloudEvent.getId());
        }
    }

}
