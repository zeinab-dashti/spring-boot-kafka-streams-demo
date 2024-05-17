package space.zeinab.demo.orderService.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@Configuration
@EnableKafka
@EnableKafkaStreams
public class KafkaConfig {
}
