import { useEffect, useState } from "react";
import { badgesApi, BadgeResponse } from "../api/badges";

export interface BadgeWithStatus {
    badge: BadgeResponse
    unlocked: boolean
    obtainedAt: string | null
}

export function useBadges() {
    const [badges, setBadges] = useState<BadgeWithStatus[]>([])
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)
    
    const fetchBadges = async () => {
        setLoading(true)
        try {
          const allBadges = await badgesApi.getAll()
            const myBadges = await badgesApi.getMyBadges()
            const earned = new Map(myBadges.map(ub  => [ub.badge.code, ub.obtainedAt]))

           setBadges(allBadges.map((badge): BadgeWithStatus => ({
                    badge,
                    unlocked: earned.has(badge.code),
                    obtainedAt: earned.get(badge.code) ?? null,
                })))
        } catch {
            setError("Error al cargar los badges")
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        fetchBadges()
    }, [])

    return {
        badges,
        loading,
        error,
        refetch: fetchBadges
    }
}