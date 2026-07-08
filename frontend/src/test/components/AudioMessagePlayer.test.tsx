import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, act, fireEvent } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import AudioMessagePlayer from '../../components/Chatbot/AudioMessagePlayer'
import { clickButton, verifyTextPresent } from '../testHelpers'

beforeEach(() => {
  HTMLMediaElement.prototype.play = vi.fn().mockResolvedValue(undefined)
  HTMLMediaElement.prototype.pause = vi.fn()
})

describe('AudioMessagePlayer', () => {
  it('renderiza el marcador de posición cuando no se proporciona audioUrl', () => {
    renderPlayer()
    verifyPlaceholderIsShown()
  })

  it('renderiza el spinner de carga cuando isLoading es true', () => {
    renderPlayer({ isLoading: true })
    verifyLoadingSpinnerIsShown()
  })

  it('renderiza el botón de reproducir cuando se proporciona audioUrl', () => {
    renderPlayer({ audioUrl: 'blob:fake-url' })
    verifyPlayButtonIsShown()
  })

  it('renderiza la barra de progreso cuando se proporciona audioUrl', () => {
    renderPlayer({ audioUrl: 'blob:fake-url' })
    verifyProgressBarIsShown()
  })

  it('al hacer click en el botón de reproducir se llama a audio.play()', () => {
    renderPlayer({ audioUrl: 'blob:fake-url' })
    return clickPlayButton().then(() => {
      verifyAudioPlayCalled()
    })
  })

  it('muestra la etiqueta del botón de pausa después de que el audio comience a reproducirse', () => {
    renderPlayer({ audioUrl: 'blob:fake-url' })
    triggerAudioPlayEvent()
    return verifyPauseButtonIsShown()
  })

  /* helpers */

  const renderPlayer = (props?: { audioUrl?: string; isLoading?: boolean }) => {
    render(<AudioMessagePlayer {...props} />)
  }

  const verifyPlaceholderIsShown = () => {
    verifyTextPresent('Mensaje de voz')
  }

  const verifyLoadingSpinnerIsShown = () => {
    expect(document.querySelector('.animate-pulse')).toBeInTheDocument()
  }

  const verifyPlayButtonIsShown = () => {
    expect(screen.getByRole('button', { name: 'Reproducir' })).toBeInTheDocument()
  }

  const verifyProgressBarIsShown = () => {
    expect(screen.getByRole('slider', { name: 'Progreso del audio' })).toBeInTheDocument()
  }

  const clickPlayButton = () => {
    const user = userEvent.setup()
    return clickButton(user, 'Reproducir')
  }

  const verifyAudioPlayCalled = () => {
    expect(HTMLMediaElement.prototype.play).toHaveBeenCalled()
  }

  const triggerAudioPlayEvent = () => {
    const audioEl = document.querySelector('audio') as HTMLAudioElement
    act(() => {
      fireEvent(audioEl, new Event('play'))
    })
  }

  const verifyPauseButtonIsShown = async () => {
    expect(await screen.findByRole('button', { name: 'Pausar' })).toBeInTheDocument()
  }
})
