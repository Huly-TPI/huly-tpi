import { describe, it, expect } from 'vitest'
import { required, minLength, maxLength, validEmail, matchesField, noHtml, safeText, validate } from '../../hooks/useForm'
import { minAge } from '../../pages/Register/Register'

const emptyValues = { email: '', password: '' }

describe('required', () => {
    const rule = required()

    it('retorna error con string vacío', () => {
        verifyRuleError(rule, '', 'Campo requerido')
    })

    it('retorna error con solo espacios', () => {
        verifyRuleError(rule, '   ', 'Campo requerido')
    })

    it('pasa con valor válido', () => {
        verifyRulePasses(rule, 'hola')
    })

    it('acepta mensaje personalizado', () => {
        verifyRuleError(required('Obligatorio'), '', 'Obligatorio')
    })
    const verifyRuleError = (r: typeof rule, val: string, expectedMsg: string) => {
        expect(r(val, emptyValues)).toBe(expectedMsg)
    }

    const verifyRulePasses = (r: typeof rule, val: string) => {
        expect(r(val, emptyValues)).toBeUndefined()
    }
})

describe('minLength', () => {
    const rule = minLength(6)

    it('retorna error si es más corto', () => {
        verifyRuleError(rule, 'abc', 'Mínimo 6 caracteres')
    })

    it('pasa con largo exacto', () => {
        verifyRulePasses(rule, 'abcdef')
    })

    it('pasa con largo mayor', () => {
        verifyRulePasses(rule, 'abcdefgh')
    })

    it('acepta mensaje personalizado', () => {
        verifyRuleError(minLength(3, 'Muy corto'), 'ab', 'Muy corto')
    })
    const verifyRuleError = (r: typeof rule, val: string, expectedMsg: string) => {
        expect(r(val, emptyValues)).toBe(expectedMsg)
    }

    const verifyRulePasses = (r: typeof rule, val: string) => {
        expect(r(val, emptyValues)).toBeUndefined()
    }
})

describe('validEmail', () => {
    const rule = validEmail()

    it('retorna error con email inválido', () => {
        verifyRuleError(rule, 'noesmail', 'Email inválido')
    })

    it('retorna error sin dominio', () => {
        verifyRuleError(rule, 'user@', 'Email inválido')
    })

    it('retorna error sin arroba', () => {
        verifyRuleError(rule, 'user.com', 'Email inválido')
    })

    it('pasa con email válido', () => {
        verifyRulePasses(rule, 'user@mail.com')
    })
    const verifyRuleError = (r: typeof rule, val: string, expectedMsg: string) => {
        expect(r(val, emptyValues)).toBe(expectedMsg)
    }

    const verifyRulePasses = (r: typeof rule, val: string) => {
        expect(r(val, emptyValues)).toBeUndefined()
    }
})

describe('matchesField', () => {
    const rule = matchesField('password')

    it('retorna error si no coinciden', () => {
        verifyRuleError(rule, '654321', { password: '123456', confirmPassword: '654321' }, 'Las contraseñas no coinciden')
    })

    it('pasa si coinciden', () => {
        verifyRulePasses(rule, '123456', { password: '123456', confirmPassword: '123456' })
    })
    const verifyRuleError = (r: typeof rule, val: string, values: any, expectedMsg: string) => {
        expect(r(val, values)).toBe(expectedMsg)
    }

    const verifyRulePasses = (r: typeof rule, val: string, values: any) => {
        expect(r(val, values)).toBeUndefined()
    }
})

describe('minAge', () => {
    const rule = minAge(13)

    it('retorna undefined con valor vacío (lo maneja required)', () => {
        verifyRulePasses(rule, '')
    })

    it('retorna error si no cumple la edad mínima', () => {
        verifyRuleError(rule, getYearsAgoDateString(10), 'Debés tener al menos 13 años')
    })

    it('pasa con edad exacta', () => {
        verifyRulePasses(rule, getYearsAgoDateString(13))
    })

    it('pasa con edad mayor', () => {
        verifyRulePasses(rule, getYearsAgoDateString(20))
    })

    it('retorna error si el cumpleaños aún no pasó este año', () => {
        verifyRuleError(rule, getAlmostAgeDateString(13), 'Debés tener al menos 13 años')
    })

    it('acepta mensaje personalizado', () => {
        verifyRuleError(minAge(18, 'Tenés que ser mayor de edad'), getYearsAgoDateString(15), 'Tenés que ser mayor de edad')
    })
    const getYearsAgoDateString = (years: number) => {
        const today = new Date()
        const target = new Date(today.getFullYear() - years, today.getMonth(), today.getDate())
        return target.toISOString().split('T')[0]
    }

    const getAlmostAgeDateString = (years: number) => {
        const today = new Date()
        const target = new Date(today.getFullYear() - years, today.getMonth() + 1, today.getDate())
        return target.toISOString().split('T')[0]
    }

    const verifyRuleError = (r: typeof rule, val: string, expectedMsg: string) => {
        expect(r(val, emptyValues)).toBe(expectedMsg)
    }

    const verifyRulePasses = (r: typeof rule, val: string) => {
        expect(r(val, emptyValues)).toBeUndefined()
    }
})

describe('maxLength', () => {
    const rule = maxLength(10)

    it('retorna error si supera el máximo', () => {
        verifyRuleError(rule, '12345678901', 'Máximo 10 caracteres')
    })

    it('pasa con largo exacto', () => {
        verifyRulePasses(rule, '1234567890')
    })

    it('pasa con largo menor', () => {
        verifyRulePasses(rule, 'abc')
    })

    it('acepta mensaje personalizado', () => {
        verifyRuleError(maxLength(5, 'Muy largo'), '123456', 'Muy largo')
    })
    const verifyRuleError = (r: typeof rule, val: string, expectedMsg: string) => {
        expect(r(val, emptyValues)).toBe(expectedMsg)
    }

    const verifyRulePasses = (r: typeof rule, val: string) => {
        expect(r(val, emptyValues)).toBeUndefined()
    }
})

describe('noHtml', () => {
    it('retorna error con tag HTML', () => {
        verifyNoHtmlError('<script>alert(1)</script>', 'El texto no puede contener HTML')
    })

    it('retorna error con tag de apertura', () => {
        verifyNoHtmlError('<img src=x>', 'El texto no puede contener HTML')
    })

    it('retorna error con javascript:', () => {
        verifyNoHtmlError('javascript:alert(1)', 'El texto no puede contener HTML')
    })

    it('pasa con texto normal', () => {
        verifyNoHtmlPasses('Juan Pérez')
    })

    it('pasa con caracteres especiales válidos', () => {
        verifyNoHtmlPasses("O'Brien")
    })
    const verifyNoHtmlError = (val: string, expectedMsg: string) => {
        expect(noHtml(val, emptyValues)).toBe(expectedMsg)
    }

    const verifyNoHtmlPasses = (val: string) => {
        expect(noHtml(val, emptyValues)).toBeUndefined()
    }
})

describe('safeText', () => {
    it('detecta XSS con script tag', () => {
        verifySafeTextError('<script>alert(1)</script>')
    })

    it('detecta XSS con event handler', () => {
        verifySafeTextError('texto onerror=alert(1)')
    })

    it("detecta SQL injection con ' OR", () => {
        verifySafeTextError("' OR 1=1")
    })

    it('detecta SQL injection con UNION SELECT', () => {
        verifySafeTextError('UNION SELECT * FROM users')
    })

    it('detecta SQL injection con DROP TABLE', () => {
        verifySafeTextError("; DROP TABLE users")
    })

    it('detecta SQL injection con --', () => {
        verifySafeTextError("admin'--")
    })

    it('pasa con texto normal', () => {
        verifySafeTextPasses('Juan Pérez')
    })

    it("pasa con apostrofe aislado como en O'Brien", () => {
        verifySafeTextPasses("O'Brien")
    })

    it('pasa con emojis', () => {
        verifySafeTextPasses('hola 🌱')
    })
    const verifySafeTextError = (val: string) => {
        expect(safeText(val, emptyValues)).toBe('El texto contiene caracteres no permitidos')
    }

    const verifySafeTextPasses = (val: string) => {
        expect(safeText(val, emptyValues)).toBeUndefined()
    }
})

describe('validate', () => {
    it('retorna el primer error encontrado', () => {
        verifyValidationError('', getRequiredAndMinLengthRules(), 'Campo requerido')
    })

    it('retorna el segundo error si el primero pasa', () => {
        verifyValidationError('abc', getRequiredAndMinLengthRules(), 'Mínimo 6 caracteres')
    })

    it('retorna undefined si todas las reglas pasan', () => {
        verifyValidationPasses('abcdef', getRequiredAndMinLengthRules())
    })
    const getRequiredAndMinLengthRules = () => [required(), minLength(6)]

    const verifyValidationError = (val: string, rules: any[], expectedMsg: string) => {
        expect(validate(val, rules, emptyValues)).toBe(expectedMsg)
    }

    const verifyValidationPasses = (val: string, rules: any[]) => {
        expect(validate(val, rules, emptyValues)).toBeUndefined()
    }
})
