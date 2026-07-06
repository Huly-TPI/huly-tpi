import { verifyTextPresent, verifyTextNotPresent } from '../testHelpers'
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import EmotionalCloudsActivity from '../../components/EmotionalClouds/EmotionalCloudsActivity'
import type { EmotionalCloudsProps } from '../../components/EmotionalClouds/types'

vi.mock('../../context/authGate', () => ({
  useAuthGate: () => ({
    requireAuth: (action: () => void) => action(),
  }),
}))

vi.mock('./EmotionalClouds.css', () => ({}))

vi.mock('../../components/EmotionalClouds/EmotionalCloud', () => ({
  default: ({ cloud, onRemove, onSelect }: {
    cloud: { id: string; text: string }
    onRemove: () => void
    onSelect?: () => void
  }) => (
    <div data-testid={`cloud-${cloud.id}`}>
      <span onClick={onRemove}>{cloud.text}</span>
      <button onClick={onSelect}>select</button>
    </div>
  ),
}))

describe('EmotionalCloudsActivity', () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  // --- CASOS DE PRUEBA (TEST SUITE) ---

  describe('renderizado inicial', () => {
    it('muestra el título y subtítulo', () => {
      renderActivity()
      verifyTextPresent('Nubes que pasan')
      verifyTextPresent('Deja ir tus pensamientos como nubes')
    })

    it('muestra el input y el botón', () => {
      renderActivity()
      verifyInputIsPresent()
      verifySubmitButtonIsPresent()
    })

    it('el botón empieza deshabilitado', () => {
      renderActivity()
      verifySubmitButtonDisabled()
    })

    it('no hay nubes al montar', () => {
      renderActivity()
      verifyNoCloudsRendered()
    })
  })

  describe('input', () => {
    it('habilita el botón al escribir texto', () => {
      renderActivity()
      typeText('Trabajo')
      verifySubmitButtonEnabled()
    })

    it('no habilita el botón con solo espacios', () => {
      renderActivity()
      typeText('   ')
      verifySubmitButtonDisabled()
    })

    it('respeta el maxLength de 100 caracteres', () => {
      renderActivity()
      verifyInputMaxLength(100)
    })

    it('muestra el contador de caracteres', () => {
      renderActivity()
      typeText('Hola')
      verifyCounterText('4/100')
    })
  })

  describe('agregar nubes', () => {
    it('agrega una nube al hacer click en Soltar', () => {
      renderActivity()
      typeAndSubmit('Mis miedos')
      verifyTextPresent('Mis miedos')
    })

    it('limpia el input después de agregar la nube', () => {
      renderActivity()
      typeAndSubmit('Estrés')
      verifyInputValue('')
    })

    it('el botón vuelve a deshabilitarse después de enviar', () => {
      renderActivity()
      typeAndSubmit('Trabajo')
      verifySubmitButtonDisabled()
    })

    it('agrega la nube al presionar Enter', () => {
      renderActivity()
      typeText('Pendientes')
      pressEnterKey()
      verifyTextPresent('Pendientes')
    })

    it('no agrega nube al presionar Shift+Enter', () => {
      renderActivity()
      typeText('Algo')
      pressShiftEnterKey()
      verifyInputValue('Algo')
      verifyTextNotPresent('Algo')
    })

    it('llama a onFinish con el texto de la nube al seleccionarla', () => {
      const onFinish = vi.fn()
      renderActivity({ onFinish })
      typeAndSubmit('Trabajo')
      clickChooseButton()
      verifyOnFinishCalledWith(onFinish, ['Trabajo'])
    })

    it('llama a onThoughtAdded con el texto al agregar una nube', () => {
      const onThoughtAdded = vi.fn()
      renderActivity({ onThoughtAdded })
      typeAndSubmit('Estrés laboral')
      verifyOnThoughtAddedCalledWith(onThoughtAdded, 'Estrés laboral')
    })

    it('llama a onThoughtAdded por cada nube agregada', () => {
      const onThoughtAdded = vi.fn()
      renderActivity({ onThoughtAdded })
      typeAndSubmit('Primer pensamiento')
      typeAndSubmit('Segundo pensamiento')
      verifyOnThoughtAddedCalledTimes(onThoughtAdded, 2)
      verifyOnThoughtAddedNthCalledWith(onThoughtAdded, 1, 'Primer pensamiento')
      verifyOnThoughtAddedNthCalledWith(onThoughtAdded, 2, 'Segundo pensamiento')
    })

    it('trimea el texto antes de crear la nube', () => {
      renderActivity()
      typeAndSubmit('  Estrés  ')
      verifyTextPresent('Estrés')
    })

    it('agrega múltiples nubes correctamente', () => {
      renderActivity()
      typeAndSubmit('Miedo')
      typeAndSubmit('Trabajo')
      typeAndSubmit('Estrés')
      verifyTextPresent('Miedo')
      verifyTextPresent('Trabajo')
      verifyTextPresent('Estrés')
    })

    it('no agrega nube si el input está vacío', () => {
      renderActivity()
      clickSubmitButton()
      verifyNoCloudsRendered()
    })
  })

  describe('límite de nubes (maxClouds)', () => {
    it('no supera el máximo de nubes configurado', () => {
      renderActivity({ maxClouds: 2 })
      typeAndSubmit('Nube1')
      typeAndSubmit('Nube2')
      typeAndSubmit('Nube3')
      verifyTextNotPresent('Nube1')
      verifyTextPresent('Nube2')
      verifyTextPresent('Nube3')
    })
  })

  describe('eliminar nubes', () => {
    it('elimina una nube al llamar onRemove', () => {
      renderActivity()
      typeAndSubmit('Trabajo')
      clickOnText('Trabajo')
      verifyTextNotPresent('Trabajo')
    })

    it('elimina solo la nube correcta cuando hay varias', () => {
      renderActivity()
      typeAndSubmit('Miedo')
      typeAndSubmit('Trabajo')
      clickOnText('Miedo')
      verifyTextNotPresent('Miedo')
      verifyTextPresent('Trabajo')
    })
  })

  /* helpers */

  const renderActivity = (props: EmotionalCloudsProps = {}) => {
    render(
      <MemoryRouter>
        <EmotionalCloudsActivity {...props} />
      </MemoryRouter>
    )
  }

  const typeText = (text: string) => {
    fireEvent.change(screen.getByTestId('emotional-input'), { target: { value: text } })
  }

  const clickSubmitButton = () => {
    fireEvent.click(screen.getByTestId('submit-button'))
  }

  const typeAndSubmit = (text: string) => {
    typeText(text)
    clickSubmitButton()
  }

  const pressEnterKey = () => {
    fireEvent.keyDown(screen.getByTestId('emotional-input'), { key: 'Enter' })
  }

  const pressShiftEnterKey = () => {
    fireEvent.keyDown(screen.getByTestId('emotional-input'), { key: 'Enter', shiftKey: true })
  }

  const clickChooseButton = () => {
    fireEvent.click(screen.getByRole('button', { name: 'select' }))
  }

  const clickOnText = (text: string) => {
    fireEvent.click(screen.getByText(text))
  }

  

  

  const verifyInputIsPresent = () => {
    expect(screen.getByTestId('emotional-input')).toBeInTheDocument()
  }

  const verifySubmitButtonIsPresent = () => {
    expect(screen.getByTestId('submit-button')).toBeInTheDocument()
  }

  const verifySubmitButtonDisabled = () => {
    expect(screen.getByTestId('submit-button')).toBeDisabled()
  }

  const verifySubmitButtonEnabled = () => {
    expect(screen.getByTestId('submit-button')).toBeEnabled()
  }

  const verifyNoCloudsRendered = () => {
    expect(screen.queryByTestId(/^cloud-cloud-/)).not.toBeInTheDocument()
  }

  const verifyInputMaxLength = (max: number) => {
    expect(screen.getByTestId('emotional-input')).toHaveAttribute('maxLength', String(max))
  }

  const verifyCounterText = (text: string) => {
    expect(screen.getByTestId('char-counter')).toHaveTextContent(text)
  }

  const verifyInputValue = (val: string) => {
    expect(screen.getByTestId('emotional-input')).toHaveValue(val)
  }

  const verifyOnFinishCalledWith = (fn: any, args: any) => {
    expect(fn).toHaveBeenCalledWith(args)
  }

  const verifyOnThoughtAddedCalledWith = (fn: any, arg: any) => {
    expect(fn).toHaveBeenCalledWith(arg)
  }

  const verifyOnThoughtAddedCalledTimes = (fn: any, times: number) => {
    expect(fn).toHaveBeenCalledTimes(times)
  }

  const verifyOnThoughtAddedNthCalledWith = (fn: any, n: number, arg: any) => {
    expect(fn).toHaveBeenNthCalledWith(n, arg)
  }
})