# build
FROM maven:3.9.9-amazoncorretto-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# run
FROM amazoncorretto:21
WORKDIR /app

COPY --from=build /app/target/*.jar ./logscope-api.jar

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=docker
ENV TZ=America/Sao_Paulo

ENV LOGSCOPE_JWT_SECRET=""
ENV MONGODB_URI=""
ENV RABBITMQ_HOST=""
ENV REDIS_HOST=""

ENTRYPOINT ["java", "-jar", "logscope-api.jar"]
