import { clearAllMocks, getLoadingSpinner } from '../testHelpers'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import RewardsModal from '../../components/RewardsModal/RewardsModal'
import type { DailyRewardDay, DailyRewardStatus } from '../../api/dailyRewards'

/* ─── Mock ─── */

const mockClaim = vi.fn()

const mockUseDailyRewards = vi.fn<[], {
  status: DailyRewardStatus | null
  loading: boolean
  claiming: boolean
  error: string | null
  claim: typeof mockClaim
}>()

vi.mock('../../hooks/shop/useDailyRewards', () => ({
  useDailyRewards: () => mockUseDailyRewards(),
}))

/* ─── Test Data ─── */

const SEVEN_DAYS: DailyRewardDay[] = [
  { dayNumber: 1, coins: 10 },
  { dayNumber: 2, coins: 15 },
  { dayNumber: 3, coins: 20 },
  { dayNumber: 4, coins: 25 },
  { dayNumber: 5, coins: 30 },
  { dayNumber: 6, coins: 40 },
  { dayNumber: 7, coins: 100 },
]




const defaultProps = {
  isOpen: true,
  onClose: vi.fn(),
  onClaimed: vi.fn(),
}




































































/* ─── Tests ─── */

describe('RewardsModal', () => {
  let onCloseSpy: any
  let onClaimedSpy: any

  beforeEach(() => {
    clearAllMocks()
    setupHookReturn(createHookReturn())
  })

  describe('visibilidad', () => {
    it('no renderiza nada cuando isOpen es false', () => {
      renderModalWithProps({ isOpen: false })
      expectModalNotToBeRendered()
    })

    it('renderiza el modal cuando isOpen es true', () => {
      renderModalWithProps({ isOpen: true })
      expectModalToBeVisible()
    })
  })

  describe('contenido del header', () => {
    it('muestra el título y subtítulo', () => {
      renderDefault()
      expectTitleAndSubtitleToBeVisible()
    })

    it('muestra la racha actual del usuario', () => {
      setupHookReturn(
        createHookReturn({ status: createStatus({ completedDays: 5, currentStreak: 5 }) }),
      )
      renderDefault()
      expectStreakToShow(5)
    })
  })

  describe('cards de recompensas', () => {
    it('muestra las 7 cards con sus días', () => {
      renderDefault()
      expectAllSevenCardsToBeVisible()
    })

    it('muestra la cantidad de monedas en cada card', () => {
      renderDefault()
      expectCardCoinsToBeVisible()
    })
  })

  describe('estado de carga', () => {
    it('muestra el spinner mientras carga', () => {
      setupHookReturn(createHookReturn({ loading: true }))
      renderDefault()
      expectLoadingSpinnerToBeVisible()
    })
  })

  describe('claim de recompensa', () => {
    it('muestra el botón Recolectar cuando se puede reclamar hoy', () => {
      renderDefault()
      expectClaimButtonToBeVisible()
    })

    it('no muestra el botón Recolectar cuando ya se reclamó hoy', () => {
      setupHookReturn(
        createHookReturn({ status: createStatus({ canClaimToday: false }) }),
      )
      renderDefault()
      expectClaimButtonNotToBeVisible()
    })

    it('deshabilita el botón mientras se está reclamando', () => {
      setupHookReturn(createHookReturn({ claiming: true }))
      renderDefault()
      expectClaimingButtonToBeDisabled()
    })

    it('llama a claim al hacer click en Recolectar', () => {
      setupClaimMockResolved({ coins: 20, dayNumber: 3, newStreak: 3 })
      renderWithUser()
      return clickClaim().then(() => {
        expectCallbackCalledOnce(mockClaim)
      })
    })

    it('llama a onClaimed después de un claim exitoso', () => {
      setupOnClaimedSpy()
      setupClaimMockResolved({ coins: 20, dayNumber: 3, newStreak: 3 })
      renderWithUserAndClaimedSpy()
      return clickClaim().then(() => {
        expectCallbackCalledOnce(onClaimedSpy)
      })
    })

    it('muestra el toast después de reclamar', () => {
      setupClaimMockResolved({ coins: 20, dayNumber: 3, newStreak: 3 })
      renderWithUser()
      return clickClaim().then(() => {
        expectToastToBeVisible()
      })
    })

    it('no muestra toast si el claim falla', () => {
      setupClaimMockResolved(null)
      renderWithUser()
      return clickClaim().then(() => {
        expectToastNotToBeVisible()
      })
    })
  })

  describe('mensaje de próxima recompensa', () => {
    it('muestra las monedas disponibles hoy cuando se puede reclamar', () => {
      renderDefault()
      expectNextRewardMessage(20, 'hoy')
    })

    it('muestra las monedas de mañana cuando ya se reclamó', () => {
      setupHookReturn(
        createHookReturn({
          status: createStatus({ canClaimToday: false, completedDays: 3, nextDay: 4 }),
        }),
      )
      renderDefault()
      expectNextRewardMessage(25, 'mañana')
    })
  })

  describe('manejo de errores', () => {
    it('muestra el mensaje de error cuando hay un error', () => {
      const errorMsg = 'No se pudo cargar el calendario de recompensas.'
      setupHookReturn(createHookReturn({ error: errorMsg }))
      renderDefault()
      expectErrorToBeVisible(errorMsg)
    })
  })

  describe('cierre del modal', () => {
    it('llama a onClose al hacer click en el botón cerrar', () => {
      setupOnCloseSpy()
      renderWithUserAndCloseSpy()
      return clickClose().then(() => {
        expectCallbackCalledOnce(onCloseSpy)
      })
    })
  })

  describe('accesibilidad', () => {
    it('tiene el rol dialog con aria-modal', () => {
      renderDefault()
      verifyModalHasAriaModal('true')
    })

    it('tiene aria-label descriptivo', () => {
      renderDefault()
      verifyModalHasAriaLabel('Recompensas diarias')
    })

    it('el botón de cerrar tiene aria-label', () => {
      renderDefault()
      verifyCloseButtonHasAriaLabel('Cerrar recompensas')
    })
  })
  let user: any

  /* helpers */

  const setupHookReturn = (val: any) => {
    mockUseDailyRewards.mockReturnValue(val)
  }

  const renderModalWithProps = (props: Partial<typeof defaultProps>) => {
    renderModal(props)
  }

  const renderDefault = () => {
    renderModal()
  }

  const setupClaimMockResolved = (val: any) => {
    mockClaim.mockResolvedValue(val)
  }

  const renderWithUser = () => {
    user = userEvent.setup()
    renderModal()
  }

  const setupOnClaimedSpy = () => {
    onClaimedSpy = vi.fn()
  }

  const renderWithUserAndClaimedSpy = () => {
    user = userEvent.setup()
    renderModal({ onClaimed: onClaimedSpy })
  }

  const clickClaim = () => {
    return clickClaimButton(user)
  }

  const setupOnCloseSpy = () => {
    onCloseSpy = vi.fn()
  }

  const renderWithUserAndCloseSpy = () => {
    user = userEvent.setup()
    renderModal({ onClose: onCloseSpy })
  }

  const clickClose = () => {
    return clickCloseButton(user)
  }

  const verifyModalHasAriaModal = (val: string) => {
    expect(getModal()).toHaveAttribute('aria-modal', val)
  }

  const verifyModalHasAriaLabel = (val: string) => {
    expect(getModal()).toHaveAttribute('aria-label', val)
  }

  const verifyCloseButtonHasAriaLabel = (val: string) => {
    expect(getCloseButton()).toHaveAttribute('aria-label', val)
  }

  function queryModal() {
    return screen.queryByRole('dialog', { name: 'Recompensas diarias' })
  }

  function getModal() {
    return screen.getByRole('dialog', { name: 'Recompensas diarias' })
  }

  function getTitle() {
    return screen.getByText('¡Cosecha diaria!')
  }

  function getSubtitle() {
    return screen.getByText(/iniciá sesión todos los días/i)
  }

  function getStreakBadge(days: number) {
    const label = `${days} ${days === 1 ? 'día' : 'días'}`
    return screen.getByText(new RegExp(label))
  }

  function getAllCardLabels() {
    return SEVEN_DAYS.map(day =>
      screen.getByText(`Día ${day.dayNumber}`),
    )
  }

  function getCardCoinAmount(coins: number) {
    return screen.getByText(coins.toString())
  }

  function getClaimButton() {
    return screen.getByRole('button', { name: 'Recolectar' })
  }

  function getClaimingButton() {
    return screen.getByRole('button', { name: '...' })
  }

  function queryClaimButton() {
    return screen.queryByRole('button', { name: 'Recolectar' })
  }

  function getCloseButton() {
    return screen.getByRole('button', { name: 'Cerrar recompensas' })
  }

  function findParagraphContaining(coins: number, when: string) {
    return screen.getByText((_content, element) => {
      if (element?.tagName !== 'P') return false
      const text = element.textContent ?? ''
      return text.includes(`${coins} semillas`) && text.includes(when)
    })
  }

  function getErrorMessage(message: string) {
    return screen.getByText(message)
  }

  

  function getToast() {
    return screen.getByRole('status')
  }

  function queryToast() {
    return screen.queryByRole('status')
  }

  async function clickClaimButton(usr: ReturnType<typeof userEvent.setup>) {
    await usr.click(getClaimButton())
  }

  async function clickCloseButton(usr: ReturnType<typeof userEvent.setup>) {
    await usr.click(getCloseButton())
  }

  function expectModalToBeVisible() {
    expect(getModal()).toBeInTheDocument()
  }

  function expectModalNotToBeRendered() {
    expect(queryModal()).not.toBeInTheDocument()
  }

  function expectTitleAndSubtitleToBeVisible() {
    expect(getTitle()).toBeInTheDocument()
    expect(getSubtitle()).toBeInTheDocument()
  }

  function expectStreakToShow(days: number) {
    expect(getStreakBadge(days)).toBeInTheDocument()
  }

  function expectAllSevenCardsToBeVisible() {
    const labels = getAllCardLabels()
    expect(labels).toHaveLength(7)
    labels.forEach(label => expect(label).toBeInTheDocument())
  }

  function expectCardCoinsToBeVisible() {
    for (const day of SEVEN_DAYS) {
      expect(getCardCoinAmount(day.coins)).toBeInTheDocument()
    }
  }

  function expectClaimButtonToBeVisible() {
    expect(getClaimButton()).toBeInTheDocument()
  }

  function expectClaimButtonNotToBeVisible() {
    expect(queryClaimButton()).not.toBeInTheDocument()
  }

  function expectClaimingButtonToBeDisabled() {
    expect(getClaimingButton()).toBeDisabled()
  }

  function expectNextRewardMessage(coins: number, when: 'hoy' | 'mañana') {
    expect(findParagraphContaining(coins, when)).toBeInTheDocument()
  }

  function expectErrorToBeVisible(message: string) {
    expect(getErrorMessage(message)).toBeInTheDocument()
  }

  function expectLoadingSpinnerToBeVisible() {
    expect(getLoadingSpinner()).toBeInTheDocument()
  }

  function expectCallbackCalledOnce(callback: ReturnType<typeof vi.fn>) {
    expect(callback).toHaveBeenCalledOnce()
  }

  function expectToastToBeVisible() {
    expect(getToast()).toBeInTheDocument()
  }

  function expectToastNotToBeVisible() {
    expect(queryToast()).not.toBeInTheDocument()
  }
})

function createStatus(overrides: Partial<DailyRewardStatus> = {}): DailyRewardStatus {
  return {
    days: SEVEN_DAYS,
    currentStreak: 2,
    completedDays: 2,
    canClaimToday: true,
    planBonusActive: false,
    nextDay: 3,
    ...overrides,
  }
}

function createHookReturn(overrides: Partial<ReturnType<typeof mockUseDailyRewards>> = {}) {
  return {
    status: createStatus(),
    loading: false,
    claiming: false,
    error: null,
    claim: mockClaim,
    ...overrides,
  }
}

function renderModal(propOverrides: Partial<typeof defaultProps> = {}) {
  const props = { ...defaultProps, ...propOverrides }
  return render(<RewardsModal {...props} />)
}
