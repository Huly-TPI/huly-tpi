import { ArrowLeft, ImageDown, RotateCcw } from "lucide-react";
import { useRef, useState } from "react";
import mandalaBackgroundDark from "../../assets/mandalas/dark-theme/background/mandala-background-dark.webp";
import mandalaBackgroundLight from "../../assets/mandalas/light-theme/background/mandala-background-light.webp";
import mandalaEasel from "../../assets/mandalas/mi_atril.webp";
import { useMandalaProgress } from "../../hooks/useMandalaProgress";
import { downloadImageBlob } from "../../utils/downloadImage";
import ThemeBackground from "../ThemeBackground/ThemeBackground";
import MandalaCanvas, { type MandalaCanvasHandle } from "./MandalaCanvas";
import MandalaToolbar from "./MandalaToolbar";
import type { MandalaCatalogItem } from "./mandalaTypes";
import "./MandalaColoringActivity.css";

const COLORS = [
  "#EF4444", // Rojo
  "#1A202C", // Negro
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
  const {
    clearProgress,
    loading: progressLoading,
    paintBlob,
    saveProgress,
  } = useMandalaProgress(mandala.id);

  const handleClear = () => {
    canvasRef.current?.clear();
    void clearProgress();
  };

  const handleExport = async () => {
    const blob = await canvasRef.current?.exportImage();
    if (!blob) return;

    await downloadImageBlob(blob, `${mandala.id}.png`);
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
            lightAlt="Fondo de día para pintar mandalas"
            darkAlt="Fondo nocturno para pintar mandalas"
          />

          <button
            aria-label="Elegir otro mandala"
            className="mandala-activity__back mandala-activity__back--scene"
            onClick={onBackToGallery}
            type="button"
          >
            <ArrowLeft aria-hidden="true" size={18} />
            Ir a galería
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
                  onPaintChange={saveProgress}
                />
              )}
            </div>

            <button
              aria-label="Limpiar mandala"
              className="mandala-canvas-clear-btn"
              onClick={handleClear}
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
