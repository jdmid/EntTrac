import { themes } from '../theme/themes'

export default function Wordmark({ size = 24, accent = themes.brand.accent }) {
  return (
    <span style={{ fontSize: size, fontWeight: 650, letterSpacing: '-0.02em', color: 'rgba(255,255,255,0.92)' }}>
      Ent<span style={{ color: accent }}>Trac</span>
    </span>
  )
}