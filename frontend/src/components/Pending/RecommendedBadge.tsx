import { Sparkles } from 'lucide-react'

export default function RecommendedBadge() {
  return (
    <span
      aria-hidden="true"
      className="absolute -top-2 -right-2 flex h-6 w-6 items-center justify-center rounded-full bg-violeta text-white shadow-md"
    >
      <Sparkles className="h-3.5 w-3.5" />
    </span>
  )
}
