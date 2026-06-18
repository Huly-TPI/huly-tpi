/<div align="center">

# Huly TPI — Backend

<p>
  <img src="https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white"/>
  <img src="https://img.shields.io/badge/Spring_Boot-4.0.6-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"/>
  <img src="https://img.shields.io/badge/PostgreSQL-16-4169E1?style=for-the-badge&logo=postgresql&logoColor=white"/>
  <img src="https://img.shields.io/badge/Swagger-OpenAPI_3-85EA2D?style=for-the-badge&logo=swagger&logoColor=black"/>
  <img src="https://img.shields.io/badge/Spring_AI-2.0.0--M4-6DB33F?style=for-the-badge&logo=spring&logoColor=white"/>
</p>

API REST del proyecto Huly TPI construida con arquitectura limpia sobre Spring Boot.

</div>

---

## Tabla de contenidos

- [Stack tecnológico](#stack-tecnológico)
- [Arquitectura](#arquitectura)
- [Requisitos previos](#requisitos-previos)
- [Instalación y ejecución](#instalación-y-ejecución)
- [Perfiles de entorno](#perfiles-de-entorno)
- [Variables de entorno](#variables-de-entorno)
- [Endpoints disponibles](#endpoints-disponibles)
- [Documentación Swagger](#documentación-swagger)
- [Tests](#tests)
- [Estructura del proyecto](#estructura-del-proyecto)

---

## Stack tecnológico

| Tecnología | Versión | Uso |
|---|---|---|
| Java | 17 | Lenguaje principal |
| Spring Boot | 4.0.6 | Framework base |
| Spring Security | — | Seguridad y CORS |
| Spring Data JPA | — | Acceso a datos |
| Spring WebSocket | — | Comunicación en tiempo real |
| Spring AI | 2.0.0-M4 | Chat memory con JDBC |
| PostgreSQL | 16 | Base de datos principal |
| H2 | — | Base de datos en memoria para tests |
| Flyway | — | Migraciones de base de datos |
| SpringDoc OpenAPI | 3.0.2 | Documentación Swagger |
| Lombok | — | Reducción de boilerplate |
| JaCoCo | — | Cobertura de tests |

---

## Arquitectura

El proyecto sigue **Clean Architecture** separando responsabilidades en tres capas:

```
presentation/        ← HTTP: controllers y DTOs
domain/              ← Lógica de negocio: models, services, use cases, repositories (interfaces)
infrastructure/      ← Implementaciones: JPA, configs, providers
```

> La capa `domain` no depende de ninguna capa externa. `infrastructure` y `presentation` dependen de `domain`, nunca al revés.

---

## Requisitos previos

Antes de comenzar verificá que tenés instalado:

- **Java 17** — [Descargar](https://adoptium.net/)
- **Maven 3.9+** — [Descargar](https://maven.apache.org/download.cgi) _(o usar el wrapper `./mvnw` incluido)_
- **Docker con Docker Compose** — PostgreSQL 16 + pgvector para desarrollo local
- **Git**

Verificar instalaciones:

```bash
java -version
mvn -version   # o ./mvnw -version
```

---

## Instalación y ejecución

### 1. Clonar el repositorio

```bash
git clone 
cd huly-tpi/backend
```

### 2. Compilar el proyecto

```bash
./mvnw clean install -DskipTests
```

### 3. Levantar PostgreSQL para desarrollo

Desde la raíz del repositorio:

```bash
docker compose up -d
```

El contenedor expone PostgreSQL en `localhost:5433`.

### 4. Ejecutar en modo desarrollo

```bash
./mvnw spring-boot:run
```

O con el perfil explícito:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

La aplicación estará disponible en: `http://localhost:8080`

### 5. Verificar que levantó correctamente

Abrí la documentación de la API (Swagger UI) en el navegador:

```
http://localhost:8080/swagger-ui.html
```

Deberías ver la interfaz de Swagger con los endpoints disponibles (habilitada en los perfiles `dev` y `qa`).

---

## Perfiles de entorno

El proyecto tiene tres perfiles. Se activa mediante la variable `SPRING_PROFILES_ACTIVE` o el flag de Maven.

| Perfil | Base de datos | Flyway | Swagger | Uso |
|--------|--------------|--------|---------|-----|
| `dev` | PostgreSQL + pgvector | Habilitado | ✅ Habilitado | Desarrollo local |
| `qa` | PostgreSQL | Habilitado | ✅ Habilitado | Testing y QA |
| `prod` | PostgreSQL | Habilitado | ❌ Deshabilitado | Producción |

El perfil por defecto es `dev`.

---

## Variables de entorno

### Perfil `dev`

El perfil `dev` carga secretos desde `application-huly-secrets.properties`, que **no se versiona** (está en `.gitignore`).

1. Copiá el archivo de ejemplo:

```bash
cp src/main/resources/application-huly-secrets.properties.example \
   src/main/resources/application-huly-secrets.properties
```

2. Editá `application-huly-secrets.properties` con tus valores locales.

| Variable | Descripción | Ejemplo |
|----------|-------------|---------|
| `PORT` | Puerto del servidor | `8080` |
| `FRONTEND_URL` | URL del frontend (CORS) | `http://localhost:5173` |
| `LANDING_URL` | URL de la landing page (CORS) | `http://localhost:3000` |
| `SPRING_DATASOURCE_URL` | URL de conexión a PostgreSQL | `jdbc:postgresql://localhost:5433/huly` |
| `SPRING_DATASOURCE_USERNAME` | Usuario de la base de datos | `huly_user` |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña de la base de datos | `huly_pass` |
| `SSL_KEY_STORE_PASSWORD` | Contraseña del keystore SSL (solo si `ssl.enabled=true`) | `secret123` |
| `JWT_SECRET` | Clave secreta para firmar JWTs — **mínimo 32 caracteres** | `MiClaveSecreta_minimo32chars_ok!` |

> **Importante:** `JWT_SECRET` debe tener al menos 32 caracteres (256 bits). Con menos caracteres JJWT lanza `WeakKeyException` al arrancar el login.

### Perfiles `qa` y `prod`

| Variable | Descripción | Ejemplo |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Perfil activo | `prod` |
| `PORT` | Puerto del servidor | `8080` |
| `FRONTEND_URL` | URL del frontend (CORS) | `https://mi-frontend.com` |
| `SPRING_DATASOURCE_URL` | URL de conexión a PostgreSQL | `jdbc:postgresql://host:5432/db` |
| `SPRING_DATASOURCE_USERNAME` | Usuario de la base de datos | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña de la base de datos | `secret` |
| `JWT_SECRET` | Clave secreta para firmar JWTs — **mínimo 32 caracteres** | _(generar con `openssl rand -base64 48`)_ |
| `JWT_ACCESS_TOKEN_EXPIRATION_MS` | Duración del access token en ms | `3600000` (1h) |
| `JWT_REFRESH_TOKEN_EXPIRATION_MS` | Duración del refresh token en ms | `604800000` (7d) |

Ejemplo para ejecutar con perfil `prod`:

```bash
export SPRING_PROFILES_ACTIVE=prod
export PORT=8080
export FRONTEND_URL=https://mi-frontend.com
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/huly
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=secret
export JWT_SECRET=$(openssl rand -base64 48)

./mvnw spring-boot:run
```

---

## Endpoints disponibles

### Auth

| Método | Ruta | Descripción | Auth requerida |
|--------|------|-------------|----------------|
| `POST` | `/api/auth/register` | Registra un nuevo usuario | No |
| `POST` | `/api/auth/login` | Inicia sesión, devuelve access token + cookie | No |
| `POST` | `/api/auth/refresh` | Rota el refresh token (requiere cookie) | No |
| `POST` | `/api/auth/logout` | Invalida el refresh token y limpia la cookie | No |

**Register** — body:
```json
{
  "name": "Juan",
  "email": "usuario@huly.com",
  "password": "password123"
}
```

**Login** — body:
```json
{
  "email": "usuario@huly.com",
  "password": "password123"
}
```
Respuesta: `{ "accessToken": "...", "role": "USER" }` + cookie HTTP-only `refreshToken`.

El access token debe enviarse en los endpoints protegidos como:
```
Authorization: Bearer <accessToken>
```

### Math

| Método | Ruta | Descripción | Body |
|--------|------|-------------|------|
| `POST` | `/api/math/sum` | Suma dos números enteros | `{ "a": number, "b": number }` |


---

## Documentación Swagger

La UI de Swagger está disponible en los perfiles `dev` y `qa`.

```
http://localhost:8080/swagger-ui.html
```

La especificación OpenAPI en formato JSON:

```
http://localhost:8080/v3/api-docs
```

> En producción, Swagger está **deshabilitado** por seguridad.

---

## Tests

### Ejecutar todos los tests

```bash
./mvnw test
```

### Ejecutar con reporte de cobertura

```bash
./mvnw verify
```

El reporte HTML de cobertura (JaCoCo) se genera en:

```
target/site/jacoco/index.html
```

### Perfiles de test disponibles

| Archivo | Base de datos | Uso |
|---------|--------------|-----|
| `application-h2-test.properties` | H2 en memoria | Tests unitarios e integración rápida |
| `application-coverage-test.properties` | PostgreSQL | Tests de cobertura con base de datos real |

---

## Estructura del proyecto

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/huly/backend/
│   │   │   ├── BackendApplication.java
│   │   │   ├── domain/
│   │   │   │   ├── model/              ← Entidades de dominio
│   │   │   │   ├── repository/         ← Interfaces de repositorio
│   │   │   │   ├── service/            ← Servicios de dominio
│   │   │   │   ├── useCase/            ← Casos de uso
│   │   │   │   └── provider/           ← Interfaces de providers
│   │   │   ├── infrastructure/
│   │   │   │   ├── config/             ← Configuraciones Spring (Security, CORS, Swagger)
│   │   │   │   ├── repository/
│   │   │   │   │   ├── entity/         ← Entidades JPA
│   │   │   │   │   └── jpaRepository/  ← Implementaciones JPA
│   │   │   │   └── provider/           ← Implementaciones de providers
│   │   │   └── presentation/
│   │   │       ├── controller/         ← Controllers REST
│   │   │       └── dto/                ← Request y Response DTOs
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       ├── application-huly-secrets.properties         ← ignorado por git (local)
│   │       ├── application-huly-secrets.properties.example ← plantilla para nuevos devs
│   │       ├── application-qa.properties
│   │       ├── application-prod.properties
│   │       └── db/migration/           ← Scripts Flyway (V1__, V2__, ...)
│   └── test/
│       ├── java/com/huly/backend/
│       └── resources/
│           ├── application-h2-test.properties
│           └── application-coverage-test.properties
└── pom.xml
```
