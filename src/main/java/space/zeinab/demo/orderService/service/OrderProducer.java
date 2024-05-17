package space.zeinab.demo.orderService.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import space.zeinab.demo.kafka.Order;
import space.zeinab.demo.orderService.models.OrderDto;

import java.util.concurrent.CompletableFuture;

@Service
public class OrderProducer {
    private static final String ORDER_TOPIC = "demo-stream-order-topic";

    private final KafkaTemplate<String, Order> kafkaTemplate;

    public OrderProducer(final KafkaTemplate<String, Order> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Async
    public CompletableFuture<SendResult<String, Order>> produceOrder(OrderDto orderDto) {
        return kafkaTemplate
                .send(MessageBuilder.withPayload(orderDto.toOrder())
                        .setHeader(KafkaHeaders.KEY, orderDto.getProductId())
                        .setHeader(KafkaHeaders.TOPIC, ORDER_TOPIC)
                        .build());
    }
}