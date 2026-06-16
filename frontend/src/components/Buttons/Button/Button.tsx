import { useState, type ButtonHTMLAttributes, type MouseEvent, type PropsWithChildren } from 'react'

type ButtonVariant =
  | 'primary'
  | 'secondary'
  | 'tertiary'
  | 'alert'
  | 'success'
  | 'successSecondary'
type ButtonSize = 'sm' | 'md' | 'lg'

type ButtonClickHandler = (event: MouseEvent<HTMLButtonElement>) => void | Promise<void>
type ButtonAsyncErrorHandler = (error: unknown) => void

interface ButtonProps extends Omit<ButtonHTMLAttributes<HTMLButtonElement>, 'onClick'> {
  fullWidth?: boolean
  isLoading?: boolean
  loadingLabel?: string
  onAsyncError?: ButtonAsyncErrorHandler
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
  onAsyncError,
  onClick,
  size = 'md',
  type = 'button',
  variant = 'primary',
  ...props
}: PropsWithChildren<ButtonProps>) {
  const [isSubmitting, setIsSubmitting] = useState(false)
  const isBusy = isLoading || isSubmitting

  const variantClasses: Record<ButtonVariant, string> = {
    primary:
      'border-transparent bg-violeta text-white hover:shadow-[inset_0_0_0_9999px_rgba(0,0,0,0.12)] focus-visible:outline-[#8869ac59]',
    secondary:
      'border-violeta bg-transparent text-violeta hover:shadow-[inset_0_0_0_9999px_rgba(0,0,0,0.08)] focus-visible:outline-[#8869ac59]',
    success:
      'border-transparent bg-bosque text-white hover:shadow-[inset_0_0_0_9999px_rgba(0,0,0,0.12)] focus-visible:outline-[rgba(76,124,100,0.35)]',
    successSecondary:
      'border-bosque bg-transparent text-bosque hover:shadow-[inset_0_0_0_9999px_rgba(0,0,0,0.08)] focus-visible:outline-[rgba(76,124,100,0.35)]',
    tertiary:
      'min-w-0 border-transparent bg-transparent px-2 py-1 text-bosque font-medium hover:font-semibold transition-[font-weight] duration-300 focus-visible:outline-[rgba(76,124,100,0.35)]',
    alert:
      'border-anaranjado bg-anaranjado text-white hover:shadow-[inset_0_0_0_9999px_rgba(0,0,0,0.12)] focus-visible:outline-[rgba(156,83,18,0.35)]',
  }

  const sizeClasses: Record<ButtonSize, string> = {
    sm: 'min-w-[9rem] px-[1.1rem] py-[0.72rem] text-[0.86rem] max-md:min-w-full',
    md: 'min-w-[11.5rem] px-[1.35rem] py-[0.85rem] text-[0.95rem] max-md:min-w-full',
    lg: 'min-w-[13.5rem] px-[1.65rem] py-[1rem] text-[1.05rem] max-md:min-w-full',
  }

  const baseClasses =
    'inline-flex items-center justify-center rounded-full border font-medium leading-[1.2] transition-[background-color,border-color,color,opacity,box-shadow] duration-300 ease-in-out cursor-pointer focus-visible:outline focus-visible:outline-[3px] focus-visible:outline-offset-3 disabled:cursor-not-allowed disabled:opacity-70 disabled:transform-none max-md:w-full max-md:text-base'

  const classes = [
    baseClasses,
    sizeClasses[size],
    variantClasses[variant],
    fullWidth ? 'w-full' : '',
    isBusy ? 'pointer-events-none' : '',
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
    } catch (error) {
      if (onAsyncError) {
        onAsyncError(error)
      } else {
        console.error('Button onClick failed', error)
      }
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
      <span className="inline-flex items-center justify-center gap-[0.4rem]">
        {isBusy ? loadingLabel : children}
      </span>
    </button>
  )
}
