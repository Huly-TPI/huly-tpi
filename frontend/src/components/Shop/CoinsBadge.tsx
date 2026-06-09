interface CoinsBadgeProps {
  coins: number
}

export function CoinsBadge({ coins }: CoinsBadgeProps) {
  return (
    <div className="flex items-center gap-2 bg-yellow-50 border border-yellow-200 rounded-xl px-4 py-2">
      <span className="text-yellow-500 text-xl">🪙</span>
      <span className="text-yellow-700 font-bold text-lg">{coins.toLocaleString('es-AR')}</span>
      <span className="text-yellow-600 text-sm">monedas</span>
    </div>
  )
}
