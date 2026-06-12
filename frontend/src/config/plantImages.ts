// Registry of plant stage images per plant type.
//
// To add images for a plant type:
//   1. Place plant-0.png through plant-5.png in:
//      src/assets/challenges/plant-stages/type-N/
//   2. Add the 6 imports below under the corresponding type section.
//   3. Replace the `defaultStages` reference for that type with the new array.

import plant0 from '../assets/challenges/plant-stages/plant-0.png'
import plant1 from '../assets/challenges/plant-stages/plant-1.png'
import plant2 from '../assets/challenges/plant-stages/plant-2.png'
import plant3 from '../assets/challenges/plant-stages/plant-3.png'
import plant4 from '../assets/challenges/plant-stages/plant-4.png'
import plant5 from '../assets/challenges/plant-stages/plant-5.png'

// ─── Type 1 ────────────────────────────────────────────────────────────────
// import type1_0 from '../assets/challenges/plant-stages/type-1/plant-0.png'
// import type1_1 from '../assets/challenges/plant-stages/type-1/plant-1.png'
// import type1_2 from '../assets/challenges/plant-stages/type-1/plant-2.png'
// import type1_3 from '../assets/challenges/plant-stages/type-1/plant-3.png'
// import type1_4 from '../assets/challenges/plant-stages/type-1/plant-4.png'
// import type1_5 from '../assets/challenges/plant-stages/type-1/plant-5.png'

// ─── Type 2 ────────────────────────────────────────────────────────────────
// import type2_0 from '../assets/challenges/plant-stages/type-2/plant-0.png'
// ... (same pattern)

// ─── Type 3 ────────────────────────────────────────────────────────────────
// ─── Type 4 ────────────────────────────────────────────────────────────────
// ─── Type 5 ────────────────────────────────────────────────────────────────
// ─── Type 6 ────────────────────────────────────────────────────────────────
// ─── Type 7 ────────────────────────────────────────────────────────────────
// ─── Type 8 ────────────────────────────────────────────────────────────────
// ─── Type 9 ────────────────────────────────────────────────────────────────
// ─── Type 10 ───────────────────────────────────────────────────────────────

type PlantStageImages = readonly [string, string, string, string, string, string]

const defaultStages: PlantStageImages = [plant0, plant1, plant2, plant3, plant4, plant5]

// Map: plant type (1–10) → 6 stage images [stage0, stage1, ..., stage5]
// Replace defaultStages with the type-specific array once assets are ready.
const PLANT_TYPE_IMAGES: Record<number, PlantStageImages> = {
  1: defaultStages,
  2: defaultStages,
  3: defaultStages,
  4: defaultStages,
  5: defaultStages,
  6: defaultStages,
  7: defaultStages,
  8: defaultStages,
  9: defaultStages,
  10: defaultStages,
}

/**
 * Returns the 6 stage images for the given plant number.
 * Plant numbers beyond 10 use type 10 (the last available type).
 */
export function getPlantImages(plantNumber: number): PlantStageImages {
  const typeIndex = Math.min(Math.max(plantNumber, 1), 10)
  return PLANT_TYPE_IMAGES[typeIndex] ?? defaultStages
}
