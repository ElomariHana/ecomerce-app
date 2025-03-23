FROM openjdk:21

WORKDIR /app/user

COPY target/user-service-1.0-SNAPSHOT.jar user-service.jar

EXPOSE 8082

ENTRYPOINT [ "java","-jar","user-service.jar" ]

