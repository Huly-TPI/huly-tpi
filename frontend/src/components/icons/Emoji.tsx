import { useMemo } from 'react'

interface EmojiProps {
  emoji: string
  className?: string
}

export function Emoji({ emoji, className = 'w-8 h-8' }: EmojiProps) {
  const html = useMemo(
    () => twemoji.parse(emoji, { folder: 'svg', ext: '.svg', className }),
    [emoji, className],
  )
  return <span dangerouslySetInnerHTML={{ __html: html }} />
}
