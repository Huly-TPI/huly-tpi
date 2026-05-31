import { useState } from 'react'
import { EyeIcon, EyeSlashIcon } from '@heroicons/react/24/outline'
import { getInputClassName } from './authInputStyles'

interface PasswordInputProps {
    id: string
    placeholder: string
    value: string
    onChange: (value: string) => void
    hasError: boolean
    errorId?: string
    autoFocus?: boolean
}

export default function PasswordInput({
    id,
    placeholder,
    value,
    onChange,
    hasError,
    errorId,
    autoFocus = false,
}: PasswordInputProps) {
    const [visible, setVisible] = useState(false)

    return (
        <div className="relative">
            <input
                id={id}
                type={visible ? 'text' : 'password'}
                placeholder={placeholder}
                value={value}
                onChange={(e) => onChange(e.target.value)}
                autoFocus={autoFocus}
                aria-invalid={hasError}
                aria-describedby={errorId}
                className={getInputClassName(
                    hasError,
                    'pr-12 [&::-ms-reveal]:hidden [&::-ms-clear]:hidden',
                )}
            />
            <button
                type="button"
                onClick={() => setVisible((prev) => !prev)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-[#9c8b74] hover:text-[#6b5d4a] transition-colors"
                aria-label={visible ? 'Ocultar contraseña' : 'Mostrar contraseña'}
            >
                {visible
                    ? <EyeSlashIcon className="size-5" />
                    : <EyeIcon className="size-5" />
                }
            </button>
        </div>
    )
}