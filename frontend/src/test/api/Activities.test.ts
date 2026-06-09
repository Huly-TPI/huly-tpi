import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ActivityType, registerActivitySession } from '../../api/activities'
import * as clientModule from '../../api/client'

vi.mock('../../api/client', async (importOriginal) => {
  const actual = await importOriginal<typeof import('../../api/client')>()
  return {
    ...actual,
    getToken: vi.fn(),
    tryRehydrateSession: vi.fn(),
    clearToken: vi.fn(),
  }
})

const mockedGetToken = vi.mocked(clientModule.getToken)
const mockedTryRehydrateSession = vi.mocked(clientModule.tryRehydrateSession)

describe('registerActivitySession', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.stubGlobal('fetch', vi.fn())
    mockedGetToken.mockReturnValue('token-de-prueba')
    mockedTryRehydrateSession.mockResolvedValue(null)
  })

  it('llama a fetch con la ruta y los datos correctos', async () => {
    vi.mocked(fetch).mockResolvedValueOnce(
      new Response(null, { status: 200 }),
    )

    await registerActivitySession({ activityType: ActivityType.RESPIRACION })

    expect(fetch).toHaveBeenCalledWith(
      expect.stringMatching(/\/api\/activities\/sessions$/),
      expect.objectContaining({
        method: 'POST',
        credentials: 'include',
        body: JSON.stringify({ activityType: ActivityType.RESPIRACION }),
      }),
    )
  })

  it('pasa keepalive cuando se indica', async () => {
    vi.mocked(fetch).mockResolvedValueOnce(
      new Response(null, { status: 200 }),
    )

    await registerActivitySession(
      { activityType: ActivityType.RESPIRACION },
      { keepalive: true },
    )

    expect(fetch).toHaveBeenCalledWith(
      expect.any(String),
      expect.objectContaining({
        keepalive: true,
      }),
    )
  })

  it('propaga errores del backend', async () => {
    vi.mocked(fetch).mockResolvedValueOnce(
      new Response(JSON.stringify({ message: 'No autorizado' }), {
        status: 401,
        headers: { 'Content-Type': 'application/json' },
      }),
    )

    await expect(
      registerActivitySession({
        activityType: ActivityType.BURBUJA,
      }),
    ).rejects.toThrow('Session expired')
  })
})
