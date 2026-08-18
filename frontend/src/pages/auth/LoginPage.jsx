import { useEffect, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { themes } from '../../theme/themes'
import Wordmark from '../../components/Wordmark'

const SHELF_MEDIA = ['manga', 'tv', 'movie', 'book', 'game']
const SPINE_HEIGHTS = [44, 34, 50, 38, 46]

export default function LoginPage() {
  const buttonRef = useRef(null)
  const { loginWithGoogle } = useAuth()
  const navigate = useNavigate()

  useEffect(() => {
    if (!window.google) return

    window.google.accounts.id.initialize({
      client_id: import.meta.env.VITE_GOOGLE_CLIENT_ID,
      callback: async (response) => {
        const user = await loginWithGoogle(response.credential)
        navigate(user.onboarded ? '/' : '/onboarding', { replace: true })
      },
    })

    window.google.accounts.id.renderButton(buttonRef.current, {
      theme: 'filled_black',
      size: 'large',
      shape: 'rectangular',
    })
  }, [loginWithGoogle, navigate])

  return (
    <div
      className="flex min-h-screen items-center justify-center"
      style={{ background: themes.brand.background }}
    >
      <div className="flex flex-col items-center gap-7">
        <div className="flex flex-col items-center gap-2 text-center">
          <Wordmark size={24} />
          <p className="text-[13px]" style={{ color: '#777788' }}>
            Your entertainment library, tracked on one shelf.
          </p>
        </div>

        <div className="flex items-end gap-1.5">
          {SHELF_MEDIA.map((id, i) => (
            <div
              key={id}
              style={{ width: 8, height: SPINE_HEIGHTS[i], background: themes[id].accent, borderRadius: 2 }}
            />
          ))}
        </div>

        <div ref={buttonRef} />
      </div>
    </div>
  )
}