package com.ecomerce.app.repository;

import com.ecomerce.app.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.rmi.server.UID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UID> {
}
