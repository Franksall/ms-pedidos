#  Java 17.
FROM eclipse-temurin:17-jdk-alpine


LABEL maintainer="Franksall"

# --- CONFIGURACIÓN ---

WORKDIR /app

# --- COPIAR EL CÓDIGO ---

COPY build/libs/*.jar app.jar

# --- EXPOSICIÓN ---
# Ppuerto 8082 de ms-pedidos.
EXPOSE 8082

# --- EJECUCIÓN ---

ENTRYPOINT ["java", "-jar", "/app/app.jar"]