import { forwardRef } from 'react'
import type { PendingTaskResponse } from '../../api/pending'
import Postit from './Postit'

export interface PostitFollowLayerProps {
  task: PendingTaskResponse
  x: number
  y: number
}

const PostitFollowLayer = forwardRef<HTMLDivElement, PostitFollowLayerProps>(function PostitFollowLayer(
  { task, x, y },
  ref,
) {
  return (
    <div
      ref={ref}
      className="fixed z-[2000] pointer-events-none"
      style={{ left: x, top: y, transform: 'translate(-50%, -50%)' }}
    >
      <Postit task={task} isRecommended={false} onPickUp={() => {}} onOpen={() => {}} />
    </div>
  )
})

export default PostitFollowLayer
