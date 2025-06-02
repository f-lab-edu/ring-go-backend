FROM openjdk:21-slim

ARG SPRING_PROFILES_ACTIVE=dev
ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE}

# JVM 옵션을 환경변수로 받을 수 있도록 설정
ENV JAVA_OPTS=""

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8080

# JAVA_OPTS 환경변수를 사용하여 JVM 옵션 적용
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app.jar"]
