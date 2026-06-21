import { ArrowLeft } from "lucide-react";
import { useRef, useState } from "react";
import mandalasScene from "../../assets/mandalas/mandalas-scene.svg";
import { useMandalaProgress } from "../../hooks/useMandalaProgress";
import MandalaCanvas, { type MandalaCanvasHandle } from "./MandalaCanvas";
import MandalaToolbar from "./MandalaToolbar";
import type { MandalaCatalogItem } from "./mandalaTypes";
import "./MandalaColoringActivity.css";

const COLORS = [
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
  const [brushSize, setBrushSize] = useState(22);
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

  return (
    <section
      className="mandala-activity"
      aria-label={`Pintar ${mandala.title}`}
    >
      <div className="mandala-activity__scene-scroll">
        <div className="mandala-painting-scene">
          <img
            alt=""
            aria-hidden="true"
            className="mandala-scene-image"
            draggable={false}
            src={mandalasScene}
          />

          <button
            aria-label="Elegir otro mandala"
            className="mandala-activity__back mandala-activity__back--scene"
            onClick={onBackToGallery}
            type="button"
          >
            <ArrowLeft aria-hidden="true" size={18} />
            Galería
          </button>

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

          <div className="mandala-palette-overlay">
            <MandalaToolbar
              brushSize={brushSize}
              colors={COLORS}
              onBrushSizeChange={setBrushSize}
              onClear={handleClear}
              onColorChange={setSelectedColor}
              selectedColor={selectedColor}
            />
          </div>
        </div>
      </div>
    </section>
  );
}
