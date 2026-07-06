import { clearAllMocks, setupMockedPostResponse } from '../testHelpers'
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
    clearAllMocks()
  })

  it('completeTutorial llama endpoint de completado', () => {
    setupMockedPostResponse(undefined)
    return callCompleteTutorial().then(() => {
      verifyPostCalledWith('/onboarding/tutorial/complete', null)
    })
  })

  it('completeProfileTutorial llama endpoint de completado del perfil', () => {
    setupMockedPostResponse(undefined)
    return callCompleteProfileTutorial().then(() => {
      verifyPostCalledWith('/onboarding/profile-onboarding-tutorial/complete', null)
    })
  })

  /* helpers */

  

  const callCompleteTutorial = () => {
    return completeTutorial()
  }

  const callCompleteProfileTutorial = () => {
    return completeProfileTutorial()
  }

  const verifyPostCalledWith = (url: string, body: any) => {
    expect(mockedPost).toHaveBeenCalledWith(url, body)
  }
})
