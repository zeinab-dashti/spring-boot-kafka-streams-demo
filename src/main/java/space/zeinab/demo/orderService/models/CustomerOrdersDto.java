package space.zeinab.demo.orderService.models;

import lombok.Getter;
import space.zeinab.demo.kafka.CustomerOrders;

@Getter
public class CustomerOrdersDto {
    private String orderId;
    private String customerId;
    private String productId;
    private int orderCount;
    private String shipDate;

    private int loyaltyPoints;
    private int earnedLoyaltyPoints;

    public CustomerOrders toCustomerOrders() {
        return CustomerOrders.newBuilder()
                .setOrderId(orderId)
                .setCustomerId(customerId)
                .setProductId(productId)
                .setOrderCount(orderCount)
                .setLoyaltyPoints(loyaltyPoints)
                .setShipDate(shipDate)
                .setEarnedLoyaltyPoints(earnedLoyaltyPoints)
                .build();
    }
}
