import { describe, it, expect, vi, beforeEach } from 'vitest'
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
    let onClickMock: any
    let onPopEndMock: any

    beforeEach(() => {
        onClickMock = vi.fn()
        onPopEndMock = vi.fn()
    })

    it('renderiza bien en el DOM', () => {
        renderBubble()
        verifyBubbleRendered()
    })

    it('llama onClick con la burbuja y la posición al hacer click', () => {
        renderBubble()
        return clickBubble().then(() => {
            verifyOnClickCalled()
        })
    })

    it('aplica el tamaño correcto via style', () => {
        renderBubble()
        verifyBubbleSize('80px')
    })

    it('aplica la clase bubble--popping cuando se hace click en una burbuja', () => {
        renderBubble(true)
        verifyPoppingClassApplied()
    })

    it('llama onPopEnd con el id al finalizar la animación', () => {
        renderBubble(true)
        triggerAnimationEnd('bubble-pop')
        verifyOnPopEndCalledWithId('b-1')
    })
    const renderBubble = (popping: boolean = false) => {
        render(
            <Bubble
                bubble={bubble}
                onClick={onClickMock}
                popping={popping}
                onPopEnd={onPopEndMock}
            />
        )
    }

    const verifyBubbleRendered = () => {
        expect(screen.getByTestId('bubble-b-1')).toBeInTheDocument()
    }

    const clickBubble = () => {
        const user = userEvent.setup()
        return user.click(screen.getByTestId('bubble-b-1'))
    }

    const verifyOnClickCalled = () => {
        expect(onClickMock).toHaveBeenCalledWith(
            bubble,
            expect.objectContaining({ x: expect.any(Number), y: expect.any(Number) })
        )
    }

    const verifyBubbleSize = (size: string) => {
        expect(screen.getByTestId('bubble-b-1')).toHaveStyle({ width: size, height: size })
    }

    const verifyPoppingClassApplied = () => {
        expect(screen.getByTestId('bubble-b-1')).toHaveClass('bubble--popping')
    }

    const triggerAnimationEnd = (animationName: string) => {
        const event = new Event('animationend', { bubbles: true })
        Object.defineProperty(event, 'animationName', { value: animationName })
        fireEvent(screen.getByTestId('bubble-b-1'), event)
    }

    const verifyOnPopEndCalledWithId = (id: string) => {
        expect(onPopEndMock).toHaveBeenCalledWith(id)
    }
})