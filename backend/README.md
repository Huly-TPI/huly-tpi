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

La columna **Auth** indica si se requiere el header `Authorization: Bearer <accessToken>`:
- **No** → endpoint público (`/api/auth/**`, `POST /api/leads`, `GET /api/breathing/techniques`, `POST /api/webhook/mercadopago` y Swagger).
- **Bearer** → requiere un usuario autenticado.
- **Bearer (admin)** → endpoint de backoffice; pensado para usuarios con rol ADMIN.

> Una colección Postman con todos estos endpoints (con bodies de ejemplo) está en [`postman/Huly_TPI.postman_collection.json`](../postman/Huly_TPI.postman_collection.json).

### Auth — `/api/auth`

| Método | Ruta | Descripción | Auth |
|--------|------|-------------|------|
| `POST` | `/api/auth/register` | Registra un nuevo usuario (rol USER) | No |
| `POST` | `/api/auth/login` | Inicia sesión, devuelve access token + cookie refresh | No |
| `POST` | `/api/auth/backoffice/login` | Login de backoffice (valida rol ADMIN) | No |
| `POST` | `/api/auth/refresh` | Rota el refresh token (requiere cookie) | No |
| `POST` | `/api/auth/logout` | Invalida el refresh token y limpia la cookie | No |

**Register** — body:
```json
{ "name": "Juan Perez", "email": "usuario@huly.com", "password": "password123", "birthDate": "2000-05-20" }
```

**Login** — body `{ "email": "usuario@huly.com", "password": "password123" }`.
Respuesta: `{ "accessToken": "...", "role": "USER", "onBoardingCompleted": false }` + cookie HTTP-only `refreshToken`.

El access token se envía en los endpoints protegidos como `Authorization: Bearer <accessToken>`.

### Users — `/api/users`

| Método | Ruta | Descripción | Auth |
|--------|------|-------------|------|
| `GET` | `/api/users/me` | Perfil del usuario autenticado | Bearer |
| `GET` | `/api/users/me/coins` | Monedas del usuario | Bearer |
| `GET` | `/api/users/me/membership` | Estado de membresía/plan | Bearer |
| `PUT` | `/api/users/me/theme` | Actualiza preferencia de tema (`LIGHT` \| `DARK`) | Bearer |

### Onboarding — `/api/onboarding`

| Método | Ruta | Descripción | Auth |
|--------|------|-------------|------|
| `POST` | `/api/onboarding/generate-options` | Genera opciones de onboarding (IA) | Bearer |
| `POST` | `/api/onboarding/complete` | Guarda las respuestas del onboarding | Bearer |
| `POST` | `/api/onboarding/tutorial/complete` | Marca el tutorial como completado | Bearer |
| `POST` | `/api/onboarding/profile-onboarding-tutorial/complete` | Marca el tutorial de perfil como completado | Bearer |

### Chat — `/api/chat`

| Método | Ruta | Descripción | Auth |
|--------|------|-------------|------|
| `POST` | `/api/chat` | Envía un mensaje al chatbot | Bearer |
| `POST` | `/api/chat/challenge-decision` | Registra la decisión sobre un reto sugerido | Bearer |
| `GET` | `/api/chat/{conversationId}/messages` | Historial paginado de una conversación | Bearer |

### Clouds — `/api/clouds`

| Método | Ruta | Descripción | Auth |
|--------|------|-------------|------|
| `POST` | `/api/clouds/thought` | Guarda un pensamiento del usuario | Bearer |
| `POST` | `/api/clouds/recommendation` | Recomendación de actividad según pensamientos | Bearer |

### Journal — `/api/journal`

| Método | Ruta | Descripción | Auth |
|--------|------|-------------|------|
| `GET` | `/api/journal` | Lista las entradas del diario | Bearer |
| `POST` | `/api/journal` | Crea una entrada de diario | Bearer |

### Emotional Events — `/api/emotional-events`

| Método | Ruta | Descripción | Auth |
|--------|------|-------------|------|
| `POST` | `/api/emotional-events` | Crea un evento emocional | Bearer |
| `PATCH` | `/api/emotional-events/{id}/decision` | Actualiza la decisión de la recomendación | Bearer |
| `PATCH` | `/api/emotional-events/{id}/feedback` | Registra feedback (score 1–5) | Bearer |

### Emotional Recommendations — `/api/emotional-recommendations`

| Método | Ruta | Descripción | Auth |
|--------|------|-------------|------|
| `POST` | `/api/emotional-recommendations` | Recomendaciones según estado emocional (VAD) | Bearer |

### Emotional States — `/api/emotional-states`

| Método | Ruta | Descripción | Auth |
|--------|------|-------------|------|
| `POST` | `/api/emotional-states` | Guarda el estado emocional (VAD) del usuario | Bearer |

### Activities — `/api/activities`

| Método | Ruta | Descripción | Auth |
|--------|------|-------------|------|
| `GET` | `/api/activities` | Lista las actividades disponibles | Bearer |
| `POST` | `/api/activities/sessions` | Registra una sesión de actividad | Bearer |

### Breathing — `/api/breathing`

| Método | Ruta | Descripción | Auth |
|--------|------|-------------|------|
| `GET` | `/api/breathing/techniques` | Lista las técnicas de respiración | No |

### Badges — `/api/badges`

| Método | Ruta | Descripción | Auth |
|--------|------|-------------|------|
| `GET` | `/api/badges` | Catálogo de insignias | Bearer |
| `GET` | `/api/badges/my` | Insignias obtenidas por el usuario | Bearer |

### Daily Rewards — `/api/daily-rewards`

| Método | Ruta | Descripción | Auth |
|--------|------|-------------|------|
| `GET` | `/api/daily-rewards/status` | Estado de la recompensa diaria (racha) | Bearer |
| `POST` | `/api/daily-rewards/claim` | Reclama la recompensa del día | Bearer |

### User Goals — `/api/user-goals`

| Método | Ruta | Descripción | Auth |
|--------|------|-------------|------|
| `POST` | `/api/user-goals/accept` | Acepta un reto sugerido | Bearer |
| `POST` | `/api/user-goals` | Crea un reto | Bearer |
| `GET` | `/api/user-goals/me` | Lista los retos (completados y pendientes) | Bearer |
| `PUT` | `/api/user-goals/{id}` | Edita un reto | Bearer |
| `PATCH` | `/api/user-goals/{id}/complete` | Completa un reto (imagen opcional, multipart; se sube al bucket y devuelve la URL pública) | Bearer |
| `DELETE` | `/api/user-goals/{id}` | Elimina un reto | Bearer |

### Risk Words — `/api/risk-words`

| Método | Ruta | Descripción | Auth |
|--------|------|-------------|------|
| `POST` | `/api/risk-words` | Crea una palabra de riesgo | Bearer |
| `PUT` | `/api/risk-words/{id}` | Actualiza una palabra de riesgo | Bearer |
| `DELETE` | `/api/risk-words/{id}` | Elimina una palabra de riesgo | Bearer |
| `GET` | `/api/risk-words` | Lista con filtros (`word`, `active`, `severity`) y paginación | Bearer |

> Severidades válidas: `LOW` \| `MEDIUM` \| `HIGH`.

### Extension — `/api/extension`

| Método | Ruta | Descripción | Auth |
|--------|------|-------------|------|
| `GET` | `/api/extension/settings` | Configuración anti-scroll del usuario | Bearer |
| `POST` | `/api/extension/settings` | Guarda la configuración anti-scroll | Bearer |
| `POST` | `/api/extension/metrics` | Envía un lote de métricas de uso | Bearer |

### Payment — `/api/payment` y `/api/webhook`

| Método | Ruta | Descripción | Auth |
|--------|------|-------------|------|
| `GET` | `/api/payment/products` | Lista los productos (paquetes de monedas) | Bearer |
| `GET` | `/api/payment/plans` | Lista los planes de membresía | Bearer |
| `POST` | `/api/payment/preference/{productId}` | Crea una preferencia de pago (MercadoPago) | Bearer |
| `POST` | `/api/webhook/mercadopago` | Webhook de notificaciones de MercadoPago | No |

### Leads — `/api/leads`

| Método | Ruta | Descripción | Auth |
|--------|------|-------------|------|
| `POST` | `/api/leads` | Registra un lead desde la landing | No |

### Admin — Backoffice

| Método | Ruta | Descripción | Auth |
|--------|------|-------------|------|
| `GET` | `/api/admin/users` | Lista usuarios (búsqueda opcional) | Bearer (admin) |
| `GET` | `/api/admin/users/{id}/statistics/activities` | Estadísticas de actividades de un usuario | Bearer (admin) |
| `GET` | `/api/admin/users/{id}/statistics/ai` | Diagnósticos de IA de un usuario | Bearer (admin) |
| `GET` | `/api/admin/users/{id}/statistics/finance` | Resumen financiero de un usuario | Bearer (admin) |
| `GET` | `/api/admin/users/{id}/statistics/antiscroll` | Métricas anti-scroll de un usuario | Bearer (admin) |
| `GET` | `/api/admin/users/antiscroll/dashboard` | Dashboard global anti-scroll | Bearer (admin) |
| `GET` | `/api/admin/users/antiscroll/config` | Config global anti-scroll | Bearer (admin) |
| `POST` | `/api/admin/users/antiscroll/config` | Actualiza la config global anti-scroll | Bearer (admin) |
| `GET` | `/api/admin/chat/config` | Configuración del chatbot | Bearer (admin) |
| `PUT` | `/api/admin/chat/config` | Actualiza la configuración del chatbot | Bearer (admin) |
| `GET` | `/api/admin/chatbot/emotional-categories` | Categorías emocionales (mock) | Bearer (admin) |
| `GET` | `/api/admin/chatbot/activities` | Actividades más usadas (mock) | Bearer (admin) |
| `GET` | `/api/admin/chatbot/wellbeing` | Bienestar semanal (mock) | Bearer (admin) |
| `GET` | `/api/admin/chatbot/training-logs` | Logs de entrenamiento (mock) | Bearer (admin) |


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
