# ── Huly Voice Microservice v4.0 ─────────────────────────────────────────────
# CPU-only. Para GPU: cambiar a pytorch/pytorch:2.3.0-cuda12.1-cudnn8-runtime
FROM python:3.11-slim

WORKDIR /app

# Dependencias del sistema: ffmpeg (conversión de audio) + libsndfile (soundfile)
RUN apt-get update && apt-get install -y --no-install-recommends \
        ffmpeg \
        libsndfile1 \
    && rm -rf /var/lib/apt/lists/*

# Dependencias Python
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Predescargar modelos en la imagen para que el arranque en producción sea inmediato.
# HF_HOME define dónde se guarda la caché dentro de la imagen.
ENV HF_HOME=/app/.cache/huggingface \
    PYTHONUNBUFFERED=1 \
    TOKENIZERS_PARALLELISM=false

RUN python -c "\
from huggingface_hub import snapshot_download; \
snapshot_download('Systran/faster-whisper-medium'); \
snapshot_download('audeering/wav2vec2-large-robust-12-ft-emotion-msp-dim')"

# Código de la aplicación (después de los modelos para no invalidar la capa de caché)
COPY main.py .

EXPOSE 8001

CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8001", "--workers", "1"]
