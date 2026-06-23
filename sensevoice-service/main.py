"""
Huly Voice Microservice v7.0
- faster-whisper medium (int8, CPU): transcripción española de alta precisión
- wav2vec2-large-robust (audeering, float32): detección emocional VAD
  (valencia, activación, dominancia) en precisión nativa del modelo

Requiere ~2.25 GB RAM. Optimizado para Azure Container Apps (4 GB disponibles).

Endpoints:
  POST /transcribe  → transcripción (Whisper medium)
  POST /analyze     → análisis emocional VAD (wav2vec2-large float32)
  GET  /health      → estado de los modelos
"""

import gc
import os
import subprocess
import tempfile
import logging
from contextlib import asynccontextmanager

import numpy as np
import soundfile as sf
import torch
import torch.nn as nn
from fastapi import FastAPI, UploadFile, File, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from transformers import Wav2Vec2Processor
from transformers.models.wav2vec2.modeling_wav2vec2 import (
    Wav2Vec2Model,
    Wav2Vec2PreTrainedModel,
)

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


WHISPER_MODEL_SIZE = "medium"
VAD_MODEL_ID       = "audeering/wav2vec2-large-robust-12-ft-emotion-msp-dim"


class RegressionHead(nn.Module):
    def __init__(self, config):
        super().__init__()
        self.dense    = nn.Linear(config.hidden_size, config.hidden_size)
        self.dropout  = nn.Dropout(config.final_dropout)
        self.out_proj = nn.Linear(config.hidden_size, config.num_labels)

    def forward(self, features, **kwargs):
        x = self.dropout(features)
        x = self.dense(x)
        x = torch.tanh(x)
        x = self.dropout(x)
        return self.out_proj(x)


class EmotionModel(Wav2Vec2PreTrainedModel):
    """wav2vec2-large-robust fine-tuned para regresión VAD (arousal, dominance, valence)."""

    _tied_weights_keys = []

    def __init__(self, config):
        super().__init__(config)
        self.wav2vec2   = Wav2Vec2Model(config)
        self.classifier = RegressionHead(config)
        self.post_init()

    def forward(self, input_values):
        outputs       = self.wav2vec2(input_values)
        hidden_states = torch.mean(outputs[0], dim=1)
        return self.classifier(hidden_states)


whisper_model = None
vad_processor = None
vad_model     = None


def convert_to_wav_16k(input_path: str) -> str:
    wav_path = input_path + "_16k.wav"
    result = subprocess.run(
        ["ffmpeg", "-y", "-i", input_path, "-ar", "16000", "-ac", "1", "-f", "wav", wav_path],
        capture_output=True,
        text=True,
    )
    if result.returncode != 0:
        raise RuntimeError(f"ffmpeg falló (código {result.returncode}): {result.stderr[-400:]}")
    return wav_path


def predict_vad(audio_array: np.ndarray) -> dict:
    """
    Predice valores VAD a partir de un array numpy float32 a 16 kHz.
    Devuelve {'arousal': float, 'dominance': float, 'valence': float} en [0, 1].
    Orden de salida del modelo: [arousal, dominance, valence].
    Corre en float32 (precisión nativa del modelo).
    """
    inputs = vad_processor(
        audio_array,
        sampling_rate=16000,
        return_tensors="pt",
        padding=True,
    )
    with torch.no_grad():
        logits = vad_model(inputs.input_values)

    vals      = logits[0].cpu().numpy()
    arousal   = float(np.clip(vals[0], 0.0, 1.0))
    dominance = float(np.clip(vals[1], 0.0, 1.0))
    valence   = float(np.clip(vals[2], 0.0, 1.0))
    return {
        "arousal":   round(arousal,   4),
        "dominance": round(dominance, 4),
        "valence":   round(valence,   4),
    }


def vad_to_emotion(arousal: float, dominance: float, valence: float) -> str:
    """
    Mapeo VAD → etiqueta de emoción dominante.
    Calibrado para el modelo audeering (entrenado en inglés) con voz española:
    el modelo sobreestima valence en ira española; arousal+dominance son más fiables.
    """
    # Ira/frustración: alta activación + alta dominancia (patrón cross-lingüístico estable).
    # Valence < 0.70 descarta voces genuinamente alegres/entusiastas.
    if arousal >= 0.55 and dominance >= 0.60 and valence < 0.70:
        return "angry"
    if valence >= 0.60:
        return "happy" if arousal >= 0.50 else "neutral"
    if valence <= 0.40:
        if arousal >= 0.55:
            return "fearful"
        return "sad"
    if arousal >= 0.65:
        return "surprised"
    return "neutral"


@asynccontextmanager
async def lifespan(app: FastAPI):
    global whisper_model, vad_processor, vad_model

    logger.info(f"Cargando faster-whisper {WHISPER_MODEL_SIZE} (int8 CPU)...")
    try:
        from faster_whisper import WhisperModel
        whisper_model = WhisperModel(WHISPER_MODEL_SIZE, device="cpu", compute_type="int8")
        logger.info("Whisper cargado.")
    except Exception as exc:
        logger.error(f"Error cargando Whisper: {exc}")
        raise

    logger.info(f"Cargando modelo VAD: {VAD_MODEL_ID}...")
    try:
        vad_processor = Wav2Vec2Processor.from_pretrained(VAD_MODEL_ID)
        vad_model     = EmotionModel.from_pretrained(VAD_MODEL_ID)
        vad_model.eval()
        logger.info("Modelo VAD cargado (float32).")
        logger.info("Ejecutando warm-up del modelo VAD...")
        predict_vad(np.zeros(16000, dtype=np.float32))
        logger.info("Warm-up completo.")
    except Exception as exc:
        logger.error(f"Error cargando modelo VAD: {exc}")
        raise

    yield

    logger.info("Liberando modelos...")
    whisper_model = vad_processor = vad_model = None


app = FastAPI(
    title="Huly Voice Microservice",
    description="Transcripción española (Whisper medium) + análisis emocional VAD (audeering, float32)",
    version="7.0.0",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["POST", "GET"],
    allow_headers=["*"],
)

ALLOWED_CONTENT_TYPES = {
    "audio/wav", "audio/wave", "audio/webm", "audio/mpeg",
    "audio/ogg", "audio/flac", "audio/x-flac", "video/webm",
    "application/octet-stream",
}


class TranscriptionResponse(BaseModel):
    transcripcion:    str
    idioma_detectado: str


class VadValues(BaseModel):
    arousal:   float
    dominance: float
    valence:   float


class EmotionResponse(BaseModel):
    emocion_dominante: str
    vad:               VadValues



def _suffix_for(content_type: str) -> str:
    if "webm" in content_type: return ".webm"
    if "mpeg" in content_type: return ".mp3"
    if "ogg"  in content_type: return ".ogg"
    if "flac" in content_type: return ".flac"
    return ".wav"


async def _save_and_convert(file: UploadFile) -> tuple[str, str]:
    """Guarda el audio subido y lo convierte a WAV 16 kHz. Retorna (tmp_path, wav_path)."""
    ct = (file.content_type or "").lower()
    if ct not in ALLOWED_CONTENT_TYPES:
        raise HTTPException(status_code=415, detail=f"Tipo MIME no soportado: {ct}")

    tmp_path = None
    with tempfile.NamedTemporaryFile(delete=False, suffix=_suffix_for(ct)) as tmp:
        tmp_path = tmp.name
        tmp.write(await file.read())

    try:
        wav_path = convert_to_wav_16k(tmp_path)
    except RuntimeError as err:
        os.unlink(tmp_path)
        logger.error(f"Conversión de audio fallida: {err}")
        raise HTTPException(status_code=422, detail=f"Audio inválido: {err}")

    return tmp_path, wav_path


def _cleanup(*paths) -> None:
    for path in paths:
        if path and os.path.exists(path):
            try:
                os.unlink(path)
            except OSError:
                pass



@app.post("/transcribe", response_model=TranscriptionResponse)
async def transcribe_audio(file: UploadFile = File(...)):
    """
    Recibe audio (WAV, WebM, MP3, OGG, FLAC) y devuelve la transcripción en español.
    No se almacena ningún dato de voz.
    """
    if whisper_model is None:
        raise HTTPException(status_code=503, detail="Modelo de transcripción no disponible aún.")

    tmp_path = wav_path = None
    try:
        tmp_path, wav_path = await _save_and_convert(file)

        logger.info(f"Transcribiendo '{file.filename}'...")
        segments, info = whisper_model.transcribe(
            wav_path,
            language="es",
            beam_size=5,
            vad_filter=True,
            vad_parameters={"min_silence_duration_ms": 300},
        )
        transcripcion    = " ".join(seg.text.strip() for seg in segments).strip()
        idioma_detectado = info.language or "es"
        logger.info(f"Transcripción: '{transcripcion[:120]}'")

        return TranscriptionResponse(
            transcripcion=transcripcion,
            idioma_detectado=idioma_detectado,
        )

    except HTTPException:
        raise
    except Exception as exc:
        logger.error(f"Error transcribiendo audio: {exc}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"Error interno: {str(exc)}")
    finally:
        _cleanup(tmp_path, wav_path)
        logger.info("Archivos temporales eliminados.")


@app.post("/analyze", response_model=EmotionResponse)
async def analyze_emotion(file: UploadFile = File(...)):
    """
    Recibe audio y devuelve los valores VAD continuos (arousal, dominance, valence)
    y la emoción dominante derivada. No se almacena ningún dato de voz.
    """
    if vad_model is None:
        raise HTTPException(status_code=503, detail="Modelo de emoción no disponible aún.")

    tmp_path = wav_path = None
    try:
        tmp_path, wav_path = await _save_and_convert(file)

        logger.info("Analizando emoción (VAD)...")
        audio_array, _ = sf.read(wav_path, dtype="float32", always_2d=False)

        gc.collect()

        vad      = predict_vad(audio_array)
        dominant = vad_to_emotion(vad["arousal"], vad["dominance"], vad["valence"])
        logger.info(
            f"VAD → arousal={vad['arousal']:.3f}  dominance={vad['dominance']:.3f}  "
            f"valence={vad['valence']:.3f}  |  emoción: {dominant}"
        )

        return EmotionResponse(
            emocion_dominante=dominant,
            vad=VadValues(**vad),
        )

    except HTTPException:
        raise
    except Exception as exc:
        logger.error(f"Error analizando emoción: {exc}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"Error interno: {str(exc)}")
    finally:
        _cleanup(tmp_path, wav_path)
        logger.info("Archivos temporales eliminados.")


@app.get("/health")
async def health():
    return {
        "status": "ok",
        "whisper":   whisper_model is not None,
        "vad_model": vad_model is not None,
    }
