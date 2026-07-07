import { describe, expect, it } from 'vitest'
import {
  createRegionMask,
  dilateMap,
  floodFillExterior,
  type MandalaAnalysis,
} from '../../../components/Mandalas/mandalaCanvasUtils'



describe('mandalaCanvas helpers', () => {
  it('floodFillExterior marca el exterior y respeta bordes cerrados', () => {
    setupBoundaryBox(7, 7, 2, 4)
    runFloodFillExterior()
    verifyExteriorMapValue(0, 0, 1)
    verifyExteriorMapValue(3, 3, 0)
  })

  it('createRegionMask devuelve solo la región interna donde empieza el trazo', () => {
    setupAnalysisWithBoundaryBox(7, 7, 2, 4)
    runCreateRegionMask(3, 3)
    verifyMaskIsNotNull()
    verifyMaskValue(3, 3, 1)
    verifyMaskValue(1, 1, 0)
    verifyMaskValue(2, 3, 0)
  })

  it('createRegionMask rechaza puntos del exterior', () => {
    setupAnalysisWithoutBoundaries(5, 5)
    runCreateRegionMask(0, 0)
    verifyMaskIsNull()
  })

  it('dilateMap expande un borde para cerrar huecos pequeños', () => {
    setupMapWithSinglePixel(5, 5, 2, 2)
    runDilateMap(1)
    verifyDilatedMapValue(1, 1, 1)
    verifyDilatedMapValue(2, 2, 1)
    verifyDilatedMapValue(3, 3, 1)
    verifyDilatedMapValue(0, 0, 0)
  })
  let currentWidth: number
  let currentHeight: number
  let currentBoundaryMap: Uint8Array
  let currentExteriorMap: Uint8Array
  let currentAnalysis: MandalaAnalysis
  let currentMask: Uint8Array | null
  let currentMap: Uint8Array
  let currentDilatedMap: Uint8Array

  /* helpers */

  const setupBoundaryBox = (w: number, h: number, min: number, max: number) => {
    currentWidth = w
    currentHeight = h
    currentBoundaryMap = new Uint8Array(w * h)
    for (let x = min; x <= max; x += 1) {
      currentBoundaryMap[indexFor(x, min, w)] = 1
      currentBoundaryMap[indexFor(x, max, w)] = 1
    }
    for (let y = min; y <= max; y += 1) {
      currentBoundaryMap[indexFor(min, y, w)] = 1
      currentBoundaryMap[indexFor(max, y, w)] = 1
    }
  }

  const runFloodFillExterior = () => {
    currentExteriorMap = floodFillExterior(currentBoundaryMap, currentWidth, currentHeight)
  }

  const verifyExteriorMapValue = (x: number, y: number, expected: number) => {
    expect(currentExteriorMap[indexFor(x, y, currentWidth)]).toBe(expected)
  }

  const setupAnalysisWithBoundaryBox = (w: number, h: number, min: number, max: number) => {
    setupBoundaryBox(w, h, min, max)
    runFloodFillExterior()
    currentAnalysis = {
      boundaryMap: currentBoundaryMap,
      exteriorMap: currentExteriorMap,
      height: h,
      width: w,
    }
  }

  const runCreateRegionMask = (x: number, y: number) => {
    currentMask = createRegionMask({ x, y }, currentAnalysis)
  }

  const verifyMaskIsNotNull = () => {
    expect(currentMask).not.toBeNull()
  }

  const verifyMaskIsNull = () => {
    expect(currentMask).toBeNull()
  }

  const verifyMaskValue = (x: number, y: number, expected: number) => {
    expect(currentMask?.[indexFor(x, y, currentWidth)]).toBe(expected)
  }

  const setupAnalysisWithoutBoundaries = (w: number, h: number) => {
    currentWidth = w
    currentHeight = h
    currentBoundaryMap = new Uint8Array(w * h)
    currentExteriorMap = floodFillExterior(currentBoundaryMap, w, h)
    currentAnalysis = {
      boundaryMap: currentBoundaryMap,
      exteriorMap: currentExteriorMap,
      height: h,
      width: w,
    }
  }

  const setupMapWithSinglePixel = (w: number, h: number, px: number, py: number) => {
    currentWidth = w
    currentHeight = h
    currentMap = new Uint8Array(w * h)
    currentMap[indexFor(px, py, w)] = 1
  }

  const runDilateMap = (radius: number) => {
    currentDilatedMap = dilateMap(currentMap, currentWidth, currentHeight, radius)
  }

  const verifyDilatedMapValue = (x: number, y: number, expected: number) => {
    expect(currentDilatedMap[indexFor(x, y, currentWidth)]).toBe(expected)
  }
})

function indexFor(x: number, y: number, width: number) {
  return y * width + x
}
