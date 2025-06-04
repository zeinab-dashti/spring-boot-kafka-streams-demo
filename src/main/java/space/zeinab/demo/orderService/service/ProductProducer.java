package space.zeinab.demo.orderService.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import space.zeinab.demo.orderService.models.ProductDto;

import java.util.concurrent.CompletableFuture;

@Service
public class ProductProducer {
    private static final String PRODUCT_TOPIC = "demo-stream-product-topic";

    private final KafkaTemplate<String, ProductDto> kafkaTemplate;

    public ProductProducer(final KafkaTemplate<String, ProductDto> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Async
    public CompletableFuture<SendResult<String, ProductDto>> produceProduct(ProductDto productDto) {
        return kafkaTemplate
                .send(MessageBuilder.withPayload(productDto)
                        .setHeader(KafkaHeaders.KEY, productDto.getProductId())
                        .setHeader(KafkaHeaders.TOPIC, PRODUCT_TOPIC)
                        .build());
    }
}