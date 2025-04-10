package com.ecomerce.app.service;

import com.ecomerce.app.dto.OrderRequest;
import com.ecomerce.app.dto.OrderResponse;
import com.ecomerce.app.event.OrderCreatedEvent;
import com.ecomerce.app.model.Order;
import com.ecomerce.app.model.OrderItem;
import com.ecomerce.app.model.OrderStatus;
import com.ecomerce.app.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository repository;
    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;
    private static final String TOPIC = "order-created";

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {

        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order request or items must not be null or empty");
        }
        Order order = toOrderEntity(request);
        Order savedOrder = repository.save(order);
        log.info("Order created: {}", savedOrder.getId());

        publishOrderCreatedEvent(savedOrder);

        return toOrderResponse(savedOrder);
    }

    private Order toOrderEntity(OrderRequest request) {
        Order order = Order.builder()
                .userId(request.getUserId())
                .status(OrderStatus.CREATED)
                .totalAmount(request.getTotalAmount())
                .build();

        List<OrderItem> items = request.getItems().stream()
                .map(item -> OrderItem.builder()
                        .productId(item.getProductId())
                        .price(item.getPrice())
                        .quantity(item.getQuantity())
                        .order(order).build())
                .collect(Collectors.toList());
        order.setItems(items);

        return order;
    }

    private void publishOrderCreatedEvent(Order order) {

        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .build();
        try {
            kafkaTemplate.send(TOPIC, event);
        } catch (Exception e) {
            log.error("Failed to publish order created event: {}", e.getMessage(), e);
        }

    }

    private OrderResponse toOrderResponse(Order savedOrder) {
        return OrderResponse.builder()
                .id(savedOrder.getId())
                .userId(savedOrder.getUserId())
                .status(savedOrder.getStatus())
                .totalAmount(savedOrder.getTotalAmount())
                .createdAt(savedOrder.getCreatedAt())
                .updatedAt(savedOrder.getUpdatedAt())
                .items(savedOrder.getItems().stream()
                        .map(item -> OrderResponse.Item.builder()
                                .price(item.getPrice())
                                .productId(item.getProductId())
                                .quantity(item.getQuantity())
                                .build())
                        .collect(Collectors.toList()))
                .build();

    }
}
