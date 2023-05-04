FROM openjdk:8-alpine

COPY target/uberjar/ride-hailing.jar /ride-hailing/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/ride-hailing/app.jar"]
