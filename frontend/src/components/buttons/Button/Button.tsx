import { useState, type ButtonHTMLAttributes, type MouseEvent, type PropsWithChildren } from 'react'
import './Button.css'

type ButtonVariant = 'brand' | 'neutral' | 'ghost'
type ButtonSize = 'sm' | 'md' | 'lg'

type ButtonClickHandler = (event: MouseEvent<HTMLButtonElement>) => void | Promise<void>

interface ButtonProps extends Omit<ButtonHTMLAttributes<HTMLButtonElement>, 'onClick'> {
  fullWidth?: boolean
  isLoading?: boolean
  loadingLabel?: string
  size?: ButtonSize
  variant?: ButtonVariant
  onClick?: ButtonClickHandler
}

function isPromiseLike(value: void | Promise<void>): value is Promise<void> {
  return typeof value === 'object' && value !== null && 'then' in value
}

export default function Button({
  children,
  className,
  disabled = false,
  fullWidth = false,
  isLoading = false,
  loadingLabel = 'Cargando...',
  onClick,
  size = 'md',
  type = 'button',
  variant = 'brand',
  ...props
}: PropsWithChildren<ButtonProps>) {
  const [isSubmitting, setIsSubmitting] = useState(false)
  const isBusy = isLoading || isSubmitting

  const classes = [
    'button',
    `button--${variant}`,
    `button--${size}`,
    fullWidth ? 'button--full-width' : '',
    isBusy ? 'button--busy' : '',
    className ?? '',
  ]
    .filter(Boolean)
    .join(' ')

  const handleClick = async (event: MouseEvent<HTMLButtonElement>) => {
    if (!onClick || disabled || isBusy) return

    const result = onClick(event)
    if (!isPromiseLike(result)) return

    try {
      setIsSubmitting(true)
      await result
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <button
      type={type}
      className={classes}
      disabled={disabled || isBusy}
      aria-busy={isBusy}
      onClick={handleClick}
      {...props}
    >
      <span className="button__label">{isBusy ? loadingLabel : children}</span>
    </button>
  )
}
