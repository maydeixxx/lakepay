package com.lakepayProj.userService.application.kafka;

import com.lakepayProj.userService.application.services.UserService;
import lombok.AllArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserConsumer {
    private final UserService service;

    @KafkaListener(topics = "ads-sub", groupId = "subscriptions", properties = {"partition.assignment.strategy=org.apache.kafka.clients.consumer.RangeAssignor"})
    public void handleSubscribe(ConsumerRecord<String, String> record) {
        if (record.partition() == 0) {
            service.subscribe(Long.valueOf(record.key()), record.value());

        } else if (record.partition() == 1) {
            service.unSubscribe(Long.valueOf(record.key()), record.value());
        }
    }
}