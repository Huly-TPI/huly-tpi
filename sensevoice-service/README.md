# Huly Voice Microservice

Microservicio Python/FastAPI que combina **SenseVoice** (transcripción en español)
y **emotion2vec+** (reconocimiento de emociones acústicas con scores por emoción).

Consumido por el backend Java de Huly. No responde directamente al usuario —
solo produce datos que el chatbot usa como contexto.

---

## Output de ejemplo

```json
{
  "transcripcion": "hoy me siento muy cansada y no sé por qué",
  "emocion_dominante": "sad",
  "idioma_detectado": "es",
  "emociones": {
    "neutral":   0.61,
    "sad":       0.22,
    "fearful":   0.08,
    "happy":     0.04,
    "angry":     0.03,
    "surprised": 0.02,
    "disgusted": 0.0,
    "unknown":   0.0
  }
}
```

---

## Setup local

### Requisitos
- Python 3.11+
- ffmpeg instalado en el sistema (`brew install ffmpeg` / `apt install ffmpeg`)
- 4 GB de RAM mínimo (los modelos ocupan ~1.5 GB en CPU)

### Instalación

```bash
# 1. Crear entorno virtual
python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate

# 2. Instalar dependencias
pip install -r requirements.txt

# 3. Iniciar el servidor (los modelos se descargan automáticamente al primer inicio)
uvicorn main:app --host 0.0.0.0 --port 8001 --reload
```

La primera vez tarda ~5 minutos en descargar los modelos desde ModelScope/HuggingFace.
Las siguientes iniciadas son instantáneas (modelos en caché local).

### Verificar que funciona

```bash
# Health check
curl http://localhost:8001/health

# Probar con un audio
curl -X POST http://localhost:8001/analyze \
  -F "file=@tu_audio.wav"
```

---

## Deploy en Render

### Opción A — Docker (recomendado)

1. En Render, crear un nuevo **Web Service**
2. Conectar el repositorio y apuntar al `Dockerfile` de este directorio
3. Configurar:
   - **Plan**: Standard (1 GB RAM mínimo; 2 GB recomendado)
   - **Puerto**: 8001
   - **Health check path**: `/health`

> ⚠️ El plan Free de Render no es suficiente para los modelos de IA.
> Usar Standard ($7/mes) como mínimo.

### Opción B — Python nativo

En Render, crear un Web Service con:
- **Build command**: `pip install -r requirements.txt`
- **Start command**: `uvicorn main:app --host 0.0.0.0 --port $PORT`

Agregar variable de entorno si hace falta:
- `TRANSFORMERS_CACHE=/opt/render/project/.cache`

---

## Variables de entorno

| Variable | Default | Descripción |
|---|---|---|
| `PORT` | `8001` | Puerto del servidor (Render lo inyecta automáticamente) |
| `TRANSFORMERS_CACHE` | `~/.cache` | Directorio de caché de modelos |

---

## Integración con el backend Java

El backend Java consume este microservicio a través de `VoiceMicroserviceAdapter`.
Configurar en `application.properties`:

```properties
voice.microservice.url=http://localhost:8001          # local
voice.microservice.url=https://huly-voice.onrender.com  # producción
```

---

## Privacidad y ética

- El audio se guarda en un archivo temporal **solo durante el procesamiento**
- Se elimina inmediatamente después, en el bloque `finally`
- No se persiste ningún dato de voz en disco ni en base de datos
- Solo el JSON de resultado (`transcripcion` + `emociones`) puede ser almacenado por el backend Java
- Requiere consentimiento explícito del usuario antes de grabar (responsabilidad del frontend)

---

## Ramas

Seguir la convención del proyecto:

```
feature/SCRUM-XXX-voice-microservice
```
