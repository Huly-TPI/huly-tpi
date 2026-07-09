import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import Pending from '../../pages/Pending/Pending'

vi.mock('../../context/toast', () => ({
    useToast: vi.fn(() => ({
        showToast: vi.fn(),
    }))
}))

vi.mock('../../context/theme', () => ({
    useTheme: vi.fn(() => ({
        theme: 'garden',
        setTheme: vi.fn(),
    }))
}))

vi.mock('../../context/authGate', () => ({
    useAuthGate: vi.fn(() => ({
        requireAuth: (cb: () => void) => cb(),
        user: { id: 1, email: 'test@example.com' },
    }))
}))

vi.mock('../../hooks/usePendingTasks', () => ({
    usePendingTasks: vi.fn(() => ({
        tasks: [],
        unplacedTasks: [],
        placedTasks: [],
        createTask: vi.fn(),
        updateTask: vi.fn(),
        deleteTask: vi.fn(),
        completeTask: vi.fn(),
        placeTask: vi.fn(),
    }))
}))

vi.mock('../../hooks/usePendingRecommendation', () => ({
    usePendingRecommendation: vi.fn(() => ({
        recommendation: null,
        hasUnrespondedRecommendation: false,
        recommendedTaskIds: new Set<number>(),
        accept: vi.fn(),
        reject: vi.fn(),
        requestOnDemand: vi.fn(),
    }))
}))

vi.mock('../../hooks/usePostitDrag', () => ({
    usePostitDrag: vi.fn(() => ({
        dragState: { mode: 'idle' },
        pickUp: vi.fn(),
    }))
}))

vi.mock('../../hooks/useActivitySessionTracker', () => ({
    useActivitySessionTracker: vi.fn(() => ({
        markConditionMet: vi.fn(),
        saveSession: vi.fn(),
    }))
}))

describe('Pending Page', () => {
    it('renderiza la bandeja de entrada y el tablero de pendientes', () => {
        renderPending()
        expect(screen.getByText('+ Nueva tarea')).toBeInTheDocument()
        expect(screen.getByLabelText('Tablero de pendientes')).toBeInTheDocument()
    })

    const renderPending = () => {
        render(
            <MemoryRouter>
                <Pending />
            </MemoryRouter>
        )
    }
})
