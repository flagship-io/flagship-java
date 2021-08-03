FROM openjdk:8-jre-slim-buster

RUN pwd
RUN ls -la

COPY ./build-ci/build/libs/demoqa-0.0.1.jar /usr/src/app/demoqa-0.0.1.jar
COPY ./build-ci/src/main/resources/static/dist/lib /usr/src/app/dist

WORKDIR /usr/src/app

CMD ["java", "-jar", "demoqa-0.0.1.jar"]
