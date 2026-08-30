# ---- Stage 1: build the frontend ----
FROM node:20-slim AS frontend-build
ARG VITE_GOOGLE_CLIENT_ID
WORKDIR /app
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# ---- Stage 2: build the backend ----
FROM maven:3.9-eclipse-temurin-21 AS backend-build
WORKDIR /app
COPY backend/pom.xml ./
RUN mvn dependency:go-offline
COPY backend/ ./
COPY --from=frontend-build /app/dist ./src/main/resources/static
RUN mvn package -DskipTests

# ---- Stage 3: run it ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S appuser && adduser -S appuser -G appuser
COPY --from=backend-build /app/target/*.jar app.jar
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]