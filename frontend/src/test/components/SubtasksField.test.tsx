import { describe, it, expect, vi } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import SubtasksField from '../../components/Pending/SubtasksField'

describe('SubtasksField', () => {
  it('agrega un ítem al presionar Enter', () => {
    const onAdd = vi.fn()
    render(<SubtasksField items={[]} onAdd={onAdd} onDelete={vi.fn()} />)

    const input = screen.getByPlaceholderText('Agregar subtarea')
    fireEvent.change(input, { target: { value: 'Comprar detergente' } })
    fireEvent.keyDown(input, { key: 'Enter' })

    expect(onAdd).toHaveBeenCalledWith('Comprar detergente')
  })

  it('agrega un ítem al clickear el botón +', () => {
    const onAdd = vi.fn()
    render(<SubtasksField items={[]} onAdd={onAdd} onDelete={vi.fn()} />)

    fireEvent.change(screen.getByPlaceholderText('Agregar subtarea'), { target: { value: 'Tender la cama' } })
    fireEvent.click(screen.getByLabelText('Agregar subtarea'))

    expect(onAdd).toHaveBeenCalledWith('Tender la cama')
  })

  it('no agrega texto vacío', () => {
    const onAdd = vi.fn()
    render(<SubtasksField items={[]} onAdd={onAdd} onDelete={vi.fn()} />)

    fireEvent.click(screen.getByLabelText('Agregar subtarea'))

    expect(onAdd).not.toHaveBeenCalled()
  })

  it('llama a onToggle al tildar un ítem', () => {
    const onToggle = vi.fn()
    render(
      <SubtasksField
        items={[{ id: 1, text: 'Lavar platos', done: false }]}
        onAdd={vi.fn()}
        onToggle={onToggle}
        onDelete={vi.fn()}
      />,
    )

    fireEvent.click(screen.getByLabelText('Marcar Lavar platos'))

    expect(onToggle).toHaveBeenCalledWith(1)
  })

  it('deshabilita el checkbox cuando no hay onToggle (modo borrador)', () => {
    render(<SubtasksField items={[{ id: 0, text: 'Borrador', done: false }]} onAdd={vi.fn()} onDelete={vi.fn()} />)

    expect(screen.getByLabelText('Marcar Borrador')).toBeDisabled()
  })

  it('llama a onDelete al eliminar un ítem', () => {
    const onDelete = vi.fn()
    render(
      <SubtasksField
        items={[{ id: 1, text: 'Lavar platos', done: false }]}
        onAdd={vi.fn()}
        onDelete={onDelete}
      />,
    )

    fireEvent.click(screen.getByLabelText('Eliminar Lavar platos'))

    expect(onDelete).toHaveBeenCalledWith(1)
  })
})
