import React, { useEffect, useState } from 'react'
import {
  adminActivitiesApi,
  AdminActivityConfig,
  AdminActivitiesKpiResponse,
  AdminActivityPopularityResponse,
  AdminActivityCorrelationResponse,
  AdminActivityImpactResponse,
} from '../../../api/adminActivities'
import { useToast } from '../../../context/toast'

export function useAdminActivities() {
  const [activeTab, setActiveTab] = useState<'metrics' | 'config'>('metrics')
  const [timeframe, setTimeframe] = useState<'total' | 'month' | 'week' | 'today'>('month')
  const [configs, setConfigs] = useState<AdminActivityConfig[]>([])
  const [kpis, setKpis] = useState<AdminActivitiesKpiResponse | null>(null)
  const [popularity, setPopularity] = useState<AdminActivityPopularityResponse[]>([])
  const [correlation, setCorrelation] = useState<AdminActivityCorrelationResponse[]>([])
  const [impact, setImpact] = useState<AdminActivityImpactResponse[]>([])
  const [loading, setLoading] = useState(true)
  const { showToast } = useToast()

  // Modal State
  const [editingConfig, setEditingConfig] = useState<AdminActivityConfig | null>(null)
  const [saveLoading, setSaveLoading] = useState(false)

  const fetchData = async () => {
    setLoading(true)
    try {
      const [configData, kpiData, popularityData, correlationData, impactData] = await Promise.all([
        adminActivitiesApi.getActivitiesConfig(),
        adminActivitiesApi.getKpis(timeframe),
        adminActivitiesApi.getPopularity(timeframe),
        adminActivitiesApi.getCorrelation(timeframe),
        adminActivitiesApi.getImpact(timeframe),
      ])
      setConfigs(configData)
      setKpis(kpiData)
      setPopularity(popularityData)
      setCorrelation(correlationData)
      setImpact(impactData)
    } catch (err) {
      showToast('Error al cargar datos del servidor', 'error')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchData()
  }, [timeframe])

  const handleEditClick = (config: AdminActivityConfig) => {
    setEditingConfig(config)
  }

  const handleUpdateConfigSubmit = async (e: React.FormEvent, configData: AdminActivityConfig) => {
    e.preventDefault()

    const {
      id,
      valenceMin,
      valenceMax,
      arousalMin,
      arousalMax,
      dominanceMin,
      dominanceMax,
      effectValence,
      effectArousal,
      effectDominance,
      title,
      description,
      goalKeywords,
      routePath,
    } = configData

    if (
      valenceMin < -1 || valenceMin > 1 ||
      valenceMax < -1 || valenceMax > 1 ||
      arousalMin < -1 || arousalMin > 1 ||
      arousalMax < -1 || arousalMax > 1 ||
      dominanceMin < -1 || dominanceMin > 1 ||
      dominanceMax < -1 || dominanceMax > 1 ||
      effectValence < -1 || effectValence > 1 ||
      effectArousal < -1 || effectArousal > 1 ||
      effectDominance < -1 || effectDominance > 1
    ) {
      showToast('Todos los rangos VAD y efectos deben estar entre -1.0 y 1.0', 'error')
      return
    }

    if (valenceMin > valenceMax) {
      showToast('El valor mínimo de Valencia no puede superar al máximo', 'error')
      return
    }
    if (arousalMin > arousalMax) {
      showToast('El valor mínimo de Arousal no puede superar al máximo', 'error')
      return
    }
    if (dominanceMin > dominanceMax) {
      showToast('El valor mínimo de Dominancia no puede superar al máximo', 'error')
      return
    }

    if (!title.trim() || !description.trim() || !routePath.trim()) {
      showToast('Título, descripción y ruta son campos obligatorios', 'error')
      return
    }

    setSaveLoading(true)
    try {
      await adminActivitiesApi.updateActivityConfig(id, {
        valenceMin,
        valenceMax,
        arousalMin,
        arousalMax,
        dominanceMin,
        dominanceMax,
        effectValence,
        effectArousal,
        effectDominance,
        title,
        description,
        goalKeywords,
        routePath,
      })
      showToast('Configuración actualizada correctamente', 'success')
      setEditingConfig(null)
      fetchData()
    } catch (err) {
      showToast('Error al guardar la configuración', 'error')
    } finally {
      setSaveLoading(false)
    }
  }

  return {
    activeTab,
    setActiveTab,
    timeframe,
    setTimeframe,
    configs,
    kpis,
    popularity,
    correlation,
    impact,
    loading,
    editingConfig,
    setEditingConfig,
    saveLoading,
    handleEditClick,
    handleUpdateConfigSubmit,
  }
}
