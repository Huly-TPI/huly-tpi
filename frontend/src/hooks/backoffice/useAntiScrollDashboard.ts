import { useEffect, useState } from 'react'
import {
  getAntiScrollDashboard,
  AntiScrollDashboardResponse,
  getAntiScrollConfig,
  saveAntiScrollConfig,
} from '../../api/admin'

export function useAntiScrollDashboard() {
  const [dashboard, setDashboard] = useState<AntiScrollDashboardResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const [defaultTime, setDefaultTime] = useState<number>(20)
  const [terms, setTerms] = useState<string>('')
  const [configLoading, setConfigLoading] = useState(true)
  const [configSaving, setConfigSaving] = useState(false)
  const [configError, setConfigError] = useState<string | null>(null)
  const [saveSuccess, setSaveSuccess] = useState(false)

  useEffect(() => {
    Promise.all([getAntiScrollDashboard(), getAntiScrollConfig()])
      .then(([dashboardData, configData]) => {
        setDashboard(dashboardData)
        setDefaultTime(configData.defaultPauseIntervalMinutes)
        setTerms(configData.termsAndConditions)
      })
      .catch((err) => {
        console.error(err)
        setError('No se pudieron cargar los datos de antiscroll')
      })
      .finally(() => {
        setLoading(false)
        setConfigLoading(false)
      })
  }, [])

  const handleSaveConfig = async (e: React.FormEvent) => {
    e.preventDefault()
    setConfigSaving(true)
    setSaveSuccess(false)
    setConfigError(null)
    try {
      await saveAntiScrollConfig({
        defaultPauseIntervalMinutes: defaultTime,
        termsAndConditions: terms,
      })
      setSaveSuccess(true)
      setTimeout(() => setSaveSuccess(false), 3000)
    } catch (err) {
      console.error(err)
      setConfigError('No se pudo guardar la configuración')
    } finally {
      setConfigSaving(false)
    }
  }

  const totalModals = dashboard?.totalModalsShown || 0
  const totalRedirects = dashboard?.totalRedirects || 0
  const totalIgnore = totalModals - totalRedirects
  const redirectRate = totalModals > 0 ? (totalRedirects / totalModals) * 100 : 0
  const ignoreRate = totalModals > 0 ? (totalIgnore / totalModals) * 100 : 0

  const maxAppTime = dashboard?.topUsedApps && dashboard.topUsedApps.length > 0
    ? Math.max(...dashboard.topUsedApps.map((a) => a.totalActiveSeconds))
    : 1

  return {
    dashboard,
    loading,
    error,
    defaultTime,
    setDefaultTime,
    terms,
    setTerms,
    configLoading,
    configSaving,
    configError,
    saveSuccess,
    handleSaveConfig,
    totalModals,
    totalRedirects,
    totalIgnore,
    redirectRate,
    ignoreRate,
    maxAppTime,
  }
}
