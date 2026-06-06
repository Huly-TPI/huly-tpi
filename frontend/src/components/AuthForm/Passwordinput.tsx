import { useState } from 'react'
import { EyeIcon, EyeSlashIcon } from '@heroicons/react/24/outline'
import { CheckCircleIcon, XCircleIcon } from '@heroicons/react/24/solid'
import { getInputClassName } from './authInputStyles'

const PASSWORD_MIN = 6
const PASSWORD_MAX = 72

interface PasswordRule {
    label: string
    passes: (value: string) => boolean
}

const PASSWORD_RULES: PasswordRule[] = [
    { label: `Al menos ${PASSWORD_MIN} caracteres`, passes: (v) => v.length >= PASSWORD_MIN },
    { label: `Máximo ${PASSWORD_MAX} caracteres`, passes: (v) => v.length <= PASSWORD_MAX },
]

interface PasswordInputProps {
    id: string
    placeholder: string
    value: string
    onChange: (value: string) => void
    hasError: boolean
    errorId?: string
    autoFocus?: boolean
    showChecklist?: boolean
}

export default function PasswordInput({
    id,
    placeholder,
    value,
    onChange,
    hasError,
    errorId,
    autoFocus = false,
    showChecklist = false,
}: PasswordInputProps) {
    const [visible, setVisible] = useState(false)

    const showRules = showChecklist && value.length > 0

    return (
        <div className="flex flex-col gap-1">
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

            {showRules && (
                <ul className="flex flex-col gap-0.5 px-1" aria-live="polite">
                    {PASSWORD_RULES.map((rule) => {
                        const ok = rule.passes(value)
                        return (
                            <li key={rule.label} className={`flex items-center gap-1.5 text-xs transition-colors ${ok ? 'text-[#4C7C64]' : 'text-red-500'}`}>
                                {ok
                                    ? <CheckCircleIcon className="size-3.5 shrink-0" />
                                    : <XCircleIcon className="size-3.5 shrink-0" />
                                }
                                {rule.label}
                            </li>
                        )
                    })}
                </ul>
            )}
        </div>
    )
}