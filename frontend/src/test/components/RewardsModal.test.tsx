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

/* ─── Setup Helpers ─── */

const defaultProps = {
  isOpen: true,
  onClose: vi.fn(),
  onClaimed: vi.fn(),
}

function renderModal(propOverrides: Partial<typeof defaultProps> = {}) {
  const props = { ...defaultProps, ...propOverrides }
  return render(<RewardsModal {...props} />)
}

function renderModalWithUser(propOverrides: Partial<typeof defaultProps> = {}) {
  const user = userEvent.setup()
  renderModal(propOverrides)
  return user
}

/* ─── Query Helpers ─── */

function queryModal() {
  return screen.queryByRole('dialog', { name: /recompensas diarias/i })
}

function getModal() {
  return screen.getByRole('dialog', { name: /recompensas diarias/i })
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
  return screen.getByRole('button', { name: /recolectar/i })
}

function getClaimingButton() {
  return screen.getByRole('button', { name: '...' })
}

function queryClaimButton() {
  return screen.queryByRole('button', { name: /recolectar/i })
}

function getCloseButton() {
  return screen.getByRole('button', { name: /cerrar recompensas/i })
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

function getLoadingSpinner() {
  return document.querySelector('.animate-spin')
}

function getToast() {
  return screen.getByRole('status')
}

function queryToast() {
  return screen.queryByRole('status')
}

/* ─── Interaction Helpers ─── */

async function clickClaimButton(user: ReturnType<typeof userEvent.setup>) {
  await user.click(getClaimButton())
}

async function clickCloseButton(user: ReturnType<typeof userEvent.setup>) {
  await user.click(getCloseButton())
}

/* ─── Assertion Helpers ─── */

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

/* ─── Tests ─── */

describe('RewardsModal', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseDailyRewards.mockReturnValue(createHookReturn())
  })

  describe('visibilidad', () => {
    it('no renderiza nada cuando isOpen es false', () => {
      renderModal({ isOpen: false })
      expectModalNotToBeRendered()
    })

    it('renderiza el modal cuando isOpen es true', () => {
      renderModal()
      expectModalToBeVisible()
    })
  })

  describe('contenido del header', () => {
    it('muestra el título y subtítulo', () => {
      renderModal()
      expectTitleAndSubtitleToBeVisible()
    })

    it('muestra la racha actual del usuario', () => {
      mockUseDailyRewards.mockReturnValue(
        createHookReturn({ status: createStatus({ completedDays: 5, currentStreak: 5 }) }),
      )
      renderModal()
      expectStreakToShow(5)
    })
  })

  describe('cards de recompensas', () => {
    it('muestra las 7 cards con sus días', () => {
      renderModal()
      expectAllSevenCardsToBeVisible()
    })

    it('muestra la cantidad de monedas en cada card', () => {
      renderModal()
      expectCardCoinsToBeVisible()
    })
  })

  describe('estado de carga', () => {
    it('muestra el spinner mientras carga', () => {
      mockUseDailyRewards.mockReturnValue(createHookReturn({ loading: true }))
      renderModal()
      expectLoadingSpinnerToBeVisible()
    })
  })

  describe('claim de recompensa', () => {
    it('muestra el botón Recolectar cuando se puede reclamar hoy', () => {
      renderModal()
      expectClaimButtonToBeVisible()
    })

    it('no muestra el botón Recolectar cuando ya se reclamó hoy', () => {
      mockUseDailyRewards.mockReturnValue(
        createHookReturn({ status: createStatus({ canClaimToday: false }) }),
      )
      renderModal()
      expectClaimButtonNotToBeVisible()
    })

    it('deshabilita el botón mientras se está reclamando', () => {
      mockUseDailyRewards.mockReturnValue(createHookReturn({ claiming: true }))
      renderModal()
      expectClaimingButtonToBeDisabled()
    })

    it('llama a claim al hacer click en Recolectar', async () => {
      mockClaim.mockResolvedValue({ coins: 20, dayNumber: 3, newStreak: 3 })
      const user = renderModalWithUser()

      await clickClaimButton(user)

      expectCallbackCalledOnce(mockClaim)
    })

    it('llama a onClaimed después de un claim exitoso', async () => {
      const onClaimed = vi.fn()
      mockClaim.mockResolvedValue({ coins: 20, dayNumber: 3, newStreak: 3 })
      const user = renderModalWithUser({ onClaimed })

      await clickClaimButton(user)

      expectCallbackCalledOnce(onClaimed)
    })

    it('muestra el toast después de reclamar', async () => {
      mockClaim.mockResolvedValue({ coins: 20, dayNumber: 3, newStreak: 3 })
      const user = renderModalWithUser()

      await clickClaimButton(user)

      expectToastToBeVisible()
    })

    it('no muestra toast si el claim falla', async () => {
      mockClaim.mockResolvedValue(null)
      const user = renderModalWithUser()

      await clickClaimButton(user)

      expectToastNotToBeVisible()
    })
  })

  describe('mensaje de próxima recompensa', () => {
    it('muestra las monedas disponibles hoy cuando se puede reclamar', () => {
      renderModal()
      expectNextRewardMessage(20, 'hoy')
    })

    it('muestra las monedas de mañana cuando ya se reclamó', () => {
      mockUseDailyRewards.mockReturnValue(
        createHookReturn({
          status: createStatus({ canClaimToday: false, completedDays: 3, nextDay: 4 }),
        }),
      )
      renderModal()
      expectNextRewardMessage(25, 'mañana')
    })
  })

  describe('manejo de errores', () => {
    it('muestra el mensaje de error cuando hay un error', () => {
      const errorMsg = 'No se pudo cargar el calendario de recompensas.'
      mockUseDailyRewards.mockReturnValue(createHookReturn({ error: errorMsg }))
      renderModal()
      expectErrorToBeVisible(errorMsg)
    })
  })

  describe('cierre del modal', () => {
    it('llama a onClose al hacer click en el botón cerrar', async () => {
      const onClose = vi.fn()
      const user = renderModalWithUser({ onClose })

      await clickCloseButton(user)

      expectCallbackCalledOnce(onClose)
    })
  })

  describe('accesibilidad', () => {
    it('tiene el rol dialog con aria-modal', () => {
      renderModal()
      const modal = getModal()
      expect(modal).toHaveAttribute('aria-modal', 'true')
    })

    it('tiene aria-label descriptivo', () => {
      renderModal()
      expect(getModal()).toHaveAttribute('aria-label', 'Recompensas diarias')
    })

    it('el botón de cerrar tiene aria-label', () => {
      renderModal()
      expect(getCloseButton()).toHaveAttribute('aria-label', 'Cerrar recompensas')
    })
  })
})