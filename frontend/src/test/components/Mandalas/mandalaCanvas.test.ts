import { describe, expect, it } from 'vitest'
import {
  createRegionMask,
  dilateMap,
  floodFillExterior,
  type MandalaAnalysis,
} from '../../../components/Mandalas/mandalaCanvas'

const indexFor = (x: number, y: number, width: number) => y * width + x

describe('mandalaCanvas helpers', () => {
  it('floodFillExterior marca el exterior y respeta bordes cerrados', () => {
    const width = 7
    const height = 7
    const boundaryMap = new Uint8Array(width * height)

    for (let x = 2; x <= 4; x += 1) {
      boundaryMap[indexFor(x, 2, width)] = 1
      boundaryMap[indexFor(x, 4, width)] = 1
    }
    for (let y = 2; y <= 4; y += 1) {
      boundaryMap[indexFor(2, y, width)] = 1
      boundaryMap[indexFor(4, y, width)] = 1
    }

    const exteriorMap = floodFillExterior(boundaryMap, width, height)

    expect(exteriorMap[indexFor(0, 0, width)]).toBe(1)
    expect(exteriorMap[indexFor(3, 3, width)]).toBe(0)
  })

  it('createRegionMask devuelve solo la region interna donde empieza el trazo', () => {
    const width = 7
    const height = 7
    const boundaryMap = new Uint8Array(width * height)

    for (let x = 2; x <= 4; x += 1) {
      boundaryMap[indexFor(x, 2, width)] = 1
      boundaryMap[indexFor(x, 4, width)] = 1
    }
    for (let y = 2; y <= 4; y += 1) {
      boundaryMap[indexFor(2, y, width)] = 1
      boundaryMap[indexFor(4, y, width)] = 1
    }

    const analysis: MandalaAnalysis = {
      boundaryMap,
      exteriorMap: floodFillExterior(boundaryMap, width, height),
      height,
      width,
    }

    const mask = createRegionMask({ x: 3, y: 3 }, analysis)

    expect(mask).not.toBeNull()
    expect(mask?.[indexFor(3, 3, width)]).toBe(1)
    expect(mask?.[indexFor(1, 1, width)]).toBe(0)
    expect(mask?.[indexFor(2, 3, width)]).toBe(0)
  })

  it('createRegionMask rechaza puntos del exterior', () => {
    const width = 5
    const height = 5
    const boundaryMap = new Uint8Array(width * height)
    const analysis: MandalaAnalysis = {
      boundaryMap,
      exteriorMap: floodFillExterior(boundaryMap, width, height),
      height,
      width,
    }

    expect(createRegionMask({ x: 0, y: 0 }, analysis)).toBeNull()
  })

  it('dilateMap expande un borde para cerrar huecos pequenos', () => {
    const width = 5
    const height = 5
    const map = new Uint8Array(width * height)
    map[indexFor(2, 2, width)] = 1

    const dilated = dilateMap(map, width, height, 1)

    expect(dilated[indexFor(1, 1, width)]).toBe(1)
    expect(dilated[indexFor(2, 2, width)]).toBe(1)
    expect(dilated[indexFor(3, 3, width)]).toBe(1)
    expect(dilated[indexFor(0, 0, width)]).toBe(0)
  })
})
