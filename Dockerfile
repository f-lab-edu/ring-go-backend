FROM openjdk:21-slim

ARG SPRING_PROFILES_ACTIVE=dev
ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE}

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8080

# JVM 메모리 옵션
ENTRYPOINT ["java", "-Xms256m", "-Xmx400m", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=200", "-jar", "/app.jar"]
