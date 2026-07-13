import { ImageDown, RotateCcw } from "lucide-react";
import { useCallback, useEffect, useRef, useState } from "react";
import { ActivityType } from "../../api/activities";
import mandalaBackgroundDark from "../../assets/mandalas/dark-theme/background/mandala-background-dark.webp";
import mandalaBackgroundLight from "../../assets/mandalas/light-theme/background/mandala-background-light.webp";
import galleryImage from "../../assets/mandalas/galery.webp";
import mandalaEasel from "../../assets/mandalas/mi_atril.webp";
import { useActivitySessionTracker } from "../../hooks/useActivitySessionTracker";
import { useMandalaProgress } from "../../hooks/useMandalaProgress";
import { downloadImageBlob } from "../../hooks/downloadImage";
import ThemeBackground from "../ThemeBackground/ThemeBackground";
import MandalaCanvas, { type MandalaCanvasHandle } from "./MandalaCanvas";
import MandalaToolbar from "./MandalaToolbar";
import type { MandalaCatalogItem } from "./mandalaTypes";
import "./MandalaColoringActivity.css";

const COLORS = [
  "#EF4444",
  "#1A202C",
  "#8869AC",
  "#7BCDBA",
  "#80B8F0",
  "#F4A7B9",
  "#F6C85F",
  "#F29E4C",
  "#6CBF84",
  "#3F6FB5",
];

interface MandalaColoringActivityProps {
  mandala: MandalaCatalogItem;
  onBackToGallery: () => void;
}

export default function MandalaColoringActivity({
  mandala,
  onBackToGallery,
}: MandalaColoringActivityProps) {
  const canvasRef = useRef<MandalaCanvasHandle>(null);
  const [brushSize, setBrushSize] = useState(40);
  const [selectedColor, setSelectedColor] = useState(COLORS[0]);
  const [canTrackSession, setCanTrackSession] = useState(false);
  const {
    clearProgress,
    loading: progressLoading,
    paintBlob,
    saveProgress,
    sessionRegistered,
  } = useMandalaProgress(mandala.id);
  const { markConditionMet, saveSession, startSession, stopSession } = useActivitySessionTracker(
    ActivityType.MANDALA,
    {
      autoStart: false,
      contextId: mandala.id,
      minDurationSeconds: 10,
    },
  );

  useEffect(() => {
    if (progressLoading) {
      return;
    }

    if (sessionRegistered) {
      setCanTrackSession(false);
      stopSession();
      return;
    }

    setCanTrackSession(true);
    startSession();
  }, [progressLoading, sessionRegistered, startSession, stopSession]);

  const handlePaintChange = useCallback(
    async (blob: Blob) => {
      await saveProgress(blob);
      if (!canTrackSession) {
        return;
      }
      markConditionMet();
    },
    [canTrackSession, markConditionMet, saveProgress],
  );

  const handleClear = async () => {
    canvasRef.current?.clear();
    await clearProgress();
    stopSession();
    setCanTrackSession(true);
    startSession();
  };

  const handleExport = async () => {
    const blob = await canvasRef.current?.exportImage();
    if (!blob) return;

    await downloadImageBlob(blob, `${mandala.id}.png`);
  };

  const handleBackToGallery = () => {
    void saveSession();
    onBackToGallery();
  };

  return (
    <section
      className="mandala-activity"
      aria-label={`Pintar ${mandala.title}`}
    >
      <div className="mandala-activity__scene-scroll">
        <div className="mandala-painting-scene">
          <ThemeBackground
            lightSrc={mandalaBackgroundLight}
            darkSrc={mandalaBackgroundDark}
            lightAlt="Fondo de dia para pintar mandalas"
            darkAlt="Fondo nocturno para pintar mandalas"
          />

          <button
            aria-label="Ir a galeria"
            className="mandala-gallery-back-btn z-50 transition hover:scale-105 active:scale-95"
            onClick={handleBackToGallery}
            type="button"
          >
            <img src={galleryImage} alt="Ir a galeria" className="h-full w-full object-contain" />
          </button>

          <div className="mandala-easel-stage">
            <img
              alt=""
              aria-hidden="true"
              className="mandala-easel-image"
              draggable={false}
              src={mandalaEasel}
            />

            <div className="mandala-easel-drawing-area">
              {progressLoading ? (
                <div className="mandala-canvas">
                  <div className="mandala-canvas__status" role="status">
                    Cargando progreso...
                  </div>
                </div>
              ) : (
                <MandalaCanvas
                  key={mandala.id}
                  ref={canvasRef}
                  brushSize={brushSize}
                  color={selectedColor}
                  initialPaintBlob={paintBlob}
                  mandalaSrc={mandala.src}
                  onPaintChange={handlePaintChange}
                />
              )}
            </div>

            <button
              aria-label="Limpiar mandala"
              className="mandala-canvas-clear-btn"
              onClick={() => void handleClear()}
              type="button"
            >
              <RotateCcw aria-hidden="true" size={28} />
            </button>

            <button
              aria-label="Descargar mandala pintada"
              className="mandala-canvas-download-btn"
              onClick={handleExport}
              type="button"
            >
              <ImageDown aria-hidden="true" size={28} />
            </button>
          </div>

          <div className="mandala-mobile-actions">
            <button
              aria-label="Limpiar dibujo del mandala"
              className="mandala-mobile-actions__btn"
              onClick={() => void handleClear()}
              type="button"
            >
              <RotateCcw aria-hidden="true" size={18} />
              <span>Limpiar</span>
            </button>

            <button
              aria-label="Guardar mandala pintada"
              className="mandala-mobile-actions__btn"
              onClick={handleExport}
              type="button"
            >
              <ImageDown aria-hidden="true" size={18} />
              <span>Guardar</span>
            </button>
          </div>

          <div className="mandala-palette-overlay">
            <MandalaToolbar
              brushSize={brushSize}
              colors={COLORS}
              onBrushSizeChange={setBrushSize}
              onColorChange={setSelectedColor}
              selectedColor={selectedColor}
            />
          </div>
        </div>
      </div>
    </section>
  );
}
