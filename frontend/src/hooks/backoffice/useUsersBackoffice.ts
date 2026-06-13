import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { getBackofficeUsers, BackofficeUserResponse } from '../../api/admin'

const DAYS = [
  { key: '0', label: 'Lunes' },
  { key: '1', label: 'Martes' },
  { key: '2', label: 'Miércoles' },
  { key: '3', label: 'Jueves' },
  { key: '4', label: 'Viernes' },
  { key: '5', label: 'Sábado' },
  { key: '6', label: 'Domingo' },
]


export function useUsersBackoffice() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [users, setUsers] = useState<BackofficeUserResponse[]>([])
  const [search, setSearch] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const [selectedWeek, setSelectedWeek] = useState('current')
  const [selectedDay, setSelectedDay] = useState('all')

  useEffect(() => {
    getBackofficeUsers()
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
  }, [])

  const filteredUsers = users.filter(
    (u) =>
      (u.name && u.name.toLowerCase().includes(search.toLowerCase())) ||
      (u.email && u.email.toLowerCase().includes(search.toLowerCase()))
  )

  const selectedUserId = id ? Number(id) : null
  const selectedUser = users.find((u) => u.id === selectedUserId)

  const getDailyTime = (dayKey: string) => {
    if (!selectedUser || !selectedUser.dailyScrollTimeSeconds) return 0
    const key = `${selectedWeek === 'current' ? 'current' : 'previous'}_${dayKey}`
    return selectedUser.dailyScrollTimeSeconds[key] || 0
  }

  const currentWeekTotal = DAYS.reduce((sum, day) => sum + (selectedUser?.dailyScrollTimeSeconds?.[`current_${day.key}`] || 0), 0)
  const previousWeekTotal = DAYS.reduce((sum, day) => sum + (selectedUser?.dailyScrollTimeSeconds?.[`previous_${day.key}`] || 0), 0)
  
  const selectedWeekTotal = selectedWeek === 'current' ? currentWeekTotal : previousWeekTotal

  const weekFactor = selectedUser && selectedUser.totalScrollTimeSeconds > 0
    ? selectedWeekTotal / selectedUser.totalScrollTimeSeconds
    : 0

  let activeFactor = 1.0
  if (selectedDay !== 'all') {
    activeFactor = selectedWeekTotal > 0
      ? getDailyTime(selectedDay) / selectedWeekTotal
      : 0
  }

  const filteredTotalTime = selectedUser
    ? (selectedDay === 'all' ? selectedWeekTotal : getDailyTime(selectedDay))
    : 0

  const domainList: { domain: string; seconds: number }[] = []
  if (selectedUser) {
    if (selectedUser.topApps && selectedUser.topApps.length > 0) {
      selectedUser.topApps.forEach((app) => {
        const scaledSeconds = Math.round(app.totalActiveSeconds * weekFactor * activeFactor)
        if (scaledSeconds > 0) {
          domainList.push({
            domain: app.domain,
            seconds: scaledSeconds,
          })
        }
      })
    } else if (selectedUser.mostUsedApp) {
      const app1Seconds = Math.round(selectedUser.mostUsedAppActiveSeconds * weekFactor * activeFactor)
      domainList.push({
        domain: selectedUser.mostUsedApp,
        seconds: app1Seconds,
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
  const hasUsageData = selectedUser ? (!!selectedUser.mostUsedApp || selectedUser.totalScrollTimeSeconds > 0) : false

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
  }
}
