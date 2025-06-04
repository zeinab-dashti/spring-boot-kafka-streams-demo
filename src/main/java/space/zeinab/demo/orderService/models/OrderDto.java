package space.zeinab.demo.orderService.models;

import lombok.Getter;

@Getter
public class OrderDto {
    private String customerId;
    private String productId;
    private int orderCount;

    /*public Order toOrder() {
        return Order.newBuilder()
                .setCustomerId(customerId)
                .setProductId(productId)
                .setOrderCount(orderCount)
                .build();
    }*/
}
