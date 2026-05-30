import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import Bubble from '../../components/Bubbles/Bubble'          
import type { Bubble as BubbleType } from '../../components/Bubbles/types'
import { fireEvent } from '@testing-library/react'

const bubble: BubbleType = {
  id: 'b-1',
  color: 'rgba(180, 230, 200, 0.55)',
  size: 80,
  driftX: 35,
  driftY: -25,
  animationDuration: 8,
  animationDelay: 0,
}

describe('Bubble', () => {
    it('rennderiza bien en el DOM', () => {
        render(<Bubble bubble={bubble} onClick={vi.fn()}/>)
        expect(screen.getByTestId('bubble-b-1')).toBeInTheDocument()
             })

             it('llama onClick con la burbuja y la posición al hacer click', async () => {
                const onClick = vi.fn()
                render(<Bubble bubble={bubble} onClick={onClick} />)
                await userEvent.click(screen.getByTestId('bubble-b-1'))
                expect(onClick).toHaveBeenCalledWith(
                bubble,
                expect.objectContaining({ x: expect.any(Number), y: expect.any(Number) })
                )
            })

              it('aplica el tamaño correcto via style', () => {
                    render(<Bubble bubble={bubble} onClick={vi.fn()} />)
                    expect(screen.getByTestId('bubble-b-1')).toHaveStyle({ width: '80px', height: '80px' })
                })

            it('aplica la clase bubble--popping cuando se hace click en una burbuja', async () => {
                render(<Bubble bubble={bubble} onClick={vi.fn()} popping={true} />)
                expect(screen.getByTestId('bubble-b-1')).toHaveClass('bubble--popping')
                })

             it('llama onPopEnd con el id al finalizar la animación', () => {
                    const onPopEnd = vi.fn()
                    render(<Bubble bubble={bubble} onClick={vi.fn()} popping={true} onPopEnd={onPopEnd} />)
                    const event = new Event('animationend', { bubbles: true })
                    Object.defineProperty(event, 'animationName', { value: 'bubble-pop' })
                    fireEvent(screen.getByTestId('bubble-b-1'), event)
                    expect(onPopEnd).toHaveBeenCalledWith('b-1')
                    })
             })