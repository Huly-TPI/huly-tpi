import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { indexedDB } from 'fake-indexeddb'
import {
  clearMandalaProgress,
  loadMandalaProgress,
  saveMandalaProgress,
} from '../../../components/Mandalas/mandalaProgressStorage'

describe('mandalaProgressStorage', () => {
  beforeEach(() => {
    vi.stubGlobal('indexedDB', indexedDB)
  })

  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('guarda, carga y borra progreso por mandala', async () => {
    const blob = new Blob(['paint'], { type: 'image/png' })

    await saveMandalaProgress('mandala-01', blob)
    const loadedBlob = await loadMandalaProgress('mandala-01')

    expect(loadedBlob).not.toBeNull()

    await clearMandalaProgress('mandala-01')

    expect(await loadMandalaProgress('mandala-01')).toBeNull()
  })
})
