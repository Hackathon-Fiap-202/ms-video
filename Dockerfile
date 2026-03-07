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

RUN apk add --no-cache curl

COPY --from=build /app/target/*.jar app.jar
RUN chown appuser:appgroup app.jar

USER appuser

# Wait for LocalStack init scripts to finish...
ENTRYPOINT ["sh", "-c", "\
    echo 'Waiting for LocalStack init scripts to finish...' && \
    until curl -sf http://localstack:4566/_localstack/init | grep -q '\"READY\": true'; do \
    echo '  LocalStack init not ready yet, retrying in 3s...' && sleep 3; \
    done && \
    echo 'LocalStack init READY — starting ms-video' && \
    exec java -jar app.jar \
    "]
