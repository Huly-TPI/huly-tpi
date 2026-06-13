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

const DAY_FACTORS: Record<string, number> = {
  '0': 0.15,
  '1': 0.12,
  '2': 0.18,
  '3': 0.22,
  '4': 0.25,
  '5': 0.05,
  '6': 0.03,
}

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

  const weekFactor = selectedWeek === 'current' ? 1.0 : 0.85

  const getDailyTime = (dayKey: string) => {
    if (!selectedUser) return 0
    const factor = DAY_FACTORS[dayKey] || 0
    return Math.round(selectedUser.totalScrollTimeSeconds * weekFactor * factor)
  }

  let activeFactor = 1.0
  if (selectedDay !== 'all') {
    activeFactor = DAY_FACTORS[selectedDay] || 1.0
  }

  const filteredTotalTime = selectedUser
    ? Math.round(selectedUser.totalScrollTimeSeconds * weekFactor * activeFactor)
    : 0

  const domainList: { domain: string; seconds: number }[] = []
  if (selectedUser && selectedUser.mostUsedApp) {
    const app1Seconds = Math.round(selectedUser.mostUsedAppActiveSeconds * weekFactor * activeFactor)
    domainList.push({
      domain: selectedUser.mostUsedApp,
      seconds: app1Seconds,
    })

    const remaining = filteredTotalTime - app1Seconds
    if (remaining > 0) {
      const secondDomain = selectedUser.mostUsedApp === 'instagram.com' ? 'tiktok.com' : 'instagram.com'
      if (remaining > 60) {
        domainList.push({
          domain: secondDomain,
          seconds: Math.round(remaining * 0.6),
        })
        domainList.push({
          domain: 'Otros sitios',
          seconds: Math.round(remaining * 0.4),
        })
      } else {
        domainList.push({
          domain: 'Otros sitios',
          seconds: remaining,
        })
      }
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
