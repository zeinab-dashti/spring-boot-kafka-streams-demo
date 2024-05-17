package space.zeinab.demo.orderService.topology;

import org.junit.jupiter.api.Test;
import space.zeinab.demo.kafka.EnrichedOrder;
import space.zeinab.demo.kafka.Order;
import space.zeinab.demo.kafka.Product;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderProcessorTest {

    @Test
    public void testToEnrichedOrder() {
        Order order = new Order();
        order.setCustomerId("customer1");
        order.setProductId("product1");
        order.setOrderCount(2);

        Product product = new Product();
        product.setProductId("product1");
        product.setProductPrice(50);
        product.setStockCount(10);

        EnrichedOrder enrichedOrder = OrderProcessor.toEnrichedOrder(order, product);

        assertEquals("customer1", enrichedOrder.getCustomerId());
        assertEquals("product1", enrichedOrder.getProductId());
        assertEquals(2, enrichedOrder.getOrderCount());
        assertEquals(50, enrichedOrder.getProductPrice());
        assertEquals(10, enrichedOrder.getStockCount());
        assertEquals(0, enrichedOrder.getEarnedLoyaltyPoints());
        assertEquals(100, enrichedOrder.getTotalPrice());
    }

    @Test
    public void testToEnrichedOrderWithNullProduct() {
        Order order = new Order();
        order.setCustomerId("customer1");
        order.setProductId("product1");
        order.setOrderCount(2);

        EnrichedOrder enrichedOrder = OrderProcessor.toEnrichedOrder(order, null);

        assertEquals("customer1", enrichedOrder.getCustomerId());
        assertEquals("product1", enrichedOrder.getProductId());
        assertEquals(2, enrichedOrder.getOrderCount());
        assertEquals(0, enrichedOrder.getProductPrice());
        assertEquals(0, enrichedOrder.getStockCount());
        assertEquals(0, enrichedOrder.getEarnedLoyaltyPoints());
        assertEquals(0, enrichedOrder.getTotalPrice());
    }
}
