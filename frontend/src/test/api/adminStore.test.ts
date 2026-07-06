import { clearAllMocks } from '../testHelpers'
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



describe('adminStoreApi', () => {
    beforeEach(() => { clearAllMocks() })

    it('create envía FormData con todos los campos', () => {
        return callCreate(baseData()).then(() => {
            verifyCreateCalledWithFullFormData()
        })
    })

    it('create omite price e imágenes cuando son null', () => {
        return callCreate(baseData({ price: null, imageLight: null, imageDark: null })).then(() => {
            verifyCreateCalledWithOmitedFields()
        })
    })

    it('update llama put con el id en la ruta', () => {
        return callUpdate(5, baseData()).then(() => {
            verifyPutCalledWithId(5)
        })
    })

    it('remove llama delete con el id', () => {
        return callRemove(9).then(() => {
            verifyDeleteCalledWith('/admin/store/items/9')
        })
    })
    const callCreate = (data: StoreItemFormData) => {
        return Promise.resolve(adminStoreApi.create(data))
    }

    const callUpdate = (id: number, data: StoreItemFormData) => {
        return Promise.resolve(adminStoreApi.update(id, data))
    }

    const callRemove = (id: number) => {
        return Promise.resolve(adminStoreApi.remove(id))
    }

    const verifyCreateCalledWithFullFormData = () => {
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
    }

    const verifyCreateCalledWithOmitedFields = () => {
        const fd = postMultipart.mock.calls[0][1] as FormData
        expect(fd.get('price')).toBeNull()
        expect(fd.get('imageLight')).toBeNull()
        expect(fd.get('imageDark')).toBeNull()
    }

    const verifyPutCalledWithId = (expectedId: number) => {
        expect(put).toHaveBeenCalledOnce()
        expect(put.mock.calls[0][0]).toBe(`/admin/store/items/${expectedId}`)
    }

    const verifyDeleteCalledWith = (expectedUrl: string) => {
        expect(del).toHaveBeenCalledWith(expectedUrl)
    }
})

function baseData(overrides: Partial<StoreItemFormData> = {}): StoreItemFormData {
  return ({
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
}
