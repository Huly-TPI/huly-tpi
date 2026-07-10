import { render, screen, fireEvent, act } from '@testing-library/react'
import { BreathingGuide } from '../../components/BreathingGuide'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { registerActivitySession } from '../../api/activities'
import {
  clickButton,
  verifyTextPresent,
  verifyTextNotPresent,
  verifyButtonPresent
} from '../testHelpers'

vi.mock('../../api/activities', () => ({
  registerActivitySession: vi.fn().mockResolvedValue({}),
  ActivityType: {
    BREATHING: 'BREATHING',
  },
}))

vi.mock('../../context/authGate', () => ({
  useAuthGate: () => ({
    requireAuth: (action: () => void) => action(),
  }),
}))

vi.mock('../../context/theme', () => ({
  useTheme: () => ({
    theme: 'light',
  }),
}))

vi.mock('../../components/Buttons/BackButton/BackButton', () => ({
  default: ({ label = 'Volver', onBeforeNavigate }: any) => (
    <button onClick={() => onBeforeNavigate?.()}>{`← ${label}`}</button>
  ),
}))

vi.mock('../../hooks/store/useInventory', () => ({
  useInventory: () => ({
    inventory: [],
  }),
}))

vi.mock('../../components/HulyAvatar/HulyAvatar', () => ({
  default: ({ animation }: any) => <div data-testid="huly-avatar-mock" data-animation={animation} />
}))

describe('BreathingGuide', () => {
  let user: ReturnType<typeof userEvent.setup>

  afterEach(() => {
    vi.useRealTimers()
  })

  // --- CASOS DE PRUEBA (TEST SUITE) ---

  it('muestra botón para iniciar ejercicios tras seleccionar técnica', () => {
    renderBreathing()
    return selectTechnique('Diafragmática').then(() => {
      verifyButtonPresent('Iniciar')
    })
  })

  it('muestra las técnicas de respiración disponibles', () => {
    renderBreathing()
    verifyTextPresent('Diafragmática')
    verifyTextPresent('Cuadrada')
  })

  it('al seleccionar una técnica oculta la selección y muestra el ejercicio', () => {
    renderBreathing()
    return selectTechnique('Diafragmática').then(() => {
      verifyTextNotPresent('Tómate un momento, Elegí un método y deja que el círculo acompañe tu respiración')
    })
  })

  it('al iniciar el ejercicio muestra la primera fase', () => {
    renderBreathing()
    return selectTechnique('Diafragmática')
      .then(() => startExercise())
      .then(() => {
        verifyTextNotPresent('Inhala profundamente')
        verifyTextPresent('Inhalá')
      })
  })

  it('muestra el tiempo restante de la fase actual al iniciar el ejercicio', () => {
    renderBreathing()
    selectTechniqueSync('Diafragmática')
    startExerciseSync()
    verifyRemainingSecondsShown(4)
  })

  it('el temporizador descuenta segundo por segundo', () => {
    enableFakeTimers()
    renderBreathing()
    selectTechniqueSync('Diafragmática')
    startExerciseSync()
    verifyRemainingSecondsShown(4)
    advanceTimeSeconds(1)
    verifyRemainingSecondsShown(3)
  })

  it('al finalizar una fase pasa a la siguiente', () => {
    enableFakeTimers()
    renderBreathing()
    selectTechniqueSync('Cuadrada')
    startExerciseSync()
    verifyTextPresent('Inhalá')
    advanceTimeSeconds(4)
    verifyTextPresent('Sostenélo')
  })

  it('al finalizar una ronda vuelve a la primera fase y repite el proceso', () => {
    enableFakeTimers()
    renderBreathing()
    selectTechniqueSync('Diafragmática')
    startExerciseSync()
    verifyTextPresent('Inhalá')
    advanceTimeSeconds(8)
    verifyTextPresent('Inhalá')
  })

  it('al hacer click en volver regresa a la selección de técnicas', () => {
    renderBreathing()
    return selectTechnique('Diafragmática')
      .then(() => clickNavigationButton('← Volver'))
      .then(() => {
        verifyTextPresent('Tómate un momento, Elegí un método y deja que el círculo acompañe tu respiración')
      })
  })

  it('al terminar una respiración corta regresa a la selección sin registrar sesión', () => {
    const shortTechnique = {
      id: 3,
      name: 'Corta',
      description: 'Inhala por 1 segundo, sostene por 1 segundo, exhala por 1 segundo.',
      inhaleSeconds: 1,
      holdSeconds: 1,
      exhaleSeconds: 1,
      roundsInterval: 1,
      rounds: 2,
    }
    enableFakeTimers()
    renderBreathing([shortTechnique])
    selectTechniqueSync('Corta')
    startExerciseSync()
    advanceTimeSeconds(6)
    verifyTextPresent('Respiración guiada')
    verifySessionRegistered(false)
  })

  it('registra la sesión si la respiración dura más de 10 segundos', () => {
    const longTechnique = {
      id: 4,
      name: 'Larga',
      description: 'Inhala por 2 segundos, sostene por 1 segundo, exhala por 1 segundo.',
      inhaleSeconds: 2,
      holdSeconds: 1,
      exhaleSeconds: 1,
      roundsInterval: 1,
      rounds: 3,
    }
    enableFakeTimers()
    renderBreathing([longTechnique])
    selectTechniqueSync('Larga')
    startExerciseSync()
    advanceTimeSeconds(12)
    verifyTextPresent('Respiración guiada')
    verifySessionRegistered(true)
  })

  it('muestra botón volver durante el ejercicio', () => {
    renderBreathing()
    selectTechniqueSync('Diafragmática')
    startExerciseSync()
    verifyButtonPresent('← Volver')
  })

  it('aplica la clase CSS correcta según la fase activa', () => {
    renderBreathing()
    selectTechniqueSync('Diafragmática')
    startExerciseSync()
    verifyActivePhaseClass('inhale')
  })

  it('pausa el ejercicio al hacer click en pausar', () => {
    enableFakeTimers()
    renderBreathing()
    selectTechniqueSync('Diafragmática')
    startExerciseSync()
    verifyRemainingSecondsShown(4)
    pauseExerciseSync()
    advanceTimeSeconds(2)
    verifyRemainingSecondsShown(4)
  })

  it('muestra la imagen de huly al inhalar', () => {
    enableFakeTimers()
    renderBreathing()
    selectTechniqueSync('Diafragmática')
    startExerciseSync()
    verifyHulyAnimation('inhale')
  })

  it('muestra la imagen de huly al exhalar', () => {
    enableFakeTimers()
    renderBreathing()
    selectTechniqueSync('Diafragmática')
    startExerciseSync()
    advanceTimeSeconds(4)
    verifyHulyAnimation('blow')
  })

  it('muestra la imagen de huly normal al sostener', () => {
    enableFakeTimers()
    renderBreathing()
    selectTechniqueSync('Cuadrada')
    startExerciseSync()
    advanceTimeSeconds(4)
    verifyHulyAnimation('hold')
  })

  /* helpers */

  const renderBreathing = (techniques?: any[]) => {
    user = userEvent.setup()
    render(<BreathingGuide techniques={techniques} />)
  }

  const enableFakeTimers = () => {
    vi.useFakeTimers()
  }

  const advanceTimeSeconds = (seconds: number) => {
    for (let i = 0; i < seconds; i++) {
      act(() => {
        vi.advanceTimersByTime(1000)
      })
    }
  }

  const selectTechnique = async (name: string) => {
    await clickButton(user, name)
  }

  const selectTechniqueSync = (name: string) => {
    fireEvent.click(screen.getByRole('button', { name }))
  }

  const startExercise = async () => {
    await clickButton(user, 'Iniciar')
  }

  const startExerciseSync = () => {
    fireEvent.click(screen.getByRole('button', { name: 'Iniciar' }))
  }

  const pauseExerciseSync = () => {
    fireEvent.click(screen.getByRole('button', { name: 'Pausar' }))
  }

  const verifyRemainingSecondsShown = (seconds: number) => {
    expect(screen.getByText(String(seconds))).toBeInTheDocument()
  }

  const clickNavigationButton = async (name: string) => {
    await clickButton(user, name)
  }

  const verifySessionRegistered = (registered: boolean) => {
    if (registered) {
      expect(registerActivitySession).toHaveBeenCalled()
    } else {
      expect(registerActivitySession).not.toHaveBeenCalled()
    }
  }

  const verifyActivePhaseClass = (className: string) => {
    const circle = screen.getByTestId('breathing-circle')
    expect(circle).toHaveClass(className)
  }

  const verifyHulyAnimation = (expectedAnimation: string) => {
    const avatar = screen.getByTestId('huly-avatar-mock')
    expect(avatar).toHaveAttribute('data-animation', expectedAnimation)
  }
})
