package com.ecomerce.app.dto;

import com.ecomerce.app.model.OrderStatus;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OrderRequest {
    private Long userId;
    private BigDecimal totalAmount;
    private List<OrderItemRequest> items;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderItemRequest {
        private Long productId;
        private Integer quantity;
        private BigDecimal price;
    }
}

