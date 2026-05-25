import { useState, useRef, useEffect } from 'react'
import { api } from '../api/client'
import Navbar from '../components/Navbar'
import dayBackgroundImage from '../assets/garden/light-theme/background/day-background.webp'

interface ChatApiResponse {
  huly_reply: string
  detected_emotion: string | null
  intensity: number | null
  suggested_action: null
  generated_challenge: null
  metadata: {
    risk_detected: boolean
    matched_word: string | null
  } | null
}

interface Message {
  role: 'user' | 'assistant'
  content: string
  emotion?: string | null
  intensity?: number | null
  riskDetected?: boolean
  matchedWord?: string | null
}

const EMOTION_COLORS: Record<string, string> = {
  JOY: 'bg-yellow-100 text-yellow-800',
  SADNESS: 'bg-blue-100 text-blue-800',
  ANGER: 'bg-red-100 text-red-800',
  FEAR: 'bg-purple-100 text-purple-800',
  ANXIETY: 'bg-orange-100 text-orange-800',
  STRESS: 'bg-orange-100 text-orange-800',
  CALM: 'bg-green-100 text-green-800',
  HOPELESSNESS: 'bg-gray-200 text-gray-700',
  LONELINESS: 'bg-indigo-100 text-indigo-800',
  EXHAUSTION: 'bg-slate-100 text-slate-700',
  SHAME: 'bg-pink-100 text-pink-800',
  GUILT: 'bg-rose-100 text-rose-800',
  GRIEF: 'bg-blue-200 text-blue-900',
  NEUTRAL: 'bg-gray-100 text-gray-600',
}

function randomConversationId() {
  return `test-${Math.random().toString(36).slice(2, 10)}`
}

export default function ChatTest() {
  const [messages, setMessages] = useState<Message[]>([])
  const [input, setInput] = useState('')
  const [conversationId, setConversationId] = useState(randomConversationId)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const bottomRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  async function send() {
    const text = input.trim()
    if (!text || loading) return

    const userMsg: Message = { role: 'user', content: text }
    setMessages(prev => [...prev, userMsg])
    setInput('')
    setLoading(true)
    setError(null)

    try {
      const res = await api.post<ChatApiResponse>('/chat', {
        message: text,
        conversationId,
      })

      const assistantMsg: Message = {
        role: 'assistant',
        content: res.huly_reply,
        emotion: res.detected_emotion,
        intensity: res.intensity,
        riskDetected: res.metadata?.risk_detected ?? false,
        matchedWord: res.metadata?.matched_word ?? null,
      }
      setMessages(prev => [...prev, assistantMsg])
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Error desconocido')
    } finally {
      setLoading(false)
    }
  }

  function handleKey(e: React.KeyboardEvent) {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      send()
    }
  }

  return (
    <div className="flex h-dvh flex-col" style={{ background: '#bfe4fb' }}>
      <Navbar />

      {/* Background */}
      <div className="relative min-h-0 flex-1 overflow-hidden">
        <img
          src={dayBackgroundImage}
          alt=""
          aria-hidden
          className="pointer-events-none absolute inset-0 h-full w-full select-none object-cover"
          style={{ objectPosition: 'center 36%' }}
        />

        {/* Chat overlay */}
        <div className="relative z-10 flex h-full flex-col items-center overflow-y-auto px-4 py-8">

          {/* Header */}
          <div className="w-full max-w-2xl mb-4">
            <div className="flex items-center gap-2 mb-1">
              <span className="text-xs font-bold uppercase tracking-widest text-violeta bg-violeta-claro px-2 py-0.5 rounded-full">
                Test
              </span>
              <h1 className="text-xl font-bold text-gray-800">Chat — Huly AI</h1>
            </div>

            {/* ConversationId editable */}
            <div className="flex items-center gap-2 text-xs text-gray-500">
              <span>conversationId:</span>
              <input
                className="border border-gray-200 rounded px-2 py-0.5 text-xs text-gray-600 w-48 focus:outline-none focus:border-violeta bg-white/80"
                value={conversationId}
                onChange={e => setConversationId(e.target.value)}
              />
              <button
                onClick={() => { setConversationId(randomConversationId()); setMessages([]) }}
                className="text-violeta hover:underline"
              >
                nueva sesión
              </button>
            </div>
          </div>

          {/* Messages */}
          <div className="w-full max-w-2xl flex-1 bg-white/80 backdrop-blur-sm rounded-2xl shadow-sm border border-white/60 p-4 flex flex-col gap-3 min-h-96 max-h-[60vh] overflow-y-auto">
            {messages.length === 0 && (
              <p className="text-center text-gray-400 text-sm mt-auto mb-auto">
                Escribí un mensaje para empezar
              </p>
            )}

            {messages.map((msg, i) => (
              <div key={i} className={`flex flex-col ${msg.role === 'user' ? 'items-end' : 'items-start'}`}>

                {/* Bubble */}
                <div className={`max-w-[80%] rounded-2xl px-4 py-2.5 text-sm leading-relaxed ${
                  msg.role === 'user'
                    ? 'bg-violeta text-white rounded-br-sm'
                    : 'bg-white text-gray-800 rounded-bl-sm shadow-sm'
                }`}>
                  {msg.content}
                </div>

                {/* Metadata badges (solo assistant) */}
                {msg.role === 'assistant' && (
                  <div className="flex flex-wrap gap-1.5 mt-1.5 ml-1">
                    {msg.emotion && (
                      <span className={`text-xs font-semibold px-2 py-0.5 rounded-full ${EMOTION_COLORS[msg.emotion] ?? 'bg-gray-100 text-gray-600'}`}>
                        {msg.emotion}
                      </span>
                    )}
                    {msg.intensity != null && (
                      <span className="text-xs px-2 py-0.5 rounded-full bg-white/80 text-gray-500 shadow-sm">
                        intensidad {msg.intensity}/10
                      </span>
                    )}
                    {msg.riskDetected && (
                      <span className="text-xs font-bold px-2 py-0.5 rounded-full bg-red-100 text-red-700">
                        ⚠ riesgo{msg.matchedWord ? `: "${msg.matchedWord}"` : ''}
                      </span>
                    )}
                  </div>
                )}
              </div>
            ))}

            {loading && (
              <div className="flex items-start">
                <div className="bg-white rounded-2xl rounded-bl-sm px-4 py-2.5 text-sm text-gray-400 shadow-sm">
                  Huly está escribiendo...
                </div>
              </div>
            )}

            {error && (
              <div className="text-xs text-red-500 text-center">{error}</div>
            )}

            <div ref={bottomRef} />
          </div>

          {/* Input */}
          <div className="w-full max-w-2xl mt-3 flex gap-2">
            <textarea
              className="flex-1 border border-white/60 bg-white/80 backdrop-blur-sm rounded-xl px-4 py-2.5 text-sm resize-none focus:outline-none focus:border-violeta focus:ring-1 focus:ring-violeta"
              rows={2}
              placeholder="Escribí tu mensaje..."
              value={input}
              onChange={e => setInput(e.target.value)}
              onKeyDown={handleKey}
              disabled={loading}
            />
            <button
              onClick={send}
              disabled={loading || !input.trim()}
              className="bg-violeta text-white rounded-xl px-5 font-semibold text-sm disabled:opacity-40 hover:bg-opacity-90 transition"
            >
              Enviar
            </button>
          </div>

          <p className="mt-3 text-xs text-gray-500/80">Enter para enviar · Shift+Enter para nueva línea</p>
        </div>
      </div>
    </div>
  )
}
