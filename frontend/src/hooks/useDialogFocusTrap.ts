import { useEffect, useRef, type KeyboardEvent, type RefObject } from 'react'

const FOCUSABLE_SELECTOR =
  'button:not([disabled]), [href], input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])'

function getFocusableElements(container: HTMLElement | null) {
  if (!container) return []

  return Array.from(container.querySelectorAll<HTMLElement>(FOCUSABLE_SELECTOR)).filter(
    element => !element.hasAttribute('disabled') && element.tabIndex !== -1,
  )
}

interface UseDialogFocusTrapParams {
  focusKey: string | number
}

interface UseDialogFocusTrapReturn {
  dialogRef: RefObject<HTMLDivElement>
  handleDialogKeyDown: (event: KeyboardEvent<HTMLDivElement>) => void
}

export function useDialogFocusTrap({
  focusKey,
}: UseDialogFocusTrapParams): UseDialogFocusTrapReturn {
  const dialogRef = useRef<HTMLDivElement>(null)
  const previousActiveElementRef = useRef<HTMLElement | null>(null)

  useEffect(() => {
    previousActiveElementRef.current = document.activeElement as HTMLElement | null

    return () => {
      previousActiveElementRef.current?.focus()
    }
  }, [])

  useEffect(() => {
    const dialog = dialogRef.current
    dialog?.focus()
  }, [focusKey])

  const handleDialogKeyDown = (event: KeyboardEvent<HTMLDivElement>) => {
    if (event.key !== 'Tab') return

    const focusableElements = getFocusableElements(dialogRef.current)
    if (focusableElements.length === 0) {
      event.preventDefault()
      dialogRef.current?.focus()
      return
    }

    const firstFocusableElement = focusableElements[0]
    const lastFocusableElement = focusableElements[focusableElements.length - 1]
    const activeElement = document.activeElement

    if (event.shiftKey && activeElement === firstFocusableElement) {
      event.preventDefault()
      lastFocusableElement.focus()
      return
    }

    if (!event.shiftKey && activeElement === lastFocusableElement) {
      event.preventDefault()
      firstFocusableElement.focus()
    }
  }

  return {
    dialogRef,
    handleDialogKeyDown,
  }
}
