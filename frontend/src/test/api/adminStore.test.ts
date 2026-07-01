import { describe, it, expect, vi, beforeEach } from 'vitest'
import { adminStoreApi, type StoreItemFormData } from '../../api/adminStore'

const postMultipart = vi.fn()
const put = vi.fn()
const del = vi.fn()

vi.mock('../../api/client', () => ({
    api: {
        postMultipart: (...a: unknown[]) => postMultipart(...a),
        put: (...a: unknown[]) => put(...a),
        delete: (...a: unknown[]) => del(...a),
    },
}))

const baseData = (overrides: Partial<StoreItemFormData> = {}): StoreItemFormData => ({
    name: 'Casa nueva',
    description: 'desc',
    category: 'HOUSE',
    priceCoins: 80,
    price: 500,
    premiumOnly: false,
    imageLight: new File(['l'], 'l.webp', { type: 'image/webp' }),
    imageDark: new File(['d'], 'd.webp', { type: 'image/webp' }),
    ...overrides,
})

describe('adminStoreApi', () => {
    beforeEach(() => { vi.clearAllMocks() })

    it('create envía FormData con todos los campos', () => {
        adminStoreApi.create(baseData())
        expect(postMultipart).toHaveBeenCalledOnce()
        const [path, fd] = postMultipart.mock.calls[0] as [string, FormData]
        expect(path).toBe('/admin/store/items')
        expect(fd.get('name')).toBe('Casa nueva')
        expect(fd.get('category')).toBe('HOUSE')
        expect(fd.get('priceCoins')).toBe('80')
        expect(fd.get('price')).toBe('500')
        expect(fd.get('premiumOnly')).toBe('false')
        expect(fd.get('imageLight')).toBeInstanceOf(File)
        expect(fd.get('imageDark')).toBeInstanceOf(File)
    })

    it('create omite price e imágenes cuando son null', () => {
        adminStoreApi.create(baseData({ price: null, imageLight: null, imageDark: null }))
        const fd = postMultipart.mock.calls[0][1] as FormData
        expect(fd.get('price')).toBeNull()
        expect(fd.get('imageLight')).toBeNull()
        expect(fd.get('imageDark')).toBeNull()
    })

    it('update llama put con el id en la ruta', () => {
        adminStoreApi.update(5, baseData())
        expect(put).toHaveBeenCalledOnce()
        expect(put.mock.calls[0][0]).toBe('/admin/store/items/5')
    })

    it('remove llama delete con el id', () => {
        adminStoreApi.remove(9)
        expect(del).toHaveBeenCalledWith('/admin/store/items/9')
    })
})