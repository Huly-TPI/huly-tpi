import { useState, useEffect, useCallback, useRef } from 'react'
import { chatbotApi, RiskWordResponse, RiskSeverity, BotConfigResponse } from '../../api/chatbot'
import { Toast } from '../../components/backoffice/Toast'
import { EmotionalCategoriesSection } from '../../components/backoffice/EmotionalCategoriesSection'
import { FlowBuilderSection } from '../../components/backoffice/FlowBuilderSection'
import { BotConfigSection } from '../../components/backoffice/BotConfigSection'
import { RiskDetectionSection } from '../../components/backoffice/RiskDetectionSection'
import { ActivitiesSection } from '../../components/backoffice/ActivitiesSection'
import { WellbeingSection } from '../../components/backoffice/WellbeingSection'
import { TrainingLogsSection } from '../../components/backoffice/TrainingLogsSection'

const MOBILE_PAGE_SIZE = 20

export default function ChatbotPage() {
  const [riskWords, setRiskWords] = useState<RiskWordResponse[]>([])
  const [riskLoading, setRiskLoading] = useState(true)
  const [riskPage, setRiskPage] = useState(0)
  const [riskTotalPages, setRiskTotalPages] = useState(0)
  const [riskTotalElements, setRiskTotalElements] = useState(0)
  const riskPageSizeRef = useRef(MOBILE_PAGE_SIZE)

  const [botConfig, setBotConfig] = useState<BotConfigResponse | null>(null)
  const [configLoading, setConfigLoading] = useState(true)

  const [toastError, setToastError] = useState<string | null>(null)

  const loadRiskWords = useCallback(async (page = 0) => {
    setRiskLoading(true)
    try {
      const res = await chatbotApi.listRiskWords({ page, size: riskPageSizeRef.current })
      setRiskWords(res.content)
      setRiskPage(res.page_number)
      setRiskTotalPages(res.total_pages)
      setRiskTotalElements(res.total_elements)
    } catch {
      setToastError('No se pudieron cargar las palabras de riesgo')
    } finally {
      setRiskLoading(false)
    }
  }, [])

  const loadBotConfig = useCallback(async () => {
    setConfigLoading(true)
    try {
      const res = await chatbotApi.getBotConfig()
      setBotConfig(res)
    } catch {
      // config may not exist yet
    } finally {
      setConfigLoading(false)
    }
  }, [])

  useEffect(() => {
    loadRiskWords()
    loadBotConfig()
  }, [loadRiskWords, loadBotConfig])

  async function handleAddRiskWord(word: string, severity: RiskSeverity) {
    try {
      await chatbotApi.createRiskWord({ word, severity })
      await loadRiskWords(0)
    } catch (e) {
      const raw = e instanceof Error ? e.message : ''
      setToastError(raw.includes('409') ? 'Esta palabra ya existe en la lista' : 'No se pudo guardar la palabra')
    }
  }

  async function handleDeleteRiskWord(id: number) {
    setRiskLoading(true)
    try {
      await chatbotApi.deleteRiskWord(id)
      const nextPage = riskWords.length === 1 && riskPage > 0 ? riskPage - 1 : riskPage
      await loadRiskWords(nextPage)
    } catch {
      setToastError('No se pudo eliminar la palabra')
      setRiskLoading(false)
    }
  }

  const handleRiskPageSizeChange = useCallback((size: number) => {
    if (size !== riskPageSizeRef.current) {
      riskPageSizeRef.current = size
      loadRiskWords(0)
    }
  }, [loadRiskWords])

  async function handleSaveBotConfig(riskDetectionEnabled: boolean, systemPrompt: string) {
    try {
      const res = await chatbotApi.updateBotConfig({ riskDetectionEnabled, systemPrompt })
      setBotConfig(res)
    } catch {
      setToastError('No se pudo guardar la configuración')
    }
  }

  return (
    <div className="flex flex-col gap-4">
      <div className="flex flex-col gap-1">
        <h1 className="text-[28px] font-extrabold leading-tight text-[#8869AC]">Chatbot</h1>
        <p className="text-sm text-[#A0AEC0]">
          Configuración emocional, automatización terapéutica y monitoreo inteligente
        </p>
      </div>

      <EmotionalCategoriesSection />

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-[3fr_1fr]">
        <div className="flex flex-col gap-4">
          <FlowBuilderSection />
          <BotConfigSection config={botConfig} loading={configLoading} onSave={handleSaveBotConfig} />
        </div>
        <RiskDetectionSection
          words={riskWords}
          loading={riskLoading}
          page={riskPage}
          totalPages={riskTotalPages}
          totalElements={riskTotalElements}
          onPrev={() => loadRiskWords(riskPage - 1)}
          onNext={() => loadRiskWords(riskPage + 1)}
          onAdd={handleAddRiskWord}
          onDelete={handleDeleteRiskWord}
          onPageSizeChange={handleRiskPageSizeChange}
        />
      </div>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <ActivitiesSection />
        <WellbeingSection />
      </div>

      <TrainingLogsSection />

      {toastError && <Toast message={toastError} onClose={() => setToastError(null)} />}
    </div>
  )
}
