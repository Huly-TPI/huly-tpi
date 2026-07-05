import { useRiskWords } from '../../hooks/backoffice/useRiskWords'
import { useBotConfig } from '../../hooks/backoffice/useBotConfig'
import { Toast } from '../../components/backoffice/Toast'
import { EmotionalCategoriesSection } from '../../components/backoffice/EmotionalCategoriesSection'
import { FlowBuilderSection } from '../../components/backoffice/FlowBuilderSection'
import { BotConfigSection } from '../../components/backoffice/BotConfigSection'
import { RiskDetectionSection } from '../../components/backoffice/RiskDetectionSection'
import { ActivitiesSection } from '../../components/backoffice/ActivitiesSection'
import { WellbeingSection } from '../../components/backoffice/WellbeingSection'
import { TrainingLogsSection } from '../../components/backoffice/TrainingLogsSection'
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
        subtitle="Configuración emocional, automatización terapéutica y monitoreo inteligente"
      />

      <EmotionalCategoriesSection />

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-[3fr_1fr]">
        <div className="flex flex-col gap-4">
          <FlowBuilderSection />
          <BotConfigSection
            config={botConfig.config}
            loading={botConfig.loading}
            onSave={botConfig.handleSave}
          />
        </div>
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

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <ActivitiesSection />
        <WellbeingSection />
      </div>

      <TrainingLogsSection />

      {toastError && <Toast message={toastError} onClose={clearToast} />}
    </div>
  )
}
