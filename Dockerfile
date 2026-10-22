FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /src
COPY pom.xml mvnw ./
COPY .mvn .mvn
COPY src src
RUN chmod +x mvnw && ./mvnw -q -DskipTests package \
    && JAR=$(ls target/*.jar | grep -v '\-plain\.jar$' | head -n1) \
    && cp "$JAR" /tmp/app.jar

FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache wget \
    && mkdir -p /data/uploads /app/logs
WORKDIR /app
COPY --from=build /tmp/app.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
HEALTHCHECK --interval=20s --timeout=5s --start-period=90s --retries=5 \
  CMD wget -qO- http://127.0.0.1:8080/actuator/health || exit 1
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
