package space.zeinab.demo.orderService.topology;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import lombok.AllArgsConstructor;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import space.zeinab.demo.kafka.CustomerOrders;
import space.zeinab.demo.kafka.EnrichedOrder;
import space.zeinab.demo.kafka.Order;
import space.zeinab.demo.kafka.Product;

@Component
@AllArgsConstructor
public class OrderProcessor {

    private static final String ORDER_TOPIC = "demo-stream-order-topic";
    private static final String PRODUCT_TOPIC = "demo-stream-product-topic";
    private static final String CUSTOMER_ORDERS_TOPIC = "demo-stream-customer-orders-topic";
    private static final String OUTPUT_TOPIC = "demo-stream-enriched-orders-topic";
    private static final int minimumStockCount = 1;

    private SchemaRegistryConfig srConfig;

    @Autowired
    void buildPipeline(StreamsBuilder streamsBuilder) {
        KStream<String, Order> orders = streamsBuilder
                .stream(ORDER_TOPIC, Consumed.with(Serdes.String(), order()));

        KTable<String, Product> products = streamsBuilder
                .table(PRODUCT_TOPIC, Consumed.with(Serdes.String(), product()))
                .filter((key, product) -> product.getStockCount() > minimumStockCount);

        KStream<String, CustomerOrders> customerOrders = streamsBuilder
                .stream(CUSTOMER_ORDERS_TOPIC, Consumed.with(Serdes.String(), customerOrders()));

        KTable<String, CustomerOrders> customersEarnedLoyaltyPoints =
                customerOrders
                        .groupByKey(Grouped.with(Serdes.String(), customerOrders()))
                        .reduce((latest, current) -> {
                            latest.setEarnedLoyaltyPoints(current.getLoyaltyPoints() + latest.getEarnedLoyaltyPoints());
                            return latest;
                        });

        KTable<String, CustomerOrders> customersPendingOrders =
                customerOrders
                        .filter((key, value) -> value.getShipDate().isEmpty())
                        .groupByKey(Grouped.with(Serdes.String(), customerOrders()))
                        .reduce((latest, current) -> {
                            latest.setOrderCount(current.getOrderCount() + latest.getOrderCount());
                            return latest;
                        });

        orders.leftJoin(products, OrderProcessor::toEnrichedOrder)
                .selectKey((key, order) -> order.getCustomerId())
                .leftJoin(customersEarnedLoyaltyPoints,
                        (order, customerLoyaltyPoints) -> {
                            order.setEarnedLoyaltyPoints(customerLoyaltyPoints == null ? 0 : customerLoyaltyPoints.getEarnedLoyaltyPoints());
                            order.setTotalPrice(order.getTotalPrice() - (customerLoyaltyPoints == null ? 0 : customerLoyaltyPoints.getEarnedLoyaltyPoints()));
                            return order;
                        },
                        Joined.with(Serdes.String(), enrichedOrder(), customerOrders())
                )
                .leftJoin(customersPendingOrders,
                        (order, pendingOrder) -> {
                            order.setOrderCount(order.getOrderCount() + (pendingOrder == null ? 0 : pendingOrder.getOrderCount()));
                            return order;
                        },
                        Joined.with(Serdes.String(), enrichedOrder(), customerOrders())
                )
                .peek((key,enrichedOrder)-> System.out.println(enrichedOrder))
                .to(OUTPUT_TOPIC, Produced.with(Serdes.String(), enrichedOrder()));
    }

    public static EnrichedOrder toEnrichedOrder(Order order, Product product) {
        return EnrichedOrder.newBuilder()
                .setCustomerId(order.getCustomerId())
                .setProductId(order.getProductId())
                .setOrderCount(order.getOrderCount())
                .setProductPrice(product == null ? 0 : product.getProductPrice())
                .setStockCount(product == null ? 0 : product.getStockCount())
                .setEarnedLoyaltyPoints(0)
                .setTotalPrice(order.getOrderCount() * (product == null ? 0 : product.getProductPrice()))
                .build();
    }

    public Serde<Order> order() {
        SpecificAvroSerde<Order> rv = new SpecificAvroSerde<>();
        rv.configure(srConfig.buildPropertiesMap(), false);
        return rv;
    }

    public Serde<Product> product() {
        SpecificAvroSerde<Product> rv = new SpecificAvroSerde<>();
        rv.configure(srConfig.buildPropertiesMap(), false);
        return rv;
    }

    public Serde<CustomerOrders> customerOrders() {
        SpecificAvroSerde<CustomerOrders> rv = new SpecificAvroSerde<>();
        rv.configure(srConfig.buildPropertiesMap(), false);
        return rv;
    }

    public Serde<EnrichedOrder> enrichedOrder() {
        SpecificAvroSerde<EnrichedOrder> rv = new SpecificAvroSerde<>();
        rv.configure(srConfig.buildPropertiesMap(), false);
        return rv;
    }

}
