import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import EmotionalCloudsActivity from '../../components/EmotionalClouds/EmotionalCloudsActivity'
import type { EmotionalCloudsProps } from '../../components/EmotionalClouds/types'

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

beforeEach(() => {
  vi.useFakeTimers()
})

afterEach(() => {
  vi.restoreAllMocks()
})

const renderActivity = (props: EmotionalCloudsProps = {}) =>
  render(
    <MemoryRouter>
      <EmotionalCloudsActivity {...props} />
    </MemoryRouter>,
  )

const typeAndSubmit = (text: string) => {
  fireEvent.change(screen.getByTestId('emotional-input'), { target: { value: text } })
  fireEvent.click(screen.getByTestId('submit-button'))
}

describe('EmotionalCloudsActivity', () => {

  describe('renderizado inicial', () => {
    it('muestra el título y subtítulo', () => {
      renderActivity()
      expect(screen.getByText('Nubes que pasan')).toBeInTheDocument()
      expect(screen.getByText(/deja ir tus pensamientos/i)).toBeInTheDocument()
    })

    it('muestra el input y el botón', () => {
      renderActivity()
      expect(screen.getByTestId('emotional-input')).toBeInTheDocument()
      expect(screen.getByTestId('submit-button')).toBeInTheDocument()
    })

    it('el botón empieza deshabilitado', () => {
      renderActivity()
      expect(screen.getByTestId('submit-button')).toBeDisabled()
    })

    it('no hay nubes al montar', () => {
      renderActivity()
      expect(screen.queryByTestId(/^cloud-cloud-/)).not.toBeInTheDocument()
    })
  })

  describe('input', () => {
    it('habilita el botón al escribir texto', () => {
      renderActivity()
      fireEvent.change(screen.getByTestId('emotional-input'), { target: { value: 'Trabajo' } })
      expect(screen.getByTestId('submit-button')).toBeEnabled()
    })

    it('no habilita el botón con solo espacios', () => {
      renderActivity()
      fireEvent.change(screen.getByTestId('emotional-input'), { target: { value: '   ' } })
      expect(screen.getByTestId('submit-button')).toBeDisabled()
    })

    it('respeta el maxLength de 100 caracteres', () => {
      renderActivity()
      expect(screen.getByTestId('emotional-input')).toHaveAttribute('maxLength', '100')
    })

    it('muestra el contador de caracteres', () => {
      renderActivity()
      fireEvent.change(screen.getByTestId('emotional-input'), { target: { value: 'Hola' } })
      expect(screen.getByTestId('char-counter')).toHaveTextContent('4/100')
    })
  })

  describe('agregar nubes', () => {
    it('agrega una nube al hacer click en Soltar', () => {
      renderActivity()
      typeAndSubmit('Mis miedos')
      expect(screen.getByText('Mis miedos')).toBeInTheDocument()
    })

    it('limpia el input después de agregar la nube', () => {
      renderActivity()
      typeAndSubmit('Estrés')
      expect(screen.getByTestId('emotional-input')).toHaveValue('')
    })

    it('el botón vuelve a deshabilitarse después de enviar', () => {
      renderActivity()
      typeAndSubmit('Trabajo')
      expect(screen.getByTestId('submit-button')).toBeDisabled()
    })

    it('agrega la nube al presionar Enter', () => {
      renderActivity()
      const input = screen.getByTestId('emotional-input')
      fireEvent.change(input, { target: { value: 'Pendientes' } })
      fireEvent.keyDown(input, { key: 'Enter' })
      expect(screen.getByText('Pendientes')).toBeInTheDocument()
    })

    it('no agrega nube al presionar Shift+Enter', () => {
      renderActivity()
      const input = screen.getByTestId('emotional-input')
      fireEvent.change(input, { target: { value: 'Algo' } })
      fireEvent.keyDown(input, { key: 'Enter', shiftKey: true })
      expect(screen.getByTestId('emotional-input')).toHaveValue('Algo')
      expect(screen.queryByText('Algo')).not.toBeInTheDocument()
    })

    it('llama a onFinish con el texto de la nube al seleccionarla', () => {
      const onFinish = vi.fn()
      renderActivity({ onFinish })
      typeAndSubmit('Trabajo')
      fireEvent.click(screen.getByRole('button', { name: 'select' }))
      expect(onFinish).toHaveBeenCalledWith(['Trabajo'])
    })

    it('llama a onThoughtAdded con el texto al agregar una nube', () => {
      const onThoughtAdded = vi.fn()
      renderActivity({ onThoughtAdded })
      typeAndSubmit('Estrés laboral')
      expect(onThoughtAdded).toHaveBeenCalledWith('Estrés laboral')
    })

    it('llama a onThoughtAdded por cada nube agregada', () => {
      const onThoughtAdded = vi.fn()
      renderActivity({ onThoughtAdded })
      typeAndSubmit('Primer pensamiento')
      typeAndSubmit('Segundo pensamiento')
      expect(onThoughtAdded).toHaveBeenCalledTimes(2)
      expect(onThoughtAdded).toHaveBeenNthCalledWith(1, 'Primer pensamiento')
      expect(onThoughtAdded).toHaveBeenNthCalledWith(2, 'Segundo pensamiento')
    })

    it('trimea el texto antes de crear la nube', () => {
      renderActivity()
      typeAndSubmit('  Estrés  ')
      expect(screen.getByText('Estrés')).toBeInTheDocument()
    })

    it('agrega múltiples nubes correctamente', () => {
      renderActivity()
      typeAndSubmit('Miedo')
      typeAndSubmit('Trabajo')
      typeAndSubmit('Estrés')
      expect(screen.getByText('Miedo')).toBeInTheDocument()
      expect(screen.getByText('Trabajo')).toBeInTheDocument()
      expect(screen.getByText('Estrés')).toBeInTheDocument()
    })

    it('no agrega nube si el input está vacío', () => {
      renderActivity()
      fireEvent.click(screen.getByTestId('submit-button'))
      expect(screen.queryByTestId(/^cloud-cloud-/)).not.toBeInTheDocument()
    })
  })

  describe('límite de nubes (maxClouds)', () => {
    it('no supera el máximo de nubes configurado', () => {
      renderActivity({ maxClouds: 2 })
      typeAndSubmit('Nube1')
      typeAndSubmit('Nube2')
      typeAndSubmit('Nube3')
      expect(screen.queryByText('Nube1')).not.toBeInTheDocument()
      expect(screen.getByText('Nube2')).toBeInTheDocument()
      expect(screen.getByText('Nube3')).toBeInTheDocument()
    })
  })

  describe('eliminar nubes', () => {
    it('elimina una nube al llamar onRemove', () => {
      renderActivity()
      typeAndSubmit('Trabajo')
      fireEvent.click(screen.getByText('Trabajo'))
      expect(screen.queryByText('Trabajo')).not.toBeInTheDocument()
    })

    it('elimina solo la nube correcta cuando hay varias', () => {
      renderActivity()
      typeAndSubmit('Miedo')
      typeAndSubmit('Trabajo')
      fireEvent.click(screen.getByText('Miedo'))
      expect(screen.queryByText('Miedo')).not.toBeInTheDocument()
      expect(screen.getByText('Trabajo')).toBeInTheDocument()
    })
  })
})