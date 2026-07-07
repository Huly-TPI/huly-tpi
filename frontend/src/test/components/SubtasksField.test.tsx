import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import SubtasksField from '../../components/Pending/SubtasksField'
import { clickButton, clickCheckbox, typePlaceholder } from '../testHelpers'

describe('SubtasksField', () => {
  it('agrega un ítem al presionar Enter', () => {
    const onAdd = vi.fn()
    renderField([], onAdd)

    return typeAndPressEnter('Comprar detergente').then(() => {
      verifyAddCalledWith(onAdd, 'Comprar detergente')
    })
  })

  it('agrega un ítem al clickear el botón +', () => {
    const onAdd = vi.fn()
    renderField([], onAdd)

    return typeDraft('Tender la cama')
      .then(() => clickAddButton())
      .then(() => {
        verifyAddCalledWith(onAdd, 'Tender la cama')
      })
  })

  it('no agrega texto vacío', () => {
    const onAdd = vi.fn()
    renderField([], onAdd)

    return clickAddButton().then(() => {
      verifyAddNotCalled(onAdd)
    })
  })

  it('llama a onToggle al tildar un ítem', () => {
    const onToggle = vi.fn()
    renderField([{ id: 1, text: 'Lavar platos', done: false }], vi.fn(), onToggle)

    return clickToggleCheckbox().then(() => {
      verifyToggleCalledWith(onToggle, 1)
    })
  })

  it('deshabilita el checkbox cuando no hay onToggle (modo borrador)', () => {
    renderField([{ id: 0, text: 'Borrador', done: false }])

    verifyCheckboxDisabled('Marcar Borrador')
  })

  it('llama a onDelete al eliminar un ítem', () => {
    const onDelete = vi.fn()
    renderField([{ id: 1, text: 'Lavar platos', done: false }], vi.fn(), undefined, onDelete)

    return clickDeleteButton('Lavar platos').then(() => {
      verifyDeleteCalledWith(onDelete, 1)
    })
  })

  /* helpers */

  const renderField = (
    items: { id: string | number; text: string; done: boolean }[],
    onAdd = vi.fn(),
    onToggle?: (id: string | number) => void,
    onDelete = vi.fn(),
  ) => {
    render(<SubtasksField items={items} onAdd={onAdd} onToggle={onToggle} onDelete={onDelete} />)
  }

  const typeDraft = (text: string) => {
    const user = userEvent.setup()
    return typePlaceholder(user, 'Agregar subtarea', text)
  }

  const typeAndPressEnter = (text: string) => {
    const user = userEvent.setup()
    return typePlaceholder(user, 'Agregar subtarea', `${text}{Enter}`)
  }

  const clickAddButton = () => {
    const user = userEvent.setup()
    return clickButton(user, 'Agregar subtarea')
  }

  const clickToggleCheckbox = () => {
    const user = userEvent.setup()
    return clickCheckbox(user)
  }

  const clickDeleteButton = (text: string) => {
    const user = userEvent.setup()
    return clickButton(user, `Eliminar ${text}`)
  }

  const verifyAddCalledWith = (onAdd: ReturnType<typeof vi.fn>, text: string) => {
    expect(onAdd).toHaveBeenCalledWith(text)
  }

  const verifyAddNotCalled = (onAdd: ReturnType<typeof vi.fn>) => {
    expect(onAdd).not.toHaveBeenCalled()
  }

  const verifyToggleCalledWith = (onToggle: ReturnType<typeof vi.fn>, id: number) => {
    expect(onToggle).toHaveBeenCalledWith(id)
  }

  const verifyDeleteCalledWith = (onDelete: ReturnType<typeof vi.fn>, id: number) => {
    expect(onDelete).toHaveBeenCalledWith(id)
  }

  const verifyCheckboxDisabled = (label: string) => {
    expect(screen.getByLabelText(label)).toBeDisabled()
  }
})
