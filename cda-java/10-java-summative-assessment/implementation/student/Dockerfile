# ---- Build stage ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# Cache dependencies first.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw -q dependency:go-offline || true

# Build the boot jar (tests already run in CI; skip here for a fast image).
COPY src/ src/
RUN ./mvnw -q clean package -DskipTests

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app

# Run as an unprivileged user.
RUN useradd --system --uid 10001 --user-group vitalledger
COPY --from=build /workspace/target/vitalledger-student-*.jar /app/app.jar
USER 10001

EXPOSE 8080

# VITALLEDGER_AES_KEY MUST be provided at runtime (e.g. --env-file .env or -e).
# The app fails fast if it is missing. Never bake secrets into the image.
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
