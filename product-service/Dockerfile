FROM openjdk:21

WORKDIR /app/product

COPY target/product-service-1.0-SNAPSHOT.jar product-service.jar

EXPOSE 8081

ENTRYPOINT [ "java","-jar","product-service.jar" ]

