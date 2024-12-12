FROM gradle:8.5.0-jdk21 AS builder
COPY --chown=gradle:gradle . /home/gradle/src

WORKDIR /home/gradle/src

RUN gradle clean build --no-daemon -x test

FROM eclipse-temurin:21 AS runner

RUN mkdir /app

COPY --from=builder /home/gradle/src/build/libs/core-0.0.1-SNAPSHOT.jar /app/

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/core-0.0.1-SNAPSHOT.jar"]