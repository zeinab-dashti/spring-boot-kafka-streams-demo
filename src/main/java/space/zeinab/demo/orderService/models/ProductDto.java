package space.zeinab.demo.orderService.models;

import lombok.Getter;
import space.zeinab.demo.kafka.Product;

@Getter
public class ProductDto {
    private String productId;
    private String productName;
    private double productPrice;
    private int stockCount;

    public Product toProduct() {
        return Product.newBuilder()
                .setProductId(productId)
                .setProductName(productName)
                .setProductPrice(productPrice)
                .setStockCount(stockCount)
                .build();
    }
}
