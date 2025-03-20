# ecomerce-app
# Microservices Overview:
# Product Service:

Manages products (CRUD operations).
Publishes events to Kafka when stock is updated.
# Order Service:

Places orders, updates inventory.
Publishes an event to Kafka when an order is placed.
# User Service:

Manages user details.
# Notification Service:

Listens to Kafka for order events and sends messages via RabbitMQ for notifications.
Uses RabbitMQ to send real-time notifications (email, SMS, push notifications).
# Docker compose 
docker-compose build
docker-compose up -d
