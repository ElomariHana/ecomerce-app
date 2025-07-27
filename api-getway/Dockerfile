FROM openjdk:21

WORKDIR /app/api-getway

COPY target/api-getway-1.0-SNAPSHOT.jar api-getway.jar

EXPOSE 8085

ENTRYPOINT [ "java","-jar","api-getway.jar" ]