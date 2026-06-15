interface WellbeingChartProps {
  points: number[]
  labels: string[]
}

export function WellbeingChart({ points, labels }: WellbeingChartProps) {
  const W = 340
  const H = 110
  const padX = 28
  const padY = 12
  const innerW = W - padX * 2
  const innerH = H - padY * 2
  const minV = 0
  const maxV = 100

  const xs = points.map((_, i) => padX + (i / (points.length - 1)) * innerW)
  const ys = points.map(v => padY + innerH - ((v - minV) / (maxV - minV)) * innerH)

  const polyline = xs.map((x, i) => `${x},${ys[i]}`).join(' ')
  const areaPath = [
    `M ${xs[0]},${H - padY}`,
    ...xs.map((x, i) => `L ${x},${ys[i]}`),
    `L ${xs[xs.length - 1]},${H - padY}`,
    'Z',
  ].join(' ')

  const yTicks = [0, 20, 40, 60, 80]

  return (
    <div className="w-full overflow-x-auto">
      <svg
        viewBox={`0 0 ${W} ${H + 20}`}
        className="w-full min-w-[240px]"
        aria-hidden
      >
        <defs>
          <linearGradient id="wellGrad" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor="#8869AC" stopOpacity="0.20" />
            <stop offset="100%" stopColor="#8869AC" stopOpacity="0.02" />
          </linearGradient>
        </defs>

        {/* Grid lines + Y labels */}
        {yTicks.map(v => {
          const y = padY + innerH - ((v - minV) / (maxV - minV)) * innerH
          return (
            <g key={v}>
              <line x1={padX} y1={y} x2={W - padX} y2={y} className="stroke-[#f0f0f0] dark:stroke-gray-800/60" strokeWidth="1" />
              <text x={padX - 4} y={y + 3.5} fontSize="8" className="fill-[#c4c4c4] dark:fill-gray-600" textAnchor="end">{v}</text>
            </g>
          )
        })}

        <path d={areaPath} fill="url(#wellGrad)" />

        <polyline
          points={polyline}
          fill="none"
          stroke="#8869AC"
          strokeWidth="2.5"
          strokeLinejoin="round"
          strokeLinecap="round"
        />

        {xs.map((x, i) => (
          <circle key={i} cx={x} cy={ys[i]} r="3" fill="#8869AC" />
        ))}

        {xs.map((x, i) => (
          <text key={i} x={x} y={H + 16} fontSize="8" className="fill-[#b0b0b0] dark:fill-gray-500" textAnchor="middle">
            {labels[i]}
          </text>
        ))}
      </svg>
    </div>
  )
}
