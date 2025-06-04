package space.zeinab.demo.orderService.controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import space.zeinab.demo.orderService.models.CustomerOrdersDto;
import space.zeinab.demo.orderService.models.OrderDto;
import space.zeinab.demo.orderService.models.ProductDto;
import space.zeinab.demo.orderService.service.CustomerOrdersProducer;
import space.zeinab.demo.orderService.service.OrderProducer;
import space.zeinab.demo.orderService.service.ProductProducer;

@Slf4j
@RestController
@RequestMapping("/v1")
@AllArgsConstructor
public class OrderController {
    private OrderProducer orderProducer;
    private ProductProducer productProducer;
    private CustomerOrdersProducer customerOrdersProducer;

    @PostMapping("/orders")
    public void addOrder(@RequestBody OrderDto orderDto) {
        orderProducer
                .produceOrder(orderDto)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Order sent to topic: " + result.getProducerRecord().value());
                    } else {
                        log.error("Failed to send order", ex);
                        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
                    }
                });
    }

    @PostMapping("/products")
    public void addProduct(@RequestBody ProductDto productDto) {
        productProducer
                .produceProduct(productDto)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Product sent to topic: " + result.getProducerRecord().value());
                    } else {
                        log.error("Failed to send product", ex);
                        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
                    }
                });
    }

    @PostMapping("/customerOrders")
    public void addProduct(@RequestBody CustomerOrdersDto customerOrdersDto) {
        customerOrdersProducer
                .produceCustomerOrders(customerOrdersDto)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Customer orders sent to topic: " + result.getProducerRecord().value());
                    } else {
                        log.error("Failed to send customer orders", ex);
                        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
                    }
                });
    }
}