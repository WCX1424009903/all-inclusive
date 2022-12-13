package org.example.kafka.listener;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaListenerHandle {

    @KafkaListener(topics = {"test_topic"})
    public void test(ConsumerRecord<String, Object> record) {
        System.out.println("key为:"+record.key());
        System.out.println("value为:"+record.value());
    }

}
