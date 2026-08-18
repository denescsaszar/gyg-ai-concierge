# syntax=docker/dockerfile:1

# ---------- build ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY gradle gradle
COPY gradlew build.gradle.kts settings.gradle.kts ./
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon
COPY src src
RUN ./gradlew bootJar --no-daemon

# ---------- unpack ----------
# Running from an extracted jar avoids Spring Boot's nested-jar class loader on
# every boot, and is a prerequisite for a useful CDS archive. If `jarmode=tools`
# is unavailable for any reason we fall back to the plain fat jar, so this step
# can never break the deploy.
FROM eclipse-temurin:21-jre AS unpack
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --destination /app/extracted || true; \
    if [ ! -f /app/extracted/app.jar ]; then mkdir -p /app/extracted && cp app.jar /app/extracted/app.jar; fi

# ---------- runtime ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Non-root: the only thing written at build time is the CDS archive below.
RUN useradd --system --create-home --uid 10001 spring && chown spring:spring /app
COPY --from=unpack --chown=spring:spring /app/extracted/ ./
USER spring

# Class Data Sharing training run: boot the Spring context once at build time and
# archive the loaded classes, so the real cold start skips most class loading and
# verification. `-Dspring.context.exit=onRefresh` stops right after refresh.
# `-Xshare:auto` at runtime means a missing or stale archive is ignored, not fatal.
RUN java -XX:ArchiveClassesAtExit=app.jsa \
         -Dspring.context.exit=onRefresh \
         -Dspring.main.lazy-initialization=false \
         -jar app.jar > /dev/null 2>&1 || true

EXPOSE 8080

# -XX:SharedArchiveFile   reuse the CDS archive built above
# -XX:TieredStopAtLevel=1 skip the C2 compiler; this service is I/O bound, and
#                         C1-only knocks seconds off startup
# -XX:MaxRAMPercentage    the free instance has 512MB; let the JVM size to the
#                         container rather than to the host
ENTRYPOINT ["java", \
  "-Xshare:auto", \
  "-XX:SharedArchiveFile=app.jsa", \
  "-XX:TieredStopAtLevel=1", \
  "-XX:MaxRAMPercentage=70", \
  "-XX:+UseSerialGC", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
