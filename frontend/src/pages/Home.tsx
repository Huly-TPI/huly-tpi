import { useState } from 'react'
import { mathApi } from '../api/math'
import { useGreeting } from '../hooks/useGreeting'
import PageHeader from '../components/PageHeader'

interface CalcState {
  a: number
  b: number
  result: number | null
  status: 'idle' | 'loading' | 'ok' | 'error'
  error: string | null
}

export default function Home() {
  const greeting = useGreeting()
  const [calc, setCalc] = useState<CalcState>({
    a: 0, b: 0, result: null, status: 'idle', error: null,
  })

  const handleSum = async () => {
    const a = Math.floor(Math.random() * 100)
    const b = Math.floor(Math.random() * 100)
    setCalc({ a, b, result: null, status: 'loading', error: null })
    try {
      const data = await mathApi.sum(a, b)
      setCalc(prev => ({ ...prev, result: data.result, status: 'ok' }))
    } catch (e) {
      setCalc(prev => ({
        ...prev,
        status: 'error',
        error: e instanceof Error ? e.message : 'Error desconocido',
      }))
    }
  }

  return (
    <div className="max-w-4xl mx-auto p-8">
      <PageHeader title={`${greeting}, bienvenido a Huly`} subtitle="Tu jardín de bienestar emocional" />
      <hr className="my-6" />
      <h2 className="text-xl font-semibold mb-2">Prueba de conexión</h2>
      <p className="text-gray-600 mb-4">
        Genera dos números random, los envía al backend y muestra la suma.
      </p>
      <button onClick={handleSum} disabled={calc.status === 'loading'} className="huly-button">
        {calc.status === 'loading' ? 'Calculando...' : 'Sumar números random'}
      </button>
      {calc.status !== 'idle' && (
        <div className="mt-4">
          <p>
            {calc.a} + {calc.b} ={' '}
            {calc.status === 'ok' && <strong>{calc.result}</strong>}
            {calc.status === 'loading' && '...'}
          </p>
          {calc.status === 'error' && <p className="text-red-500">Error: {calc.error}</p>}
        </div>
      )}
    </div>
  )
}