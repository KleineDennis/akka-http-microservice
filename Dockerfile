FROM openjdk:8-jre-alpine

EXPOSE 8080

RUN mkdir /app

WORKDIR /app

ADD ./target/akka-http-microservice-1.0-SNAPSHOT-allinone.jar /app

CMD ["java", "-jar", "akka-http-microservice-1.0-SNAPSHOT-allinone.jar", "0.0.0.0", "8080"]
