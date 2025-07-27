package com.ecomerce.app.model;

public enum OrderStatus {
    PENDING,
    CREATED,
    INVENTORY_RESERVED,
    PAYMENT_COMPLETED,
    SHIPPED,
    CANCELLED,
    FAILED
}
