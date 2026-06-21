import { useSearchParams, Link } from 'react-router-dom'
import './Unsubscribe.css'

export default function Unsubscribe() {
    const [searchParams] = useSearchParams()
    const ok = searchParams.get('status') === 'ok'

     return (
        <main className="unsubscribe-page">
            <div className="unsubscribe-card">
                <span className="unsubscribe-icon" aria-hidden="true">🌿</span>
                {ok ? (
                    <>
                        <h1 className="unsubscribe-title">Listo, te diste de baja</h1>
                        <p className="unsubscribe-text">
                            No te vamos a mandar más correos de recordatorio. Si cambiás de idea,
                            podés volver a activarlos desde tu perfil cuando quieras.
                        </p>
                    </>
                ) : (
                    <>
                        <h1 className="unsubscribe-title">No pudimos procesar tu baja</h1>
                        <p className="unsubscribe-text">
                            El enlace no es válido o ya venció. Probá de nuevo desde el último
                            correo que te enviamos.
                        </p>
                    </>
                )}
                <Link to="/" className="unsubscribe-btn">Volver a Huly</Link>
            </div>
        </main>
    )
}