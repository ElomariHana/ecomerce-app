FROM openjdk:21

WORKDIR /app/shop

COPY target/shop-service-1.0-SNAPSHOT.jar shop-service.jar

EXPOSE 8081

ENTRYPOINT [ "java","-jar","shop-service.jar" ]