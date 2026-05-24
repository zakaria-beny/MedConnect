package com.medconnect.appointmentservice.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka configuration for appointment service topics.
 */
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public NewTopic appointmentBookedTopic() {
        return TopicBuilder.name("appointment.booked")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic appointmentCancelledTopic() {
        return TopicBuilder.name("appointment.cancelled")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic appointmentRescheduledTopic() {
        return TopicBuilder.name("appointment.rescheduled")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic appointmentNoShowTopic() {
        return TopicBuilder.name("appointment.no_show")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
