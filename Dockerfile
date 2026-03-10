FROM amazoncorretto:17-alpine AS build

WORKDIR /app

RUN apk add --no-cache maven

COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn clean package -DskipTests -q

# ---------- Runtime ----------
FROM amazoncorretto:17-alpine

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar
RUN chown appuser:appgroup app.jar

USER appuser

ENTRYPOINT ["java", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod}", "-jar", "app.jar"]
