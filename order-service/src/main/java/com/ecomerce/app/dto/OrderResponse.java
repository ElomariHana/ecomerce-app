package com.ecomerce.app.dto;

import com.ecomerce.app.model.OrderStatus;
import lombok.*;

import java.math.BigDecimal;
import java.rmi.server.UID;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OrderResponse {
    private UID id;
    private Long userId;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private List<Item> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Item {
        private Long productId;
        private Integer quantity;
        private BigDecimal price;
    }
}
