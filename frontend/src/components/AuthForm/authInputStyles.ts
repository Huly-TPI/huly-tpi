export function getInputClassName(hasError: boolean, extraClasses = ''): string {
  return [
    'w-full rounded-xl border bg-[#faf3e8] px-4 py-2.5 text-sm text-[#4a3f32] outline-none transition',
    'placeholder-[#9c8b74]',
    'focus:border-[#b8a88e] focus:ring-2 focus:ring-[#d4c4a8]/40',
    hasError ? 'border-red-400' : 'border-[#d4c4a8]',
    extraClasses,
  ].join(' ')
}