# Stage 1: Build application with Maven
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /app

# Copy project files
COPY pom.xml .
COPY src ./src

# Build package and copy dependencies
RUN mvn clean package -DskipTests

# Stage 2: Minimal runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy target folder and webapp source (for JSP runtime)
COPY --from=builder /app/target /app/target
COPY --from=builder /app/src/main/webapp /app/src/main/webapp

ENV PORT=8080
EXPOSE 8080

CMD ["java", "-cp", "target/classes:target/dependency/*", "launcher.AppLauncher"]
