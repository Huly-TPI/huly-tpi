import { beforeEach, describe, expect, it, vi } from 'vitest'
import { completeProfileTutorial, completeTutorial } from '../../api/onboarding'
import { api } from '../../api/client'

vi.mock('../../api/client', () => ({
  api: {
    get: vi.fn(),
    post: vi.fn(),
  },
}))

const mockedPost = vi.mocked(api.post)

describe('onboarding tutorial api', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('completeTutorial llama endpoint de completado', async () => {
    mockedPost.mockResolvedValueOnce(undefined)

    await completeTutorial()

    expect(mockedPost).toHaveBeenCalledWith('/onboarding/tutorial/complete', null)
  })

  it('completeProfileTutorial llama endpoint de completado del perfil', async () => {
    mockedPost.mockResolvedValueOnce(undefined)

    await completeProfileTutorial()

    expect(mockedPost).toHaveBeenCalledWith('/onboarding/profile-onboarding-tutorial/complete', null)
  })
})
