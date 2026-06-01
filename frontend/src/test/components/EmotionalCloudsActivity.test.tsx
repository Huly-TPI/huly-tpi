import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import EmotionalCloudsActivity from '../../components/EmotionalClouds/EmotionalCloudsActivity'


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

const typeAndSubmit = (text: string) => {
  fireEvent.change(screen.getByTestId('emotional-input'), { target: { value: text } })
  fireEvent.click(screen.getByTestId('submit-button'))
}

describe('EmotionalCloudsActivity', () => {

  describe('renderizado inicial', () => {
    it('muestra el título y subtítulo', () => {
      render(<EmotionalCloudsActivity />)
      expect(screen.getByText('Nubes que pasan')).toBeInTheDocument()
      expect(screen.getByText(/deja ir tus pensamientos/i)).toBeInTheDocument()
    })

    it('muestra el input y el botón', () => {
      render(<EmotionalCloudsActivity />)
      expect(screen.getByTestId('emotional-input')).toBeInTheDocument()
      expect(screen.getByTestId('submit-button')).toBeInTheDocument()
    })

    it('el botón empieza deshabilitado', () => {
      render(<EmotionalCloudsActivity />)
      expect(screen.getByTestId('submit-button')).toBeDisabled()
    })

    it('no hay nubes al montar', () => {
      render(<EmotionalCloudsActivity />)
      expect(screen.queryByTestId(/^cloud-cloud-/)).not.toBeInTheDocument()
    })
  })

  describe('input', () => {
    it('habilita el botón al escribir texto', () => {
      render(<EmotionalCloudsActivity />)
      fireEvent.change(screen.getByTestId('emotional-input'), { target: { value: 'Trabajo' } })
      expect(screen.getByTestId('submit-button')).toBeEnabled()
    })

    it('no habilita el botón con solo espacios', () => {
      render(<EmotionalCloudsActivity />)
      fireEvent.change(screen.getByTestId('emotional-input'), { target: { value: '   ' } })
      expect(screen.getByTestId('submit-button')).toBeDisabled()
    })

    it('respeta el maxLength de 100 caracteres', () => {
      render(<EmotionalCloudsActivity />)
      expect(screen.getByTestId('emotional-input')).toHaveAttribute('maxLength', '100')
    })

    it('muestra el contador de caracteres', () => {
      render(<EmotionalCloudsActivity />)
      fireEvent.change(screen.getByTestId('emotional-input'), { target: { value: 'Hola' } })
      expect(screen.getByTestId('char-counter')).toHaveTextContent('4/100')
    })
  })

  describe('agregar nubes', () => {
    it('agrega una nube al hacer click en Soltar', () => {
      render(<EmotionalCloudsActivity />)
      typeAndSubmit('Mis miedos')
      expect(screen.getByText('Mis miedos')).toBeInTheDocument()
    })

    it('limpia el input después de agregar la nube', () => {
      render(<EmotionalCloudsActivity />)
      typeAndSubmit('Estrés')
      expect(screen.getByTestId('emotional-input')).toHaveValue('')
    })

    it('el botón vuelve a deshabilitarse después de enviar', () => {
      render(<EmotionalCloudsActivity />)
      typeAndSubmit('Trabajo')
      expect(screen.getByTestId('submit-button')).toBeDisabled()
    })

    it('agrega la nube al presionar Enter', () => {
      render(<EmotionalCloudsActivity />)
      const input = screen.getByTestId('emotional-input')
      fireEvent.change(input, { target: { value: 'Pendientes' } })
      fireEvent.keyDown(input, { key: 'Enter' })
      expect(screen.getByText('Pendientes')).toBeInTheDocument()
    })

    it('no agrega nube al presionar Shift+Enter', () => {
      render(<EmotionalCloudsActivity />)
      const input = screen.getByTestId('emotional-input')
      fireEvent.change(input, { target: { value: 'Algo' } })
      fireEvent.keyDown(input, { key: 'Enter', shiftKey: true })
      expect(screen.getByTestId('emotional-input')).toHaveValue('Algo')
      expect(screen.queryByText('Algo')).not.toBeInTheDocument()
    })

    it('llama a onFinish con el texto de la nube al seleccionarla', () => {
      const onFinish = vi.fn()
      render(<EmotionalCloudsActivity onFinish={onFinish} />)
      typeAndSubmit('Trabajo')
      fireEvent.click(screen.getByRole('button', { name: 'select' }))
      expect(onFinish).toHaveBeenCalledWith(['Trabajo'])
    })

    it('llama a onThoughtAdded con el texto al agregar una nube', () => {
      const onThoughtAdded = vi.fn()
      render(<EmotionalCloudsActivity onThoughtAdded={onThoughtAdded} />)
      typeAndSubmit('Estrés laboral')
      expect(onThoughtAdded).toHaveBeenCalledWith('Estrés laboral')
    })

    it('llama a onThoughtAdded por cada nube agregada', () => {
      const onThoughtAdded = vi.fn()
      render(<EmotionalCloudsActivity onThoughtAdded={onThoughtAdded} />)
      typeAndSubmit('Primer pensamiento')
      typeAndSubmit('Segundo pensamiento')
      expect(onThoughtAdded).toHaveBeenCalledTimes(2)
      expect(onThoughtAdded).toHaveBeenNthCalledWith(1, 'Primer pensamiento')
      expect(onThoughtAdded).toHaveBeenNthCalledWith(2, 'Segundo pensamiento')
    })

    it('trimea el texto antes de crear la nube', () => {
      render(<EmotionalCloudsActivity />)
      typeAndSubmit('  Estrés  ')
      expect(screen.getByText('Estrés')).toBeInTheDocument()
    })

    it('agrega múltiples nubes correctamente', () => {
      render(<EmotionalCloudsActivity />)
      typeAndSubmit('Miedo')
      typeAndSubmit('Trabajo')
      typeAndSubmit('Estrés')
      expect(screen.getByText('Miedo')).toBeInTheDocument()
      expect(screen.getByText('Trabajo')).toBeInTheDocument()
      expect(screen.getByText('Estrés')).toBeInTheDocument()
    })

    it('no agrega nube si el input está vacío', () => {
      render(<EmotionalCloudsActivity />)
      fireEvent.click(screen.getByTestId('submit-button'))
      expect(screen.queryByTestId(/^cloud-cloud-/)).not.toBeInTheDocument()
    })
  })

  describe('límite de nubes (maxClouds)', () => {
    it('no supera el máximo de nubes configurado', () => {
      render(<EmotionalCloudsActivity maxClouds={2} />)
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
      render(<EmotionalCloudsActivity />)
      typeAndSubmit('Trabajo')
      fireEvent.click(screen.getByText('Trabajo'))
      expect(screen.queryByText('Trabajo')).not.toBeInTheDocument()
    })

    it('elimina solo la nube correcta cuando hay varias', () => {
      render(<EmotionalCloudsActivity />)
      typeAndSubmit('Miedo')
      typeAndSubmit('Trabajo')
      fireEvent.click(screen.getByText('Miedo'))
      expect(screen.queryByText('Miedo')).not.toBeInTheDocument()
      expect(screen.getByText('Trabajo')).toBeInTheDocument()
    })
  })
})


