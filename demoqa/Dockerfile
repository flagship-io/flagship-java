FROM maven:3-jdk-11-slim as builder
COPY . /usr/src/app
WORKDIR /usr/src/app
RUN mvn clean package

FROM openjdk:11.0.10-slim-buster
COPY --from=builder /usr/src/app/target/ /usr/src/app 
WORKDIR /usr/src/app

CMD ["java", "-jar", "JavaQA-0.0.1-SNAPSHOT.jar"]
