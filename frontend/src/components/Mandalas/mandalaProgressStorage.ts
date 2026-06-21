const DB_NAME = 'huly-mandala-progress'
const DB_VERSION = 1
const STORE_NAME = 'paintLayers'

interface MandalaProgressRecord {
  mandalaId: string
  blob: Blob
  updatedAt: number
}

function openMandalaProgressDb() {
  if (!('indexedDB' in globalThis)) {
    return Promise.resolve<IDBDatabase | null>(null)
  }

  return new Promise<IDBDatabase>((resolve, reject) => {
    const request = indexedDB.open(DB_NAME, DB_VERSION)

    request.onupgradeneeded = () => {
      const db = request.result
      if (!db.objectStoreNames.contains(STORE_NAME)) {
        db.createObjectStore(STORE_NAME, { keyPath: 'mandalaId' })
      }
    }

    request.onerror = () => reject(request.error)
    request.onsuccess = () => resolve(request.result)
  })
}

function runStoreTransaction<T>(
  mode: IDBTransactionMode,
  operation: (store: IDBObjectStore) => IDBRequest<T>,
) {
  return openMandalaProgressDb().then(db => (
    new Promise<T | null>((resolve, reject) => {
      if (!db) {
        resolve(null)
        return
      }

      const transaction = db.transaction(STORE_NAME, mode)
      const store = transaction.objectStore(STORE_NAME)
      const request = operation(store)
      let result: T | null = null

      request.onerror = () => {
        db.close()
        reject(request.error)
      }
      request.onsuccess = () => {
        result = request.result ?? null
      }
      transaction.onerror = () => {
        db.close()
        reject(transaction.error)
      }
      transaction.oncomplete = () => {
        db.close()
        resolve(result)
      }
    })
  ))
}

export function saveMandalaProgress(mandalaId: string, blob: Blob) {
  const record: MandalaProgressRecord = {
    mandalaId,
    blob,
    updatedAt: Date.now(),
  }

  return runStoreTransaction('readwrite', store => store.put(record)).then(() => undefined)
}

export function loadMandalaProgress(mandalaId: string) {
  return runStoreTransaction<MandalaProgressRecord>('readonly', store => store.get(mandalaId))
    .then(record => record?.blob ?? null)
}

export function clearMandalaProgress(mandalaId: string) {
  return runStoreTransaction('readwrite', store => store.delete(mandalaId)).then(() => undefined)
}
