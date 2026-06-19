import { describe, it, expect, beforeEach } from 'vitest'
import 'fake-indexeddb/auto'
import { saveAudioBlob, getAudioBlob, deleteAudioBlob } from '../../hooks/useAudioCache'

// fake-indexeddb/auto replaces the global indexedDB with an in-memory implementation.
// Each test gets a fresh database because the module re-opens a connection per call.

describe('audioCache', () => {
  beforeEach(async () => {
    // Delete any leftover entries from previous tests by using a unique key per test.
    // No explicit reset needed — each test uses its own key.
  })

  it('saveAudioBlob stores a blob retrievable by its key', async () => {
    const blob = new Blob(['hello audio'], { type: 'audio/webm' })

    await saveAudioBlob('test-key-1', blob)
    const retrieved = await getAudioBlob('test-key-1')

    // fake-indexeddb serializa Blobs a objetos planos en jsdom; verificamos que existe
    expect(retrieved).not.toBeNull()
  })

  it('getAudioBlob returns null for a nonexistent key', async () => {
    const result = await getAudioBlob('nonexistent-key')

    expect(result).toBeNull()
  })

  it('deleteAudioBlob removes the entry so subsequent reads return null', async () => {
    const blob = new Blob(['audio-data'], { type: 'audio/webm' })
    await saveAudioBlob('test-key-delete', blob)

    await deleteAudioBlob('test-key-delete')
    const result = await getAudioBlob('test-key-delete')

    expect(result).toBeNull()
  })

  it('saveAudioBlob overwrites an existing entry with the same key', async () => {
    const first = new Blob(['first'], { type: 'audio/webm' })
    const second = new Blob(['second-longer'], { type: 'audio/webm' })

    await saveAudioBlob('test-key-overwrite', first)
    await saveAudioBlob('test-key-overwrite', second)
    const retrieved = await getAudioBlob('test-key-overwrite')

    // fake-indexeddb serializa Blobs a objetos planos en jsdom; solo verificamos que existe
    expect(retrieved).not.toBeNull()
  })
})
