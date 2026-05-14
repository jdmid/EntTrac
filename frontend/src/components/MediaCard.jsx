import { themes, statusStyles } from '../theme/themes'

function MediaCard({ title, score, status, progress, total, theme, icon, progressLabel, onClick }) {
  const style = statusStyles[status] ?? statusStyles.FINISHED
  const unread = total != null ? Math.max(0, total - (progress ?? 0)) : null

  return (
    <div
      onClick={onClick}
      className="rounded-lg overflow-hidden cursor-pointer hover:opacity-90 transition-opacity"
      style={{ border: '0.5px solid rgba(255,255,255,0.09)' }}
    >
      {/* Cover */}
      <div
        className="w-full aspect-[2/3] flex items-center justify-center relative"
        style={{ background: theme.cardCover }}
      >
        <span style={{ fontSize: 28, color: theme.cardIcon }}>{icon}</span>

        {/* Score pill */}
        <div
          className="absolute top-1.5 right-1.5 rounded-full px-[7px] py-[2px] text-[11px] font-semibold"
          style={{
            background: 'rgba(0,0,0,0.75)',
            border: '0.5px solid rgba(255,255,255,0.15)',
            color: score != null ? '#fbbf24' : '#555566',
          }}
        >
          ★ {score != null ? score : '—'}
        </div>
      </div>

      {/* Card body */}
      <div
        className="px-2.5 pt-2 pb-2.5"
        style={{ background: style.bg, borderTop: `0.5px solid ${style.border}` }}
      >
        {/* Title */}
        <p className="text-[12px] font-medium text-[#d0d0e0] m-0 mb-1 truncate">
          {title}
        </p>

        {/* Status badge */}
        <span
          className="inline-block text-[10px] font-medium px-[7px] py-[2px] rounded-full mb-1.5"
          style={style.badge}
        >
          {style.label}
        </span>

        {/* Progress row */}
        <div className="flex items-center justify-between">
          <span className="text-[10px] text-[#8a8a9a]">
            {progressLabel} {progress ?? 0}{total != null ? ` / ${total}` : ''}
          </span>

          {unread != null && unread > 0 && (
            <span
              className="text-[10px] font-medium px-1.5 py-[1px] rounded-full"
              style={{ background: theme.unreadBadge, color: theme.accent }}
            >
              +{unread}
            </span>
          )}
        </div>
      </div>
    </div>
  )
}

export default MediaCard