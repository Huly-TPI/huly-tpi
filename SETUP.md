# Setup del proyecto — Huly TPI

Guía para levantar la pila completa localmente: backend Java, microservicio Python SenseVoice y frontend React.

---

## 1. Qué instalar

| Herramienta | Versión mínima | Para qué |
|-------------|----------------|----------|
| [Java JDK](https://adoptium.net/) | 17 | Backend Spring Boot |
| [Node.js + npm](https://nodejs.org/) | 18 / 9 | Frontend React |
| [Python](https://www.python.org/downloads/) | 3.11+ | SenseVoice microservice |
| [ffmpeg](https://ffmpeg.org/download.html) | cualquiera | Conversión de audio en SenseVoice |
| [Docker + Docker Compose](https://www.docker.com/products/docker-desktop/) | — | PostgreSQL local |

**ffmpeg en Windows** (no viene por defecto):
```powershell
# PowerShell como Administrador
winget install Gyan.FFmpeg
# Reiniciar la terminal para que el PATH se actualice
```

Verificar instalaciones:
```bash
java -version       # openjdk 17 o superior
node -v             # v18.x o superior
python --version    # Python 3.11.x o superior
ffmpeg -version
docker -v
```

---

## 2. Variables de entorno del backend

```bash
# Desde la raíz del proyecto
cp backend/src/main/resources/application-huly-secrets.properties.example \
   backend/src/main/resources/application-huly-secrets.properties
```

Abrí el archivo copiado y completá con los valores reales del equipo. El `.example` tiene todos los campos explicados. Este archivo está en `.gitignore` — **nunca se commitea**.

> **JWT_SECRET** debe tener mínimo 32 caracteres. Con menos, JJWT lanza `WeakKeyException` al arrancar.

---

## 3. Microservicio Python (SenseVoice)

### Crear el entorno virtual

```bash
cd sensevoice-service

# Windows (PowerShell)
python -m venv venv
venv\Scripts\activate

# macOS / Linux
python3 -m venv venv
source venv/bin/activate
```

El prompt cambia a `(venv)` cuando el entorno está activado.

### Instalar dependencias

```bash
pip install -r requirements.txt
```

Instala FastAPI, faster-whisper, torch, transformers, librosa, pytest, httpx y el resto. Puede tardar varios minutos la primera vez por el tamaño de torch y transformers.

### Levantar el servidor

```bash
uvicorn main:app --host 0.0.0.0 --port 8001 --reload
```

> **Primera vez:** descarga automáticamente los modelos de IA desde HuggingFace (~3.5 GB). Puede tardar 5–10 minutos según la conexión. Las siguientes iniciadas son instantáneas (modelos en caché local).

Verificar que levantó:
```bash
curl http://localhost:8001/health
# {"status":"ok","whisper":true,"vad_model":true}
```

---

## 4. Backend Java

```bash
cd backend

# Primera vez: compilar todo
./mvnw clean install -DskipTests

# Levantar (perfil dev por defecto: H2 en memoria, no requiere PostgreSQL)
./mvnw spring-boot:run
```

En Windows PowerShell, si `./mvnw` no funciona:
```powershell
.\mvnw.cmd spring-boot:run
```

Verificar: `http://localhost:8080/api/examples/test` → `"Server is running!"`  
Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## 5. Frontend

```bash
cd frontend
npm install
npm run dev   # http://localhost:5173
```

El proxy de Vite ya está configurado: requests a `/api/*` se redirigen al backend en `http://localhost:8080`.

---

## 6. Orden de arranque recomendado

| Paso | Servicio | Puerto |
|------|----------|--------|
| 1 | PostgreSQL (solo para perfil qa/prod) — `docker compose up -d postgres` | 5432 |
| 2 | SenseVoice — `uvicorn main:app --host 0.0.0.0 --port 8001` | 8001 |
| 3 | Backend Java — `./mvnw spring-boot:run` | 8080 |
| 4 | Frontend — `npm run dev` | 5173 |

En perfil `dev` (por defecto) el backend usa H2 en memoria, por lo que el paso 1 no es necesario.

---

## 7. Correr los tests

### Python (pytest)

```bash
cd sensevoice-service
venv\Scripts\activate          # (o source venv/bin/activate en macOS/Linux)
pytest tests/ -v
```

Los tests mockean los modelos de IA — pasan en segundos, no descargan ni cargan nada.

### Java (JUnit + Mockito)

```bash
cd backend
./mvnw test                    # todos los tests
./mvnw verify                  # tests + reporte de cobertura JaCoCo
```

Reporte de cobertura HTML: `backend/target/site/jacoco/index.html`

### Frontend (Vitest)

```bash
cd frontend
npm run test              # una pasada
npm run test:watch        # modo watch (re-ejecuta al guardar)
npm run test:coverage     # con reporte de cobertura
```

---

## 8. Extensiones de VS Code recomendadas

### Java
- `vscjava.vscode-java-pack` — Extension Pack for Java (Language Support, Debugger, Test Runner, Maven)
- `vmware.vscode-spring-boot` — Spring Boot Tools

### Python (SenseVoice)
- `ms-python.python` — Python
- `ms-python.vscode-pylance` — Pylance

**Configurar el intérprete Python:**
1. `Ctrl+Shift+P` → "Python: Select Interpreter"
2. Elegir el de `sensevoice-service/venv/...`

**Habilitar pytest:**
1. `Ctrl+Shift+P` → "Python: Configure Tests"
2. Seleccionar **pytest** → directorio `sensevoice-service`

### Frontend
- `dbaeumer.vscode-eslint` — ESLint
- `bradlc.vscode-tailwindcss` — Tailwind CSS IntelliSense
- `vitest.explorer` — Vitest (panel de tests en la sidebar)
