FROM amazoncorretto:25-alpine AS build

WORKDIR /app

RUN apk add --no-cache curl maven

COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn clean package -DskipTests -q

# Download Datadog Java agent during build so the runtime image has no extra tools
RUN curl -Lo /app/dd-java-agent.jar https://dtdg.co/latest-java-tracer

# ---------- Runtime ----------
FROM amazoncorretto:25-alpine

RUN apk add --no-cache curl

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar
# Datadog Java agent — auto-instruments Spring Boot, MongoDB, SQS, HTTP clients
COPY --from=build /app/dd-java-agent.jar /dd-java-agent.jar
RUN chown appuser:appgroup app.jar /dd-java-agent.jar

USER appuser

ENTRYPOINT ["java", "-javaagent:/dd-java-agent.jar", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod}", "-jar", "app.jar"]
