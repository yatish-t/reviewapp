FROM amazoncorretto:17-alpine
WORKDIR /app
COPY target/ReviewApp-0.0.1-SNAPSHOT.jar ReviewApp-0.0.1-SNAPSHOT.jar
EXPOSE 8080
CMD ["java","-jar","ReviewApp-0.0.1-SNAPSHOT.jar"]

