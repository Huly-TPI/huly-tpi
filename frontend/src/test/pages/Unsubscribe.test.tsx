import { describe, it } from 'vitest'
import { render } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import Unsubscribe from '../../pages/Unsubscribe/Unsubscribe'
import { verifyTextPresent } from '../testHelpers'

describe('Unsubscribe', () => {
    it('muestra la confirmación cuando status es ok', () => {
        renderWithStatus('ok')
        verifySuccessMessageVisible()
    })

    it('muestra el error cuando status es error', () => {
        renderWithStatus('error')
        verifyErrorMessageVisible()
    })
    const renderWithStatus = (status: string) => {
        render(
            <MemoryRouter initialEntries={[`/unsubscribe?status=${status}`]}>
                <Unsubscribe />
            </MemoryRouter>
        )
    }

    const verifySuccessMessageVisible = () => {
        verifyTextPresent('Listo, te diste de baja')
    }

    const verifyErrorMessageVisible = () => {
        verifyTextPresent('No pudimos procesar tu baja')
    }
})