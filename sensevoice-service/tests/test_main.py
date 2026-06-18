import io
import struct
import wave
from unittest.mock import MagicMock, patch

import numpy as np
import pytest
from starlette.testclient import TestClient

import main as main_module
from main import app, convert_to_wav_16k, vad_to_emotion


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def _make_wav_bytes(num_samples: int = 1600, sample_rate: int = 16000) -> bytes:
    """Return minimal in-memory WAV file (16-bit mono silence)."""
    buf = io.BytesIO()
    with wave.open(buf, "wb") as wf:
        wf.setnchannels(1)
        wf.setsampwidth(2)
        wf.setframerate(sample_rate)
        wf.writeframes(struct.pack(f"<{num_samples}h", *([0] * num_samples)))
    return buf.getvalue()


# ---------------------------------------------------------------------------
# vad_to_emotion — pure function
# ---------------------------------------------------------------------------

class TestVadToEmotion:
    def test_happy(self):
        assert vad_to_emotion(0.6, 0.5, 0.7) == "happy"

    def test_neutral_low_arousal_high_valence(self):
        assert vad_to_emotion(0.3, 0.5, 0.7) == "neutral"

    def test_angry(self):
        assert vad_to_emotion(0.6, 0.6, 0.3) == "angry"

    def test_fearful(self):
        assert vad_to_emotion(0.6, 0.3, 0.3) == "fearful"

    def test_sad(self):
        assert vad_to_emotion(0.3, 0.5, 0.2) == "sad"

    def test_surprised(self):
        assert vad_to_emotion(0.7, 0.5, 0.5) == "surprised"

    def test_neutral_middle_values(self):
        assert vad_to_emotion(0.3, 0.5, 0.5) == "neutral"

    def test_boundary_valence_exactly_06(self):
        # valence == 0.6 triggers the >= branch; arousal < 0.5 → neutral
        assert vad_to_emotion(0.4, 0.5, 0.6) == "neutral"


# ---------------------------------------------------------------------------
# convert_to_wav_16k
# ---------------------------------------------------------------------------

class TestConvertToWav16k:
    def test_raises_runtime_error_on_ffmpeg_failure(self, tmp_path):
        dummy = tmp_path / "test.webm"
        dummy.write_bytes(b"fake-audio")
        with patch("main.subprocess.run") as mock_run:
            mock_run.return_value = MagicMock(returncode=1, stderr="conversion failed")
            with pytest.raises(RuntimeError, match="ffmpeg falló"):
                convert_to_wav_16k(str(dummy))


# ---------------------------------------------------------------------------
# GET /health
# ---------------------------------------------------------------------------

class TestHealth:
    def test_returns_ok_and_true_when_models_loaded(self, monkeypatch):
        monkeypatch.setattr(main_module, "whisper_model", MagicMock())
        monkeypatch.setattr(main_module, "vad_model", MagicMock())
        client = TestClient(app)

        response = client.get("/health")

        assert response.status_code == 200
        data = response.json()
        assert data["status"] == "ok"
        assert data["whisper"] is True
        assert data["vad_model"] is True

    def test_returns_false_when_models_are_none(self, monkeypatch):
        monkeypatch.setattr(main_module, "whisper_model", None)
        monkeypatch.setattr(main_module, "vad_model", None)
        client = TestClient(app)

        response = client.get("/health")

        assert response.status_code == 200
        data = response.json()
        assert data["whisper"] is False
        assert data["vad_model"] is False


# ---------------------------------------------------------------------------
# POST /analyze
# ---------------------------------------------------------------------------

class TestAnalyze:
    def test_returns_503_when_models_unavailable(self, monkeypatch):
        monkeypatch.setattr(main_module, "whisper_model", None)
        monkeypatch.setattr(main_module, "vad_model", None)
        client = TestClient(app)

        response = client.post(
            "/analyze",
            files={"file": ("audio.wav", _make_wav_bytes(), "audio/wav")},
        )

        assert response.status_code == 503

    def test_returns_415_for_unsupported_mime_type(self, monkeypatch):
        monkeypatch.setattr(main_module, "whisper_model", MagicMock())
        monkeypatch.setattr(main_module, "vad_model", MagicMock())
        client = TestClient(app)

        response = client.post(
            "/analyze",
            files={"file": ("document.pdf", b"fake", "application/pdf")},
        )

        assert response.status_code == 415

    def test_returns_422_when_ffmpeg_fails(self, monkeypatch):
        monkeypatch.setattr(main_module, "whisper_model", MagicMock())
        monkeypatch.setattr(main_module, "vad_model", MagicMock())
        monkeypatch.setattr(
            main_module,
            "convert_to_wav_16k",
            MagicMock(side_effect=RuntimeError("ffmpeg falló (código 1): error")),
        )
        client = TestClient(app)

        response = client.post(
            "/analyze",
            files={"file": ("audio.wav", b"not-real-audio", "audio/wav")},
        )

        assert response.status_code == 422

    def test_processes_wav_audio_and_returns_full_response(self, monkeypatch, tmp_path):
        wav_path = str(tmp_path / "converted.wav")
        wav_bytes = _make_wav_bytes()
        with open(wav_path, "wb") as f:
            f.write(wav_bytes)

        mock_segment = MagicMock()
        mock_segment.text = "hola mundo"
        mock_info = MagicMock()
        mock_info.language = "es"

        mock_whisper = MagicMock()
        mock_whisper.transcribe.return_value = (iter([mock_segment]), mock_info)

        monkeypatch.setattr(main_module, "whisper_model", mock_whisper)
        monkeypatch.setattr(main_module, "vad_model", MagicMock())
        monkeypatch.setattr(main_module, "convert_to_wav_16k", MagicMock(return_value=wav_path))
        monkeypatch.setattr(
            main_module,
            "predict_vad",
            MagicMock(return_value={"arousal": 0.6, "dominance": 0.5, "valence": 0.7}),
        )

        client = TestClient(app)
        response = client.post(
            "/analyze",
            files={"file": ("recording.wav", wav_bytes, "audio/wav")},
        )

        assert response.status_code == 200
        data = response.json()
        assert data["transcripcion"] == "hola mundo"
        assert data["idioma_detectado"] == "es"
        assert data["emocion_dominante"] == "happy"  # arousal=0.6>=0.5, valence=0.7>=0.6 → happy
        assert data["vad"]["arousal"] == pytest.approx(0.6)
        assert data["vad"]["valence"] == pytest.approx(0.7)

    def test_cleans_up_temp_files_after_processing(self, monkeypatch, tmp_path):
        wav_path = str(tmp_path / "converted.wav")
        wav_bytes = _make_wav_bytes()
        with open(wav_path, "wb") as f:
            f.write(wav_bytes)

        mock_segment = MagicMock()
        mock_segment.text = "texto"
        mock_info = MagicMock()
        mock_info.language = "es"

        mock_whisper = MagicMock()
        mock_whisper.transcribe.return_value = (iter([mock_segment]), mock_info)

        monkeypatch.setattr(main_module, "whisper_model", mock_whisper)
        monkeypatch.setattr(main_module, "vad_model", MagicMock())
        monkeypatch.setattr(main_module, "convert_to_wav_16k", MagicMock(return_value=wav_path))
        monkeypatch.setattr(
            main_module,
            "predict_vad",
            MagicMock(return_value={"arousal": 0.3, "dominance": 0.5, "valence": 0.5}),
        )

        deleted_paths = []
        real_unlink = main_module.os.unlink

        def capture_unlink(path):
            deleted_paths.append(path)
            try:
                real_unlink(path)
            except OSError:
                pass

        monkeypatch.setattr(main_module.os, "unlink", capture_unlink)

        client = TestClient(app)
        client.post(
            "/analyze",
            files={"file": ("audio.wav", wav_bytes, "audio/wav")},
        )

        assert wav_path in deleted_paths

    def test_webm_content_type_is_accepted(self, monkeypatch, tmp_path):
        wav_path = str(tmp_path / "converted.wav")
        wav_bytes = _make_wav_bytes()
        with open(wav_path, "wb") as f:
            f.write(wav_bytes)

        mock_segment = MagicMock()
        mock_segment.text = ""
        mock_info = MagicMock()
        mock_info.language = "es"

        mock_whisper = MagicMock()
        mock_whisper.transcribe.return_value = (iter([mock_segment]), mock_info)

        monkeypatch.setattr(main_module, "whisper_model", mock_whisper)
        monkeypatch.setattr(main_module, "vad_model", MagicMock())
        monkeypatch.setattr(main_module, "convert_to_wav_16k", MagicMock(return_value=wav_path))
        monkeypatch.setattr(
            main_module,
            "predict_vad",
            MagicMock(return_value={"arousal": 0.3, "dominance": 0.5, "valence": 0.5}),
        )

        client = TestClient(app)
        response = client.post(
            "/analyze",
            files={"file": ("recording.webm", b"fake-webm", "audio/webm")},
        )

        assert response.status_code == 200
