FROM openjdk:8-jre-slim-buster

RUN pwd
RUN ls -la

COPY ./build/libs/demoqa-0.0.1.jar /usr/src/app/demoqa-0.0.1.jar
COPY ./src/main/resources/static/dist /usr/src/app/dist

WORKDIR /usr/src/app

CMD ["java", "-jar", "demoqa-0.0.1.jar"]
