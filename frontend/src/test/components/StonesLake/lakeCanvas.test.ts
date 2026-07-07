import { describe, expect, it, vi } from 'vitest'
import {
  coverPlacement,
  createRipple,
  drawRipples,
  getLakePoint,
  isWaterColor,
  LAKE_PALETTES,
  rippleProgress,
  toImagePoint,
  type Ripple,
} from '../../../components/StonesLake/lakeCanvas'

describe('lakeCanvas', () => {
  it('traduce el punto del puntero a coordenadas relativas al canvas', () => {
    setupCanvasRect(20, 10)
    runGetLakePoint(120, 80)
    verifyPoint(100, 70)
  })

  it('crea una onda con un radio máximo proporcional al tamaño de la vista', () => {
    runCreateRipple(50, 60, 1000, 1000)
    verifyRippleMatches({ x: 50, y: 60, createdAt: 1000, life: 4200 })
    verifyRippleMaxRadius(220)
  })

  it('garantiza un radio máximo mínimo para lagos pequeños', () => {
    runCreateRipple(0, 0, 0, 100)
    verifyRippleMaxRadius(120)
  })

  it('calcula el progreso de la onda respecto a su tiempo de vida', () => {
    runCreateRipple(0, 0, 1000, 1000)
    verifyProgressAt(3100, 0.5)
  })

  it('descarta las ondas ya desvanecidas y conserva las vivas', () => {
    setupMockContext()
    setupAliveAndDeadRipples()
    runDrawRipples(1000)
    verifyOnlyAliveRippleRemains()
    verifyContextStrokeCalled()
  })

  it('reconoce el azul del agua y descarta el pasto y las rocas', () => {
    verifyIsWaterColor(90, 175, 225, true)
    verifyIsWaterColor(150, 200, 40, false)
    verifyIsWaterColor(60, 140, 70, false)
    verifyIsWaterColor(200, 190, 170, false)
  })

  it('mapea el punto de la vista a píxeles de la imagen con cover cuadrado centrado', () => {
    verifyImagePointMapping(100, 50, 200, 100, 100, 100, 50, 50)
  })

  it('ubica la imagen con cover escalada y centrada dentro de la vista', () => {
    verifyCoverPlacement(200, 100, 100, 100, { x: 0, y: -50, width: 200, height: 200 })
  })

  let currentRect: { left: number; top: number }
  let currentPoint: { x: number; y: number }
  let currentRipple: Ripple
  let currentContext: CanvasRenderingContext2D & { stroke: ReturnType<typeof vi.fn> }
  let currentAliveRipples: Ripple[]

  const setupCanvasRect = (left: number, top: number) => {
    currentRect = { left, top }
  }

  const runGetLakePoint = (clientX: number, clientY: number) => {
    const canvas = {
      getBoundingClientRect: () => currentRect,
    } as unknown as HTMLCanvasElement
    currentPoint = getLakePoint({ clientX, clientY }, canvas)
  }

  const verifyPoint = (x: number, y: number) => {
    expect(currentPoint).toEqual({ x, y })
  }

  const runCreateRipple = (x: number, y: number, now: number, viewSize: number) => {
    currentRipple = createRipple(x, y, now, viewSize)
  }

  const verifyRippleMatches = (expected: Partial<Ripple>) => {
    expect(currentRipple).toMatchObject(expected)
  }

  const verifyRippleMaxRadius = (expected: number) => {
    expect(currentRipple.maxRadius).toBe(expected)
  }

  const verifyProgressAt = (now: number, expected: number) => {
    expect(rippleProgress(currentRipple, now)).toBeCloseTo(expected)
  }

  const setupMockContext = () => {
    currentContext = createMockContext() as unknown as CanvasRenderingContext2D & {
      stroke: ReturnType<typeof vi.fn>
    }
  }

  const setupAliveAndDeadRipples = () => {
    const vivo = createRipple(10, 10, 0, 1000)
    const muerto = createRipple(10, 10, -5000, 1000)
    currentAliveRipples = [vivo, muerto]
  }

  const runDrawRipples = (now: number) => {
    currentAliveRipples = drawRipples(currentContext, currentAliveRipples, LAKE_PALETTES.light, now)
  }

  const verifyOnlyAliveRippleRemains = () => {
    expect(currentAliveRipples).toHaveLength(1)
  }

  const verifyContextStrokeCalled = () => {
    expect(currentContext.stroke).toHaveBeenCalled()
  }

  const verifyIsWaterColor = (r: number, g: number, b: number, expected: boolean) => {
    expect(isWaterColor(r, g, b)).toBe(expected)
  }

  const verifyImagePointMapping = (
    viewX: number,
    viewY: number,
    viewWidth: number,
    viewHeight: number,
    naturalWidth: number,
    naturalHeight: number,
    expectedX: number,
    expectedY: number,
  ) => {
    const point = toImagePoint(viewX, viewY, viewWidth, viewHeight, naturalWidth, naturalHeight)
    expect(point.x).toBeCloseTo(expectedX)
    expect(point.y).toBeCloseTo(expectedY)
  }

  const verifyCoverPlacement = (
    viewWidth: number,
    viewHeight: number,
    naturalWidth: number,
    naturalHeight: number,
    expected: { x: number; y: number; width: number; height: number },
  ) => {
    const placement = coverPlacement(viewWidth, viewHeight, naturalWidth, naturalHeight)
    expect(placement.x).toBeCloseTo(expected.x)
    expect(placement.y).toBeCloseTo(expected.y)
    expect(placement.width).toBeCloseTo(expected.width)
    expect(placement.height).toBeCloseTo(expected.height)
  }
})

function createMockContext() {
  return {
    beginPath: vi.fn(),
    moveTo: vi.fn(),
    lineTo: vi.fn(),
    arc: vi.fn(),
    stroke: vi.fn(),
    fill: vi.fn(),
    lineWidth: 0,
    strokeStyle: '',
    fillStyle: '',
  }
}
