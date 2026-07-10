interface BadgeProps {
  label: string
  className?: string
}

export function Badge({ label, className = '' }: BadgeProps) {
  return (
    <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-[11px] font-bold uppercase tracking-wide ${className}`}>
      {label}
    </span>
  )
}

// Figma exact hex colors per severity
export const SEVERITY_BADGE_CLS: Record<'ALTA' | 'MEDIA' | 'BAJA', string> = {
  ALTA: 'text-[#EF4444]',
  MEDIA: 'text-[#F59E0B]',
  BAJA: 'text-[#BBACA1]',
}

export const RISK_BADGE: Record<'HIGH' | 'MEDIUM' | 'LOW', { label: string; cls: string }> = {
  HIGH:   { label: 'CRÍTICO', cls: 'bg-[#EF4444] text-white' },
  MEDIUM: { label: 'ALTO',    cls: 'bg-[#F59E0B] text-white' },
  LOW:    { label: 'INFO',    cls: 'bg-[#A0AEC0] text-white' },
}
