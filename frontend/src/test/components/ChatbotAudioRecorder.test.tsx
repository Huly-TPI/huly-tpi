import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, act } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import ChatbotAudioRecorder from '../../components/Chatbot/ChatbotAudioRecorder'
import { clickButton, verifyButtonDisabled, clearAllMocks } from '../testHelpers'

let capturedOnStop: (() => void) | null = null
let capturedOnDataAvailable: ((e: { data: { size: number } }) => void) | null = null

const mockStart = vi.fn()
const mockStop = vi.fn()

const MockMediaRecorder = vi.fn().mockImplementation(() => ({
  start: mockStart,
  stop: mockStop,
  mimeType: 'audio/webm',
  get ondataavailable() {
    return capturedOnDataAvailable
  },
  set ondataavailable(fn: ((e: { data: { size: number } }) => void) | null) {
    capturedOnDataAvailable = fn
  },
  get onstop() {
    return capturedOnStop
  },
  set onstop(fn: (() => void) | null) {
    capturedOnStop = fn
  },
}))

const mockGetUserMedia = vi.fn()
const mockStream = {
  getTracks: vi.fn().mockReturnValue([{ stop: vi.fn() }]),
}

beforeEach(() => {
  clearAllMocks()
  capturedOnStop = null
  capturedOnDataAvailable = null

  global.MediaRecorder = MockMediaRecorder as unknown as typeof MediaRecorder
  global.URL.createObjectURL = vi.fn().mockReturnValue('blob:fake-url')
  global.URL.revokeObjectURL = vi.fn()

  Object.defineProperty(global.navigator, 'mediaDevices', {
    value: { getUserMedia: mockGetUserMedia },
    writable: true,
    configurable: true,
  })
  mockGetUserMedia.mockResolvedValue(mockStream)
})

describe('ChatbotAudioRecorder', () => {
  let onSendMock: any
  let onActiveChangeMock: any

  beforeEach(() => {
    onSendMock = vi.fn()
    onActiveChangeMock = vi.fn()
  })

  it('renderiza el botón del micrófono en estado inactivo', () => {
    renderRecorder()
    verifyMicButtonPresent()
  })

  it('el botón del micrófono está deshabilitado cuando la prop disabled es true', () => {
    renderRecorder(true)
    verifyMicButtonDisabled()
  })

  it('al hacer click en el botón del micrófono se llama a getUserMedia con audio: true', () => {
    renderRecorder()
    return clickMicButton().then(() => {
      verifyGetUserMediaCalled()
    })
  })

  it('pasa al estado de grabación después de que getUserMedia se resuelva con éxito', () => {
    renderRecorder()
    return clickMicButton().then(() => {
      verifyRecordingStateShown()
    })
  })

  it('llama a onActiveChange(true) cuando comienza la grabación', () => {
    renderRecorder()
    return clickMicButton().then(() => {
      verifyOnActiveChangeCalledWith(true)
    })
  })

  it('al hacer click en el botón de detener se llama a mediaRecorder.stop()', () => {
    renderRecorder()
    return clickMicButton()
      .then(() => clickStopButton())
      .then(() => {
        verifyMediaRecorderStopCalled()
      })
  })

  it('pasa al estado de grabado después de que se dispare onstop', () => {
    renderRecorder()
    return clickMicButton()
      .then(() => triggerOnStopEvent())
      .then(() => {
        verifyRecordedStateButtonsShown()
      })
  })

  it('al hacer click en descartar se reinicia a inactivo y llama a onActiveChange(false)', () => {
    renderRecorder()
    return clickMicButton()
      .then(() => triggerOnStopEvent())
      .then(() => clickDiscardButton())
      .then(() => {
        verifyMicButtonPresent()
        verifyOnActiveChangeLastCalledWith(false)
      })
  })

  it('al hacer click en enviar se llama a onSend con el blob y se reinicia a inactivo', () => {
    renderRecorder()
    return clickMicButton()
      .then(() => triggerOnStopEvent())
      .then(() => clickSendButton())
      .then(() => {
        verifyOnSendCalledWithBlob()
        verifyMicButtonPresent()
      })
  })

  it('si se deniega el permiso de getUserMedia se mantiene silenciosamente en estado inactivo', () => {
    setupGetUserMediaRejected()
    renderRecorder()
    return clickMicButton().then(() => {
      verifyMicButtonPresent()
    })
  })

  /* helpers */

  const renderRecorder = (disabled: boolean = false) => {
    render(
      <ChatbotAudioRecorder
        onSend={onSendMock}
        disabled={disabled}
        onActiveChange={onActiveChangeMock}
      />
    )
  }

  const verifyMicButtonPresent = () => {
    expect(screen.getByRole('button', { name: 'Grabar audio' })).toBeInTheDocument()
  }

  const verifyMicButtonDisabled = () => {
    verifyButtonDisabled('Grabar audio')
  }

  const clickMicButton = () => {
    const user = userEvent.setup()
    return clickButton(user, 'Grabar audio')
  }

  const verifyGetUserMediaCalled = () => {
    expect(mockGetUserMedia).toHaveBeenCalledWith({ audio: true })
  }

  const verifyRecordingStateShown = () => {
    expect(screen.getByText('Grabando...', { exact: false })).toBeInTheDocument()
  }

  const verifyOnActiveChangeCalledWith = (active: boolean) => {
    expect(onActiveChangeMock).toHaveBeenCalledWith(active)
  }

  const verifyOnActiveChangeLastCalledWith = (active: boolean) => {
    expect(onActiveChangeMock).toHaveBeenLastCalledWith(active)
  }

  const clickStopButton = () => {
    const user = userEvent.setup()
    return clickButton(user, 'Detener grabación')
  }

  const verifyMediaRecorderStopCalled = () => {
    expect(mockStop).toHaveBeenCalled()
  }

  const triggerOnStopEvent = () => {
    return act(async () => {
      capturedOnStop?.()
    })
  }

  const verifyRecordedStateButtonsShown = () => {
    expect(screen.getByRole('button', { name: 'Eliminar grabación' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Enviar audio' })).toBeInTheDocument()
  }

  const clickDiscardButton = () => {
    const user = userEvent.setup()
    return clickButton(user, 'Eliminar grabación')
  }

  const clickSendButton = () => {
    const user = userEvent.setup()
    return clickButton(user, 'Enviar audio')
  }

  const verifyOnSendCalledWithBlob = () => {
    expect(onSendMock).toHaveBeenCalledWith(expect.any(Blob))
  }

  const setupGetUserMediaRejected = () => {
    mockGetUserMedia.mockRejectedValueOnce(new Error('Permission denied'))
  }
})
