import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, act, fireEvent } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import AudioMessagePlayer from '../../components/Chatbot/AudioMessagePlayer'

beforeEach(() => {
  HTMLMediaElement.prototype.play = vi.fn().mockResolvedValue(undefined)
  HTMLMediaElement.prototype.pause = vi.fn()
})

describe('AudioMessagePlayer', () => {
  it('renders placeholder when no audioUrl is provided', () => {
    render(<AudioMessagePlayer />)

    expect(screen.getByText('Mensaje de voz')).toBeInTheDocument()
  })

  it('renders loading spinner when isLoading is true', () => {
    const { container } = render(<AudioMessagePlayer isLoading={true} />)

    expect(container.querySelector('.animate-pulse')).toBeInTheDocument()
  })

  it('renders play button when audioUrl is provided', () => {
    render(<AudioMessagePlayer audioUrl="blob:fake-url" />)

    expect(screen.getByRole('button', { name: 'Reproducir' })).toBeInTheDocument()
  })

  it('renders progress bar when audioUrl is provided', () => {
    render(<AudioMessagePlayer audioUrl="blob:fake-url" />)

    expect(screen.getByRole('slider', { name: 'Progreso del audio' })).toBeInTheDocument()
  })

  it('clicking play button calls audio.play()', async () => {
    const user = userEvent.setup()
    render(<AudioMessagePlayer audioUrl="blob:fake-url" />)

    await user.click(screen.getByRole('button', { name: 'Reproducir' }))

    expect(HTMLMediaElement.prototype.play).toHaveBeenCalled()
  })

  it('shows pause button label after audio starts playing', async () => {
    const { container } = render(<AudioMessagePlayer audioUrl="blob:fake-url" />)
    const audioEl = container.querySelector('audio') as HTMLAudioElement

    await act(async () => {
      fireEvent(audioEl, new Event('play'))
    })

    expect(screen.getByRole('button', { name: 'Pausar' })).toBeInTheDocument()
  })
})
