FROM openjdk:8-jre-slim-buster

COPY ./build-ci/build/libs/demoqa-0.0.1.jar /usr/src/app/demoqa-0.0.1.jar

WORKDIR /usr/src/app

CMD ["java", "-jar", "demoqa-0.0.1.jar"]
