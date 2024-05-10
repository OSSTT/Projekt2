FROM maven:3.8.4-openjdk-17-slim as build

WORKDIR /usr/src/app
COPY . .

RUN mvn -Dmaven.test.skip=true package

FROM openjdk:17-jdk-slim

WORKDIR /usr/src/app

COPY --from=build /usr/src/app/target/celebclassification-0.0.1-SNAPSHOT.jar .
COPY azureModels /usr/src/app/azureModels

EXPOSE 8082
CMD ["java","-jar","celebclassification-0.0.1-SNAPSHOT.jar"]