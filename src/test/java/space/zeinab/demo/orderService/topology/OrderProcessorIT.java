package space.zeinab.demo.orderService.topology;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.test.TestRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import space.zeinab.demo.kafka.CustomerOrders;
import space.zeinab.demo.kafka.EnrichedOrder;
import space.zeinab.demo.kafka.Order;
import space.zeinab.demo.kafka.Product;

import java.time.Duration;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderProcessorIT {
    private static final String ORDER_TOPIC = "demo-stream-order-topic";
    private static final String PRODUCT_TOPIC = "demo-stream-product-topic";
    private static final String CUSTOMER_ORDERS_TOPIC = "demo-stream-customer-orders-topic";
    private static final String OUTPUT_TOPIC = "demo-stream-enriched-orders-topic";

    private TopologyTestDriver testDriver;
    private TestInputTopic<String, Order> orderInputTopic;
    private TestInputTopic<String, Product> productInputTopic;
    private TestInputTopic<String, CustomerOrders> customerOrdersInputTopic;
    private TestOutputTopic<String, EnrichedOrder> outputTopic;

    @BeforeEach
    public void setUp() {
        SchemaRegistryConfig config = new SchemaRegistryConfig("mock://localhost", "", "", "", "", "");
        OrderProcessor orderProcessor = new OrderProcessor(config);

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test-app");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");

        StreamsBuilder streamsBuilder = new StreamsBuilder();
        orderProcessor.buildPipeline(streamsBuilder);

        testDriver = new TopologyTestDriver(streamsBuilder.build(), props);
        orderInputTopic = testDriver.createInputTopic(ORDER_TOPIC, Serdes.String().serializer(), orderProcessor.order().serializer());
        productInputTopic = testDriver.createInputTopic(PRODUCT_TOPIC, Serdes.String().serializer(), orderProcessor.product().serializer());
        customerOrdersInputTopic = testDriver.createInputTopic(CUSTOMER_ORDERS_TOPIC, Serdes.String().serializer(), orderProcessor.customerOrders().serializer());
        outputTopic = testDriver.createOutputTopic(OUTPUT_TOPIC, Serdes.String().deserializer(), orderProcessor.enrichedOrder().deserializer());
    }

    @AfterEach
    public void tearDown() {
        testDriver.close();
    }

    @Test
    public void testOrderProcessing() {
        Order order = new Order();
        order.setCustomerId("customer1");
        order.setProductId("product1");
        order.setOrderCount(2);

        Product product = new Product();
        product.setProductId("product1");
        product.setProductName("productName1");
        product.setProductPrice(50);
        product.setStockCount(10);

        CustomerOrders customerOrder = new CustomerOrders();
        customerOrder.setOrderId("order1");
        customerOrder.setProductId("product1");
        customerOrder.setCustomerId("customer1");
        customerOrder.setLoyaltyPoints(10);
        customerOrder.setOrderCount(1);
        customerOrder.setShipDate("");

        // Pipe in product and customer order first to ensure they are available for the join
        productInputTopic.pipeInput("product1", product, System.currentTimeMillis());
        customerOrdersInputTopic.pipeInput("customer1", customerOrder, System.currentTimeMillis() + 10000);

        // Advance the wall clock time to ensure proper processing order
        testDriver.advanceWallClockTime(Duration.ofMillis(30000));

        // Now pipe in the order
        orderInputTopic.pipeInput("product1", order, System.currentTimeMillis() + 30000);

        TestRecord<String, EnrichedOrder> result = outputTopic.readRecord();
        EnrichedOrder enrichedOrder = result.value();

        assertEquals("customer1", enrichedOrder.getCustomerId());
        assertEquals("product1", enrichedOrder.getProductId());
        assertEquals(3, enrichedOrder.getOrderCount());
        assertEquals(50, enrichedOrder.getProductPrice());
        assertEquals(10, enrichedOrder.getStockCount());
        assertEquals(10, enrichedOrder.getEarnedLoyaltyPoints());
        assertEquals(90, enrichedOrder.getTotalPrice()); // totalPrice = 2 * 50 - 10
    }

    @Test
    public void testOrderProcessingWithNullProduct() {
        Order order = new Order();
        order.setCustomerId("customer1");
        order.setProductId("product1");
        order.setOrderCount(2);

        orderInputTopic.pipeInput("order1", order);

        TestRecord<String, EnrichedOrder> result = outputTopic.readRecord();
        EnrichedOrder enrichedOrder = result.value();

        assertEquals("customer1", enrichedOrder.getCustomerId());
        assertEquals("product1", enrichedOrder.getProductId());
        assertEquals(2, enrichedOrder.getOrderCount());
        assertEquals(0, enrichedOrder.getProductPrice());
        assertEquals(0, enrichedOrder.getStockCount());
        assertEquals(0, enrichedOrder.getEarnedLoyaltyPoints());
        assertEquals(0, enrichedOrder.getTotalPrice());
    }

}
