"""
Huly Voice Microservice v5.0
- faster-whisper medium (int8, CPU): transcripción española de alta precisión
- wav2vec2-large-robust (audeering): detección emocional VAD (valencia,
  activación, dominancia) entrenado en habla espontánea multilingüe
"""

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
    """
    inputs = vad_processor(
        audio_array,
        sampling_rate=16000,
        return_tensors="pt",
        padding=True,
    )
    with torch.no_grad():
        logits = vad_model(inputs.input_values)   # [1, 3]

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
    Basado en el modelo circumplejo de Russell (activación-valencia) con dominancia.
    Valores en [0, 1]: valence 0=negativo 1=positivo, arousal 0=calmado 1=activado.
    """
    if valence >= 0.6:
        return "happy" if arousal >= 0.5 else "neutral"
    if valence <= 0.4:
        if arousal >= 0.55:
            return "angry" if dominance >= 0.5 else "fearful"
        return "sad"
    if arousal >= 0.6:
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
        logger.info("Modelo VAD cargado.")
    except Exception as exc:
        logger.error(f"Error cargando modelo VAD: {exc}")
        raise

    yield

    logger.info("Liberando modelos...")
    whisper_model = vad_processor = vad_model = None


app = FastAPI(
    title="Huly Voice Microservice",
    description="Transcripción española (Whisper medium) + análisis emocional VAD (audeering)",
    version="5.0.0",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["POST", "GET"],
    allow_headers=["*"],
)


class VadValues(BaseModel):
    arousal:   float
    dominance: float
    valence:   float

class VoiceAnalysisResponse(BaseModel):
    transcripcion:     str
    emocion_dominante: str
    idioma_detectado:  str
    vad:               VadValues


ALLOWED_CONTENT_TYPES = {
    "audio/wav", "audio/wave", "audio/webm", "audio/mpeg",
    "audio/ogg", "audio/flac", "audio/x-flac", "video/webm",
    "application/octet-stream",
}

@app.post("/analyze", response_model=VoiceAnalysisResponse)
async def analyze_audio(file: UploadFile = File(...)):
    """
    Recibe audio (WAV, WebM, MP3, OGG, FLAC), devuelve transcripción en español
    + valores VAD continuos y emoción dominante derivada.
    No se almacena ningún dato de voz.
    """
    if whisper_model is None or vad_model is None:
        raise HTTPException(status_code=503, detail="Modelos no disponibles aún.")

    ct = (file.content_type or "").lower()
    if ct not in ALLOWED_CONTENT_TYPES:
        raise HTTPException(status_code=415, detail=f"Tipo MIME no soportado: {ct}")

    if   "webm" in ct: suffix = ".webm"
    elif "mpeg" in ct: suffix = ".mp3"
    elif "ogg"  in ct: suffix = ".ogg"
    elif "flac" in ct: suffix = ".flac"
    else:              suffix = ".wav"

    tmp_path = wav_path = None

    try:
        with tempfile.NamedTemporaryFile(delete=False, suffix=suffix) as tmp:
            tmp_path = tmp.name
            tmp.write(await file.read())

        try:
            wav_path = convert_to_wav_16k(tmp_path)
        except RuntimeError as err:
            logger.error(f"Conversión de audio fallida: {err}")
            raise HTTPException(status_code=422, detail=f"Audio inválido: {err}")

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

        logger.info("Analizando emoción (VAD)...")
        audio_array, _ = sf.read(wav_path, dtype="float32", always_2d=False)
        vad             = predict_vad(audio_array)
        dominant        = vad_to_emotion(vad["arousal"], vad["dominance"], vad["valence"])
        logger.info(
            f"VAD → arousal={vad['arousal']:.3f}  dominance={vad['dominance']:.3f}  "
            f"valence={vad['valence']:.3f}  |  emoción: {dominant}"
        )

        return VoiceAnalysisResponse(
            transcripcion=transcripcion,
            emocion_dominante=dominant,
            idioma_detectado=idioma_detectado,
            vad=VadValues(**vad),
        )

    except HTTPException:
        raise
    except Exception as exc:
        logger.error(f"Error procesando audio: {exc}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"Error interno: {str(exc)}")
    finally:
        for path in (tmp_path, wav_path):
            if path and os.path.exists(path):
                try:
                    os.unlink(path)
                except OSError:
                    pass
        logger.info("Archivos temporales eliminados.")


@app.get("/health")
async def health():
    return {
        "status": "ok",
        "whisper":   whisper_model is not None,
        "vad_model": vad_model is not None,
    }
