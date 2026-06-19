import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Timeframe } from '../../types/timeframe'
import {
  getUsers,
  UserResponse,
  getUserActivities,
  getUserAiDiagnostics,
  getUserFinancials,
  getUserAntiScroll,
  UserActivitiesResponse,
  UserAiDiagnosticsResponse,
  UserFinancialsResponse,
  UserAntiScrollResponse
} from '../../api/admin'

const DAYS = [
  { key: '0', label: 'Lunes' },
  { key: '1', label: 'Martes' },
  { key: '2', label: 'Miércoles' },
  { key: '3', label: 'Jueves' },
  { key: '4', label: 'Viernes' },
  { key: '5', label: 'Sábado' },
  { key: '6', label: 'Domingo' },
]

export function useUsers() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [users, setUsers] = useState<UserResponse[]>([])
  const [search, setSearch] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const [selectedWeek, setSelectedWeek] = useState('current')
  const [selectedDay, setSelectedDay] = useState('all')

  const [activeTab, setActiveTab] = useState<'antiscroll' | 'usage' | 'ai' | 'finance'>('usage')

  const [activities, setActivities] = useState<UserActivitiesResponse | null>(null)
  const [activitiesLoading, setActivitiesLoading] = useState(false)
  const [activitiesError, setActivitiesError] = useState<string | null>(null)

  const [aiDiagnostics, setAiDiagnostics] = useState<UserAiDiagnosticsResponse | null>(null)
  const [aiLoading, setAiLoading] = useState(false)
  const [aiError, setAiError] = useState<string | null>(null)

  const [financials, setFinancials] = useState<UserFinancialsResponse | null>(null)
  const [financialsLoading, setFinancialsLoading] = useState(false)
  const [financialsError, setFinancialsError] = useState<string | null>(null)

  const [antiscrollStats, setAntiscrollStats] = useState<UserAntiScrollResponse | null>(null)
  const [antiscrollLoading, setAntiscrollLoading] = useState(false)
  const [antiscrollError, setAntiscrollError] = useState<string | null>(null)

  const [activityTimeframe, setActivityTimeframe] = useState<Timeframe>('total')

  const selectedUserId = id ? Number(id) : null
  const selectedUser = users.find((u) => u.id === selectedUserId)

  useEffect(() => {
    setLoading(true)
    getUsers(search)
      .then((data) => {
        setUsers(data)
      })
      .catch((err) => {
        console.error(err)
        setError('no se pudieron cargar los usuarios')
      })
      .finally(() => {
        setLoading(false)
      })
  }, [search])

  useEffect(() => {
    // Clear data when user changes
    setActivities(null)
    setAiDiagnostics(null)
    setFinancials(null)
    setAntiscrollStats(null)
    setActivitiesError(null)
    setAiError(null)
    setFinancialsError(null)
    setAntiscrollError(null)
    setActivityTimeframe('total')
    setActiveTab('usage')
  }, [selectedUserId])

  useEffect(() => {
    if (selectedUserId && activeTab === 'usage') {
      setActivitiesLoading(true)
      setActivitiesError(null)
      getUserActivities(selectedUserId, activityTimeframe)
        .then((data) => {
          setActivities(data)
        })
        .catch((err) => {
          console.error(err)
          setActivitiesError('No se pudieron cargar las actividades del usuario')
        })
        .finally(() => {
          setActivitiesLoading(false)
        })
    }
  }, [selectedUserId, activityTimeframe, activeTab])

  useEffect(() => {
    if (selectedUserId && activeTab === 'ai') {
      setAiLoading(true)
      setAiError(null)
      getUserAiDiagnostics(selectedUserId)
        .then((data) => {
          setAiDiagnostics(data)
        })
        .catch((err) => {
          console.error(err)
          setAiError('No se pudieron cargar las métricas de la IA')
        })
        .finally(() => {
          setAiLoading(false)
        })
    }
  }, [selectedUserId, activeTab])

  useEffect(() => {
    if (selectedUserId && activeTab === 'finance') {
      setFinancialsLoading(true)
      setFinancialsError(null)
      getUserFinancials(selectedUserId)
        .then((data) => {
          setFinancials(data)
        })
        .catch((err) => {
          console.error(err)
          setFinancialsError('No se pudieron cargar las métricas financieras')
        })
        .finally(() => {
          setFinancialsLoading(false)
        })
    }
  }, [selectedUserId, activeTab])

  useEffect(() => {
    if (selectedUserId && activeTab === 'antiscroll') {
      setAntiscrollLoading(true)
      setAntiscrollError(null)
      getUserAntiScroll(selectedUserId, selectedWeek as 'current' | 'previous', selectedDay)
        .then((data) => {
          setAntiscrollStats(data)
        })
        .catch((err) => {
          console.error(err)
          setAntiscrollError('No se pudieron cargar las métricas de Antiscroll')
        })
        .finally(() => {
          setAntiscrollLoading(false)
        })
    }
  }, [selectedUserId, activeTab, selectedWeek, selectedDay])

  const filteredUsers = users

  const getDailyTime = (dayKey: string) => {
    if (!antiscrollStats || !antiscrollStats.dailyScrollTimeSeconds) return 0
    const key = `${selectedWeek === 'current' ? 'current' : 'previous'}_${dayKey}`
    return antiscrollStats.dailyScrollTimeSeconds[key] || 0
  }

  const filteredTotalTime = antiscrollStats?.totalScrollTimeSeconds || 0

  const domainList: { domain: string; seconds: number }[] = []
  if (antiscrollStats) {
    if (antiscrollStats.topApps && antiscrollStats.topApps.length > 0) {
      antiscrollStats.topApps.forEach((app) => {
        if (app.totalActiveSeconds > 0) {
          domainList.push({
            domain: app.domain,
            seconds: app.totalActiveSeconds,
          })
        }
      })
    } else if (antiscrollStats.mostUsedApp) {
      domainList.push({
        domain: antiscrollStats.mostUsedApp,
        seconds: antiscrollStats.mostUsedAppActiveSeconds,
      })
    }

    const sumTopAppsSeconds = domainList.reduce((sum, d) => sum + d.seconds, 0)
    const remaining = filteredTotalTime - sumTopAppsSeconds
    if (remaining > 0) {
      domainList.push({
        domain: 'Otros sitios',
        seconds: remaining,
      })
    }
  }

  const maxDomainTime = domainList.length > 0 ? Math.max(...domainList.map((d) => d.seconds)) : 1
  const hasUsageData = antiscrollStats ? (!!antiscrollStats.mostUsedApp || antiscrollStats.totalScrollTimeSeconds > 0) : false

  const handleBack = () => {
    setSelectedWeek('current')
    setSelectedDay('all')
    navigate('/backoffice/usuarios')
  }

  return {
    users,
    search,
    setSearch,
    loading,
    error,
    selectedWeek,
    setSelectedWeek,
    selectedDay,
    setSelectedDay,
    filteredUsers,
    selectedUser,
    DAYS,
    getDailyTime,
    filteredTotalTime,
    domainList,
    maxDomainTime,
    hasUsageData,
    handleBack,
    navigate,
    activityTimeframe,
    setActivityTimeframe,
    activeTab,
    setActiveTab,
    activities,
    activitiesLoading,
    activitiesError,
    aiDiagnostics,
    aiLoading,
    aiError,
    financials,
    financialsLoading,
    financialsError,
    antiscrollStats,
    antiscrollLoading,
    antiscrollError,
  }
}
