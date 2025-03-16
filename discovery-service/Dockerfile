FROM openjdk:21

WORKDIR /app/discovery-service

COPY target/discovery-service-1.0-SNAPSHOT.jar discovery-service.jar

EXPOSE 8761

ENTRYPOINT [ "java","-jar","discovery-service.jar" ]

