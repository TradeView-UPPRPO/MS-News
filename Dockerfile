# ---------- build ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

COPY pom.xml .
COPY src src
RUN mvn -q -DskipTests package

# ---------- runtime ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/target/*.jar /app/ms-news.jar

EXPOSE 8080                # внутри сети Docker
ENTRYPOINT ["java","-jar","/app/ms-news.jar"]
