import { type ReactNode, useEffect } from 'react'
import { useDialogFocusTrap } from '../../hooks/useDialogFocusTrap'

interface BaseModalProps {
  isOpen: boolean
  title: string
  onClose: () => void
  children: ReactNode
  outsideContent?: ReactNode
}

export default function BaseModal({ isOpen, title, onClose, children, outsideContent }: BaseModalProps) {
  const { dialogRef, handleDialogKeyDown } = useDialogFocusTrap({ focusKey: isOpen ? 'open' : 'closed' })

  useEffect(() => {
    if (!isOpen) return

    const originalOverflow = document.body.style.overflow
    document.body.style.overflow = 'hidden'

    return () => {
      document.body.style.overflow = originalOverflow
    }
  }, [isOpen])

  if (!isOpen) return null

  return (
    <div className="fixed inset-0 z-50 flex items-end justify-center bg-[var(--overlay-strong)] p-3 md:items-center md:p-4">
      <button
        type="button"
        aria-label="Cerrar modal"
        className="absolute inset-0 cursor-default"
        onClick={onClose}
      />
      <div className="relative z-10 w-full max-w-2xl">
        {outsideContent}
        <div
          ref={dialogRef}
          role="dialog"
          aria-modal="true"
          aria-label={title}
          tabIndex={-1}
          onKeyDown={event => {
            if (event.key === 'Escape') onClose()
            handleDialogKeyDown(event)
          }}
          className="relative z-10 flex h-[78dvh] w-full flex-col rounded-2xl bg-[var(--surface-primary)] text-[var(--text-primary)] shadow-2xl outline-none"
        >
          {children}
        </div>
      </div>
    </div>
  )
}
