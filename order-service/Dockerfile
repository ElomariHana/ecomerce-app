FROM openjdk:21

WORKDIR /app/order

COPY target/order-service-1.0-SNAPSHOT.jar order-service.jar

EXPOSE 8080

ENTRYPOINT [ "java","-jar","order-service.jar" ]