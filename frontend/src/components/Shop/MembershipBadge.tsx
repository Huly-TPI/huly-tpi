import type { Membership } from '../../api/auth'

interface MembershipBadgeProps {
  membership: Membership
}

export function MembershipBadge({ membership }: MembershipBadgeProps) {
  if (!membership.active) {
    return (
      <div className="flex items-center gap-2 bg-gray-50 border border-gray-200 rounded-xl px-4 py-2">
        <span className="text-gray-400 text-xl">☆</span>
        <span className="text-gray-500 text-sm font-medium">Sin membresía</span>
      </div>
    )
  }

  const expiry = membership.expiresAt
    ? new Date(membership.expiresAt).toLocaleDateString('es-AR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
      })
    : null

  return (
    <div className="flex items-center gap-2 bg-green-50 border border-green-200 rounded-xl px-4 py-2">
      <span className="text-green-500 text-xl">★</span>
      <span className="text-green-700 font-bold text-lg">{membership.planCode}</span>
      {expiry && <span className="text-green-600 text-sm">vence {expiry}</span>}
    </div>
  )
}
