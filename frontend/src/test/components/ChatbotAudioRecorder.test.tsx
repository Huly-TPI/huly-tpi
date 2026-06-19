import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, act } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import ChatbotAudioRecorder from '../../components/Chatbot/ChatbotAudioRecorder'

// --- MediaRecorder mock ---

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
  vi.clearAllMocks()
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
  it('renders microphone button in idle state', () => {
    render(
      <ChatbotAudioRecorder onSend={vi.fn()} disabled={false} onActiveChange={vi.fn()} />,
    )

    expect(screen.getByRole('button', { name: 'Grabar audio' })).toBeInTheDocument()
  })

  it('microphone button is disabled when disabled prop is true', () => {
    render(
      <ChatbotAudioRecorder onSend={vi.fn()} disabled={true} onActiveChange={vi.fn()} />,
    )

    expect(screen.getByRole('button', { name: 'Grabar audio' })).toBeDisabled()
  })

  it('clicking mic button calls getUserMedia with audio: true', async () => {
    const user = userEvent.setup()
    render(
      <ChatbotAudioRecorder onSend={vi.fn()} disabled={false} onActiveChange={vi.fn()} />,
    )

    await user.click(screen.getByRole('button', { name: 'Grabar audio' }))

    expect(mockGetUserMedia).toHaveBeenCalledWith({ audio: true })
  })

  it('transitions to recording state after getUserMedia succeeds', async () => {
    const user = userEvent.setup()
    render(
      <ChatbotAudioRecorder onSend={vi.fn()} disabled={false} onActiveChange={vi.fn()} />,
    )

    await user.click(screen.getByRole('button', { name: 'Grabar audio' }))

    expect(screen.getByText(/Grabando\.\.\./)).toBeInTheDocument()
  })

  it('calls onActiveChange(true) when recording starts', async () => {
    const user = userEvent.setup()
    const onActiveChange = vi.fn()
    render(
      <ChatbotAudioRecorder onSend={vi.fn()} disabled={false} onActiveChange={onActiveChange} />,
    )

    await user.click(screen.getByRole('button', { name: 'Grabar audio' }))

    expect(onActiveChange).toHaveBeenCalledWith(true)
  })

  it('clicking stop button calls mediaRecorder.stop()', async () => {
    const user = userEvent.setup()
    render(
      <ChatbotAudioRecorder onSend={vi.fn()} disabled={false} onActiveChange={vi.fn()} />,
    )

    await user.click(screen.getByRole('button', { name: 'Grabar audio' }))
    await user.click(screen.getByRole('button', { name: 'Detener grabación' }))

    expect(mockStop).toHaveBeenCalled()
  })

  it('transitions to recorded state after onstop fires', async () => {
    const user = userEvent.setup()
    render(
      <ChatbotAudioRecorder onSend={vi.fn()} disabled={false} onActiveChange={vi.fn()} />,
    )

    await user.click(screen.getByRole('button', { name: 'Grabar audio' }))

    await act(async () => {
      capturedOnStop?.()
    })

    expect(screen.getByRole('button', { name: 'Eliminar grabación' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Enviar audio' })).toBeInTheDocument()
  })

  it('clicking discard resets to idle and calls onActiveChange(false)', async () => {
    const user = userEvent.setup()
    const onActiveChange = vi.fn()
    render(
      <ChatbotAudioRecorder onSend={vi.fn()} disabled={false} onActiveChange={onActiveChange} />,
    )

    await user.click(screen.getByRole('button', { name: 'Grabar audio' }))
    await act(async () => { capturedOnStop?.() })
    await user.click(screen.getByRole('button', { name: 'Eliminar grabación' }))

    expect(screen.getByRole('button', { name: 'Grabar audio' })).toBeInTheDocument()
    expect(onActiveChange).toHaveBeenLastCalledWith(false)
  })

  it('clicking send calls onSend with blob and resets to idle', async () => {
    const user = userEvent.setup()
    const onSend = vi.fn()
    render(
      <ChatbotAudioRecorder onSend={onSend} disabled={false} onActiveChange={vi.fn()} />,
    )

    await user.click(screen.getByRole('button', { name: 'Grabar audio' }))
    await act(async () => { capturedOnStop?.() })
    await user.click(screen.getByRole('button', { name: 'Enviar audio' }))

    expect(onSend).toHaveBeenCalledWith(expect.any(Blob))
    expect(screen.getByRole('button', { name: 'Grabar audio' })).toBeInTheDocument()
  })

  it('getUserMedia permission denied silently stays in idle state', async () => {
    mockGetUserMedia.mockRejectedValueOnce(new Error('Permission denied'))
    const user = userEvent.setup()
    render(
      <ChatbotAudioRecorder onSend={vi.fn()} disabled={false} onActiveChange={vi.fn()} />,
    )

    await user.click(screen.getByRole('button', { name: 'Grabar audio' }))

    expect(screen.getByRole('button', { name: 'Grabar audio' })).toBeInTheDocument()
  })
})
