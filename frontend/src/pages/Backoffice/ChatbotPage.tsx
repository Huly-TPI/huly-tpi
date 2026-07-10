import { useRiskWords } from '../../hooks/backoffice/useRiskWords'
import { useBotConfig } from '../../hooks/backoffice/useBotConfig'
import { Toast } from '../../components/backoffice/Toast'
import { EmotionalCategoriesSection } from '../../components/backoffice/EmotionalCategoriesSection'
import { BotConfigSection } from '../../components/backoffice/BotConfigSection'
import { RiskDetectionSection } from '../../components/backoffice/RiskDetectionSection'
import { WellbeingSection } from '../../components/backoffice/WellbeingSection'
import PageHeader from '../../components/backoffice/PageHeader'

export default function ChatbotPage() {
  const risk = useRiskWords()
  const botConfig = useBotConfig()
  const toastError = risk.error ?? botConfig.error
  const clearToast = risk.error ? risk.clearError : botConfig.clearError

  return (
    <div className="flex flex-col gap-6 animate-fadeIn">
      <PageHeader 
        title="Chatbot" 
        subtitle="Configuración emocional y monitoreo inteligente"
      />

      {/* Fila 1: Configuración ' Emociones */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <BotConfigSection
          config={botConfig.config}
          loading={botConfig.loading}
          onSave={botConfig.handleSave}
        />
        <EmotionalCategoriesSection />
      </div>

      {/* Fila 2: Evolución ' Detección de riesgo */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <WellbeingSection />
        <RiskDetectionSection
          words={risk.words}
          loading={risk.loading}
          page={risk.page}
          totalPages={risk.totalPages}
          totalElements={risk.totalElements}
          onPrev={() => risk.loadPage(risk.page - 1)}
          onNext={() => risk.loadPage(risk.page + 1)}
          onAdd={risk.handleAdd}
          onDelete={risk.handleDelete}
          onPageSizeChange={risk.handlePageSizeChange}
        />
      </div>

      {toastError && <Toast message={toastError} onClose={clearToast} />}
    </div>
  )
}
