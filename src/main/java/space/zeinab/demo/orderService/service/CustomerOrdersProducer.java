package space.zeinab.demo.orderService.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import space.zeinab.demo.orderService.models.CustomerOrdersDto;

import java.util.concurrent.CompletableFuture;

@Service
public class CustomerOrdersProducer {
    private static final String CUSTOMER_ORDERS_TOPIC = "demo-stream-customer-orders-topic";

    private final KafkaTemplate<String, CustomerOrdersDto> kafkaTemplate;

    public CustomerOrdersProducer(final KafkaTemplate<String, CustomerOrdersDto> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Async
    public CompletableFuture<SendResult<String, CustomerOrdersDto>> produceCustomerOrders(CustomerOrdersDto customerOrdersDto) {
        return kafkaTemplate
                .send(MessageBuilder.withPayload(customerOrdersDto)
                        .setHeader(KafkaHeaders.KEY, customerOrdersDto.getCustomerId())
                        .setHeader(KafkaHeaders.TOPIC, CUSTOMER_ORDERS_TOPIC)
                        .build());
    }
}