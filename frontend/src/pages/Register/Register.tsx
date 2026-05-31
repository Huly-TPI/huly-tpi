import { useState } from 'react'
import { register } from '../../api/auth'

export default function Register() {
    const [email, setEmail] = useState('')
    const [password, setPassword] = useState('')
    const [name, setName] = useState('')
    const [lastname, setLastname] = useState('')
    const [birthDate, setBirthDate] = useState('')

    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)

    const today = new Date()

    const maxBirthDate = new Date(
        today.getFullYear() - 13,
        today.getMonth(),
        today.getDate()
    )
        .toISOString()
        .split('T')[0]


    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault()

        setLoading(true)
        setError(null)

        try {
            const res = await register({
                email,
                password,
                name,
                lastname,
                birthDate,
            })

            localStorage.setItem('token', res.accessToken)

            if(res.profileOnBoardingCompleted === false) {
                window.location.href = '/onboarding'
            } else {
                window.location.href = '/'
            }



        } catch (err) {
            setError(err instanceof Error ? err.message : 'Error inesperado')
        } finally {
            setLoading(false)
        }
    }

    return (
        <div style={{ maxWidth: 400, margin: '0 auto' }}>
            <h2>Registro</h2>

            <form onSubmit={handleSubmit}>
                <label htmlFor="email">Email</label>
                <input
                    id="email"
                    type="email"
                    placeholder="Email"
                    required
                    autoComplete="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                />

                <label htmlFor="password">Password</label>
                <input
                    id="password"
                    type="password"
                    placeholder="Password"
                    required
                    autoComplete="new-password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                />

                <label htmlFor="name">Name</label>
                <input
                    id="name"
                    type="text"
                    placeholder="Name"
                    required
                    autoComplete="given-name"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                />

                <label htmlFor="lastname">Lastname</label>
                <input
                    id="lastname"
                    type="text"
                    placeholder="Lastname"
                    required
                    autoComplete="family-name"
                    value={lastname}
                    onChange={(e) => setLastname(e.target.value)}
                />

                <label htmlFor="birthDate">Birth date</label>
                <input
                id="birthDate"
                type="date"
                required
                min="1900-01-01"
                max={maxBirthDate}
                autoComplete="bday"
                value={birthDate}
                onChange={(e) => setBirthDate(e.target.value)}
                />

                <button type="submit" disabled={loading}>
                    {loading ? 'Registrando...' : 'Registrarse'}
                </button>

                {error && <p style={{ color: 'red' }}>{error}</p>}
            </form>
        </div>
    )
}