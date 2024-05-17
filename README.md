# Spring Boot Kafka Streams Demo

## Overview

This service demonstrate how to use the Kafka Streams API and Spring Boot. The sample application focuses on the power of the
Kafka Streams API for data processing.

## Example Use Case

Consider an order service that allows customers to earn credits from purchases, applies discounts based on these
credits, updates pending orders for the same product, and verifies stock for order acceptance.

The following diagram illustrate the data flow for a new order-saving process whose topology has been developed using a
high-level DSL:

![collector-notifier-architecture.jpg](./img/Save-new-order-topology.jpg#center)

## Data Serialization

This example utilizes the AVRO format for data serialization, check out the **avro** directory if you are interested to
the schema.

## Required Topics

This example needs to have the following topics. 

| Topic                                   | Key        | Value                    |
|-----------------------------------------|------------|--------------------------|
| ```demo-stream-order-topic```           | productId  | customer new orders      |
| ```demo-stream-product-topic```         | productId  | product details          |
| ```demo-stream-customer-orders-topic``` | customerId | customer prior orders    |
| ```demo-stream-enriched-orders-topic``` | customerId | customer enriched orders |

## Build & Run

Your local environment should have Java 17, maven and docker installed in order to build and run this application.

After starting up the Kafka, building the topics, and providing appropriate configuration values in the [local property file](./src/main/resources/application-local.properties), run ```mvn clean install spring-boot:run``` to build and run the application.

## Manual Test

The [OrderController](./src/main/java/space/zeinab/demo/orderService/controllers/OrderController.java) shows how to
produce Kafka messages using KafkaTemplate.

This command produce a new order request:

```bash
curl -X POST http://localhost:8080/v1/orders 
   -H "Content-Type: application/json"
   -d '{"customerId": "c1", "productId": "p1", "orderCount": 3}'
```

This command produce a new product:

```bash
curl -X POST http://localhost:8080/v1/products 
   -H "Content-Type: application/json"
   -d '{"productId": "p1", "productName": "productName1", "productPrice": 50, "stockCount": 10}'
```

This command produce customer prior orders:

```bash
curl -X POST http://localhost:8080/v1/customerOrders 
   -H "Content-Type: application/json"
   -d '{"orderId": "o1", "customerId": "c1", "productId": "p1", "orderCount": 3, "loyaltyPoints": 2, "shipDate": "2024-12-8"}'
```

Once new order is posted, the output message which is an enriched order is printed in the screen and also published in the ```enriched-order-topic```
topic, whether the product or the customer's prior orders have already been produced or not.
