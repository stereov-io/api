FROM gradle:8.5-jdk21 AS build

WORKDIR /app

COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts gradlew ./
COPY src src

RUN chmod +x gradlew && \
    ./gradlew dependencies --configuration=runtimeClasspath --no-daemon && \
    ./gradlew bootJar -x test --no-daemon

FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8000

ENTRYPOINT ["java", "-jar", "app.jar"]