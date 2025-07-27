package com.ecomerce.app.event;

import lombok.*;

import java.rmi.server.UID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreatedEvent {
    //Kafka Producer event
    private UID orderId;
    private Long userId;
}
